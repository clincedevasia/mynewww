package com.statoil.reinvent.rss;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.servlets.RssFeedServlet;

public class RSSFeedBuilder {
	private static String XML_BLOCK = "\n";
	private static String XML_INDENT = "\t";

	private static XMLOutputFactory output = XMLOutputFactory.newInstance();;
	private static XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	private static XMLEvent endSection = eventFactory.createDTD(XML_BLOCK);
	
	private static final Logger logger = LoggerFactory.getLogger(RssFeedServlet.class);

	public static void writeTaggedPages(SlingHttpServletRequest request, String rssFeedTitle, String[] tagIds, String lang, String feedType, List<Page> feedPages,
										ResourceResolver resourceResolver, Writer outputWriter) {
		Externalizer externalizer = resourceResolver.adaptTo(Externalizer.class);
		try {
			XMLEventWriter writer;

			// Both the English and the Norwegian RSS feed for vacant positions in the existing solution use the date
			// format Sun, 13 Nov 2016 10:53:29 GMT. It would have been logical to use a Norwegian date format for the
			// Norwegian feed, but to prevent breaking changes we also only use one format.
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

			writer = output.createXMLEventWriter(outputWriter);
			StringBuffer commaSeparatedIds = new StringBuffer();
			for (String tagId : tagIds) {
				commaSeparatedIds.append(tagId + ", ");
			}
			commaSeparatedIds.deleteCharAt(commaSeparatedIds.length() - 1);
			commaSeparatedIds.deleteCharAt(commaSeparatedIds.length() - 1);
			createRSSHeader(request, writer, lang, feedType, rssFeedTitle, resourceResolver);
			for (Page feedPage : feedPages) {
				if (feedPage != null) {
					writer.add(eventFactory.createStartElement("", "", "item"));
					writer.add(endSection);
					createNode(writer, "title", feedPage.getTitle());

					Resource resource = feedPage.getContentResource("par/text");
					if (resource == null) {
						logger.error(String.format("Page '%s' at '%s' does not have par/text node.", feedPage.getTitle(), feedPage.getPath()));
					} else {
						ValueMap properties = resource.adaptTo(ValueMap.class);
						String text = properties.get("text", String.class);

						if (StringUtils.isBlank(text)) {
							logger.error(String.format("Page '%s' at '%s' has blank par/text/text property.", feedPage.getTitle(), feedPage.getPath()));
							createNode(writer, "description", StringUtils.EMPTY);
						} else {
							text = text.replaceAll("<h3>", StringUtils.EMPTY).replaceAll("</h3>", StringUtils.EMPTY).trim();
							createNode(writer, "description", text);
						}
					}

					// Externalize the URL
					// http://blogs.adobe.com/experiencedelivers/experience-management/getting_to_know_theexternalizer/
					createNode(writer, "link", externalizer.absoluteLink(request, request.getScheme(), feedPage.getPath()) + ".html");

					String publishDateString = dateFormat.format(feedPage.getLastModified().getTime());
					createNode(writer, "pubDate", publishDateString);

					writer.add(eventFactory.createEndElement("", "", "item"));
					writer.add(endSection);
				}
			}
			createRSSFooter(writer);

			writer.close();
		} catch (XMLStreamException e) {
			logger.error("XMLStreamException occurred", e);
		}
	}

	private static void createRSSHeader(SlingHttpServletRequest request, XMLEventWriter writer, String lang, String feedType, String printableKeywords, ResourceResolver resourceResolver) {
		Externalizer externalizer = resourceResolver.adaptTo(Externalizer.class);
		StartDocument startDocument = eventFactory.createStartDocument();
		
		try {
			writer.add(startDocument);
			writer.add(endSection);
			StartElement rssStart = eventFactory.createStartElement("", "", "rss");
			writer.add(rssStart);
			writer.add(eventFactory.createAttribute("version", "2.0"));
			writer.add(endSection);

			writer.add(eventFactory.createStartElement("", "", "channel"));
			writer.add(endSection);
		} catch (XMLStreamException e) {
			logger.error("XMLStreamException occurred", e);
		}
		createNode(writer, "title", "Statoil - " + printableKeywords);

		createNode(writer, "link", externalizer.absoluteLink(request, request.getScheme(), "/services/wcwfeed?lang=" + lang + "&feedtype=" + feedType));
		createNode(writer, "ttl", "60");
	}

	private static void createRSSFooter(XMLEventWriter writer) {
		try {
			writer.add(endSection);
			writer.add(eventFactory.createEndElement("", "", "channel"));
			writer.add(endSection);
			writer.add(eventFactory.createEndElement("", "", "rss"));

			writer.add(endSection);
			writer.add(eventFactory.createEndDocument());
			writer.close();
		} catch (XMLStreamException e) {
			logger.error("XMLStreamException occurred", e);
		}
	}

	private static void createNode(XMLEventWriter eventWriter, String name, String value) {
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent endSection = eventFactory.createDTD(XML_BLOCK);
		XMLEvent tabSection = eventFactory.createDTD(XML_INDENT);

		StartElement sElement = eventFactory.createStartElement("", "", name);
		try {
			eventWriter.add(tabSection);
			eventWriter.add(sElement);

			Characters characters = eventFactory.createCharacters(value);
			eventWriter.add(characters);

			EndElement eElement = eventFactory.createEndElement("", "", name);
			eventWriter.add(eElement);
			eventWriter.add(endSection);
		} catch (XMLStreamException e) {
			logger.error("XMLStreamException occurred", e);
		}
	}
}