package com.statoil.reinvent.rss;

import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.Writer;

public class RssFeedWriter {

    private Writer writer;
    private RssFeed rssFeed;

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    public RssFeedWriter(RssFeed rssFeed, Writer writer) {
        this.rssFeed = rssFeed;
        this.writer = writer;
    }

    private void createNode(XMLEventWriter eventWriter, String name, String value, Boolean isCdata) throws XMLStreamException {
        StartElement startElement = eventFactory.createStartElement(StringUtils.EMPTY, StringUtils.EMPTY, name);
        eventWriter.add(startElement);

        Characters characters = isCdata ? eventFactory.createCData(value) : eventFactory.createCharacters(value);
        eventWriter.add(characters);

        EndElement endElement = eventFactory.createEndElement(StringUtils.EMPTY, StringUtils.EMPTY, name);
        eventWriter.add(endElement);
    }

    private void createNode(XMLEventWriter eventWriter, String name, String value) throws XMLStreamException {
        createNode(eventWriter, name, value, false);
    }

    private void createCdataNode(XMLEventWriter eventWriter, String name, String value) throws XMLStreamException {
        createNode(eventWriter, name, value, true);
    }

    private void createItem(XMLEventWriter eventWriter, RssItem rssItem) throws XMLStreamException {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        StartElement start = eventFactory.createStartElement(StringUtils.EMPTY, StringUtils.EMPTY, "item");
        eventWriter.add(start);

        createNode(eventWriter, "title", rssItem.getTitle());
        createNode(eventWriter, "link", rssItem.getLink());
        createNode(eventWriter, "category", rssItem.getCategory());
        createCdataNode(eventWriter, "description", rssItem.getDescription());
        createNode(eventWriter, "pubDate", rssItem.getPubDate());

        EndElement end = eventFactory.createEndElement("item", StringUtils.EMPTY, StringUtils.EMPTY);
        eventWriter.add(end);
    }

    private void createChannel(XMLEventWriter eventWriter, RssFeed rssFeed) throws XMLStreamException {
        XMLEvent end = eventFactory.createDTD("\n");
        eventWriter.add(eventFactory.createStartElement(StringUtils.EMPTY, StringUtils.EMPTY, "channel"));
        eventWriter.add(end);

        createNode(eventWriter, "title", rssFeed.getTitle());
        createNode(eventWriter, "link", rssFeed.getLink());
        createNode(eventWriter, "description", rssFeed.getDescription());

        for (RssItem rssItem: rssFeed.getItems()) {
            createItem(eventWriter, rssItem);
        }

        end = eventFactory.createEndElement("channel", StringUtils.EMPTY, StringUtils.EMPTY);
        eventWriter.add(end);
    }

    private void createRss(XMLEventWriter eventWriter, RssFeed rssFeed) throws XMLStreamException {
        StartElement start = eventFactory.createStartElement(StringUtils.EMPTY, StringUtils.EMPTY, "rss");
        eventWriter.add(start);
        eventWriter.add(eventFactory.createAttribute("version", "2.0"));

        createChannel(eventWriter, rssFeed);

        EndElement end = eventFactory.createEndElement("rss", StringUtils.EMPTY, StringUtils.EMPTY);
        eventWriter.add(end);
    }

    public void write() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(writer);

        StartDocument startDocument = eventFactory.createStartDocument();
        eventWriter.add(startDocument);
        XMLEvent end = eventFactory.createDTD("\n");
        eventWriter.add(end);

        createRss(eventWriter, rssFeed);

        eventWriter.flush();
        eventWriter.close();
    }
}
