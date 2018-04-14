package com.statoil.reinvent.servlets;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;
import com.statoil.reinvent.constants.StatoilConstants;
import com.statoil.reinvent.services.SitemapFactory;

/**
 * Servlet for generating site map. Based on SiteMapServlet in ACS Commons.
 */
@SlingServlet(paths = { "/services/sitemap" }, methods = { "GET" })
public class SiteMapServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = -5277975071013101609L;

	private static final Logger LOG = LoggerFactory.getLogger(LanguageServlet.class);
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
	private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";
	private static final String TEXT_XML = "text/xml";
	private static final String TEXT_HTML = "text/html";
	private static final String LANG_PARAM = "lang";

	@Reference
	private SitemapFactory sitemapFactory;
	private ResourceResolver resourceResolver;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

		try {
			resourceResolver = request.getResourceResolver();
			String requestedLanguage = request.getParameter(LANG_PARAM);
			if (StringUtils.isNotEmpty(requestedLanguage)) {
				response.setContentType(TEXT_XML);
				ResourceResolver resourceResolver = request.getResourceResolver();
				PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

				final String protocol = "https://";
				String serverName = request.getServerName();
				LOG.error("Generating site map for " + protocol + serverName);
				XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
				XMLStreamWriter stream = outputFactory.createXMLStreamWriter(response.getWriter());
				stream.writeStartDocument("1.0");
				stream.writeStartElement("", "urlset", NS); 
				stream.writeNamespace("", NS);

				List<String> languagePaths = sitemapFactory.getSitemapConfigurations(serverName);
				if (languagePaths != null) {
					for (String languagePath : languagePaths) {
						Page page = pageManager.getPage(languagePath);
						if (page != null) {
							String languageCode = page.getLanguage(false).getLanguage();
							if (languageCode.equals(requestedLanguage)) {
								write(page, stream, protocol, serverName);
								Iterator<Page> children = page.listChildren(new PageFilter(false, true), true);
								while (children.hasNext()) {
									write(children.next(), stream, protocol, serverName);
								}
								break;
							}
						} else {
							throw new ConfigurationException(serverName, "Specified path is not valid");
						}
					}
				} else {
					throw new ConfigurationException(serverName, "Language paths are not configured.");
				}

				stream.writeEndElement();
				stream.writeEndDocument();
			} else {
				response.setContentType(TEXT_HTML);
				response.getWriter()
						.write("Language Code is required. Please pass ISO Language Code to 'lang' parameter");
			}
		} catch (XMLStreamException | ConfigurationException e) {
			LOG.error("Exception due to", e);
		}
	}

	private boolean isHidden(final Page page) {
		return page.getProperties().get(NameConstants.PN_HIDE_IN_NAV, false);
	}

	private void write(Page page, XMLStreamWriter stream, String protocol, String serverName)
			throws XMLStreamException {
		if (isHidden(page)) {
			return;
		}

		stream.writeStartElement(NS, "url");
		String pagePath = sanitizePagePath(page.getPath()) + StatoilConstants.Extensions.HTML_EXTENSION;
		writeElement(stream, "loc", protocol + serverName + pagePath);

		Calendar cal = page.getLastModified();
		if (cal != null) {
			writeElement(stream, "lastmod", DATE_FORMAT.format(cal));
		}

		stream.writeEndElement();
	}

	private void writeElement(final XMLStreamWriter stream, final String elementName, final String text)
			throws XMLStreamException {
		stream.writeStartElement(NS, elementName);
		stream.writeCharacters(text);
		stream.writeEndElement();
	}
	
	/**
	 * This method removes page /content/application name
	 * @param pagePath
	 * @return
	 */
	private static String sanitizePagePath(String pagePath) {
		int indexStart = StringUtils.ordinalIndexOf(pagePath, "/", 3);
		return pagePath.substring(indexStart, pagePath.length());
	}

}
