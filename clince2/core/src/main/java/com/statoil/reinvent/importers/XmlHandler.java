package com.statoil.reinvent.importers;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public abstract class XmlHandler extends DefaultHandler {
    private final String[] values;
    private final Map<String, String> attrs;
    private Map<String, String> fetchedAttrs;
    private Map<String, List<String>> fetched;
    private final String startElement;
    private boolean inner = false;
    private String currentElm = null;

    private StringBuilder tempVal;

    public abstract void parseData(Map<String,List<String>> data, Map<String, String> attrs) throws SAXException;

    public XmlHandler(String[] values, String startElement) {
        this(values, new HashMap<String, String>(), startElement);
    }

    public XmlHandler(String[] values, Map<String, String> attrs, String startElement) {
        this.values = values;
        this.attrs = attrs;
        this.startElement = startElement;
        this.fetchedAttrs = new HashMap<>();
        tempVal = new StringBuilder();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(inner) {
            StringBuilder attrs = new StringBuilder();
            for (int i = 0; i < attributes.getLength(); i++) {
                attrs.append(" ").append(attributes.getQName(i)).append("=\"").append(attributes.getValue(i)).append("\"");
            }
            tempVal.append("<").append(qName).append(attrs.toString()).append(">");
        }
        if(attrs.containsKey(qName)) {
            String key = attrs.get(qName);
            String value = attributes.getValue(attrs.get(qName));
            fetchedAttrs.put(key, value);
        }
        if(qName.equalsIgnoreCase(startElement)) {
            fetched = new HashMap<>();
            for (String key : values) {
                fetched.put(key, new ArrayList<String>());
            }
        }
        for (String elm : values) {
            if (elm.equalsIgnoreCase(qName)) {
                tempVal = new StringBuilder();
                inner = true;
                currentElm = qName;
            }
        }
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal.append(StringEscapeUtils.unescapeXml(new String(ch, start, length)));
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(currentElm != null && currentElm.equalsIgnoreCase(qName)) {
            inner = false;
        } else if (inner){
            tempVal.append("</" + qName + ">");
        }
        if (qName.equalsIgnoreCase(startElement)) {
            parseData(fetched, fetchedAttrs);
        } else {
            for(String key : values) {
                if(qName.equalsIgnoreCase(key)) {
                    fetched.get(key).add(tempVal.toString());
                }
            }
        }
    }

    public static Long getLong(Map<String,List<String>> data, String key) {
        return Long.parseLong(data.get(key).get(0));
    }

    public static String getString(Map<String,List<String>> data, String key) {
        if (data.get(key).size() == 0) {
            return null;
        }
        return data.get(key).get(0);
    }

    public static Date getDate(Map<String,List<String>> data, String key, DateFormat df) throws SAXException {
        try {
            return df.parse(data.get(key).get(0));
        }  catch (ParseException e) {
            throw new SAXException(e);
        }
    }
}
