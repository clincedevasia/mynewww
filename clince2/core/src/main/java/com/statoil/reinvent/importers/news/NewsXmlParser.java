package com.statoil.reinvent.importers.news;

import com.statoil.reinvent.importers.XmlHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by andreas on 04/08/15.
 */
public class NewsXmlParser extends XmlHandler {

    private static final Map<String, String> attrsMap;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("page", "href");
        attrsMap = Collections.unmodifiableMap(aMap);
    }

    List<NewsData> list = new ArrayList<>();

    private final DateFormat df = new SimpleDateFormat("MMM dd, yyyy");

    public NewsXmlParser() {
        super(new String[] {"title",
                "category",
                "summary",
                "published",
                "xhtml:body",
                "downloads"},
                attrsMap,
                "page");
    }

    public List<NewsData> listData(InputStream data) throws IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(data, this);
        } catch (SAXException se) {
            throw new IOException(se);
        } catch (ParserConfigurationException pce) {
            throw new IOException(pce);
        }
        return list;
    }

    public void parseData(Map<String,List<String>> data, Map<String, String> attrs) throws SAXException {
        if (getString(data, "xhtml:body") != null && getString(data, "published") != null) {
            try {
                list.add(new NewsData(getString(data, "title"),
                        getString(data, "category"),
                        getString(data, "summary"),
                        getString(data, "published"),
                        getString(data, "xhtml:body"),
                        attrs.get("href"),
                        createDownloadData(getString(data, "downloads"))));
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    private List<NewsData.Download> createDownloadData(String downloads) {
        if (downloads == null) {
            return new ArrayList<>(0);
        }
        Pattern pattern = Pattern.compile("href=\"(.*?)\".*?>(.*)?<");
        Matcher matcher = pattern.matcher(downloads);
        List<NewsData.Download> downloadList = new ArrayList<>();
        while (matcher.find()) {
            try {
                downloadList.add(new NewsData.Download(matcher.group(2), matcher.group(1).replace("autaccept", "www")));
            } catch (IOException e) {
                // Log error here
            }
        }
        return downloadList;
    }
}
