package com.statoil.reinvent.importers.news;

import com.statoil.reinvent.importers.ImportData;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsData extends ImportData {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsData.class);

    public final String year;
    public final String day;
    public final String month;
    public final String body;
    public final String oldUrl;
    public final String language;
    public final String nodeName;
    public final List<Download> downloads;
    public final List<Image> images;

    public final Date published;

    public NewsData(String title,
                    String category,
                    String summary,
                    String published,
                    String body,
                    String oldUrl,
                    List<Download> downloads) throws IOException {


        put("jcr:title", title);
        put("jcr:description", summary);
        put("category", category);

        List<Date> dates = findDates(published);

        this.published = dates.get(0);

        put("published", this.published);

        if (dates.size() > 1) {
            put("updated", dates.get(1));
        }

        this.language = findLanguage(oldUrl);

        this.oldUrl = oldUrl;
        put("oldUrl", this.oldUrl);
        this.downloads = downloads;

        this.nodeName = findNodeName(this.oldUrl);

        Calendar publishedDate = Calendar.getInstance();
        publishedDate.setTime(this.published);
        this.year = Integer.toString(publishedDate.get(Calendar.YEAR));
        this.day = String.format("%02d", publishedDate.get(Calendar.DAY_OF_MONTH));
        this.month = String.format("%02d", publishedDate.get(Calendar.MONTH) + 1);
        String tempBody = removeBodyTags(body);
        this.images = findImages(tempBody);
        this.body = rewriteUrls(this.images, tempBody);
    }

    private List<Date> findDates(String s) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd, HH:mm zzz");
        List<Date> results = new ArrayList<>();
        if (s == null) {
            return results;
        }
        Pattern pattern = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d, \\d\\d:\\d\\d \\w\\w\\w");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            try {
                results.add(df.parse(matcher.group()));
            } catch (ParseException e) {
                //LOGGER.error("Couldn't parse date", e);
            }
        }
        return results;
    }

    private String removeBodyTags(String body) {
        String newBody = body.replaceAll("\\s\\s+", " ").replaceAll("\n", "");
        newBody = newBody.replaceAll("src=\"/no", "src=\"http://www.statoil.com/no");
        newBody = newBody.replaceAll("src=\"/en", "src=\"http://www.statoil.com/en");
        newBody = newBody.replaceAll("src=\"data.*?\"", "");
        return newBody;
    }

    private String rewriteUrls(List<Image> images, String body) {
        String newBody = body;
        newBody = newBody.replaceAll("http://www.statoil.com/icons/ecblank.gif", "");
        newBody = newBody.replaceAll(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
        for (Image image : images) {
            newBody = newBody.replaceAll(image.url, "images/" + image.fileName.replaceAll(" ", "%20"));
        }
        return newBody;
    }

    private List<Image> findImages(String body) throws IOException {
        List<Image> imageList = new ArrayList<>();
        Pattern pattern = Pattern.compile("src=\"(http://www.statoil.com.*?)\"");
        Matcher matcher = pattern.matcher(body);

        // Find all matches
        while (matcher.find()) {
            // Get the matching string
            String match = matcher.group(1);
            imageList.add(new Image(match));
        }
        return imageList;
    }

    private String findLanguage(String oldUrl) {
        Pattern pattern = Pattern.compile("com/(\\w\\w)/");
        Matcher matcher = pattern.matcher(oldUrl);
        matcher.find();
        return matcher.group(1);
    }

    public static class Image {
        public final String url;
        public final String fileName;
        public Image(String url) throws IOException {
            this.url = url;
            String[] split = url.split("/");
            try {
                this.fileName = URLDecoder.decode(split[split.length-1], "UTF-8").trim();
            } catch (UnsupportedEncodingException e) {
                throw new IOException(e);
            }
        }
    }

    public static class Download {

        public final String name;
        public final String fileName;
        public final String url;

        public Download(String name, String url) throws IOException {
            this.url = url;
            String[] split = url.split("/");
            try {
                this.fileName = URLDecoder.decode(split[split.length - 1], "UTF-8").trim();
            } catch (UnsupportedEncodingException e) {
                throw new IOException(e);
            }
            String[] extArr = this.fileName.split("\\.");
            String ext = extArr[extArr.length -1];
            this.name = name.replace("."+ext, "");
        }

        public String toString() {
            return "URL[" + this.url + "] Filename[" + this.fileName + "] Name[" + this.name + "]";
        }
    }

    public Node writeDownloadsToNode(Node node) throws RepositoryException {
        if (downloads.isEmpty()) {
            return node;
        }
        Node downloadsNode = null;
        Node pNode = node.getParent();
        if (pNode.hasNode("downloads")) {
            downloadsNode = pNode.getNode("downloads");
        } else {
            downloadsNode = pNode.addNode("downloads", "nt:folder");
        }
        Node infoNode = node.addNode("downloadsInfo", "nt:unstructured");
        for (Download download : downloads) {
            if (!downloadsNode.hasNode(download.fileName)) {
                try {
                    infoNode.setProperty(download.fileName, download.name);
                    Node fileNode = downloadsNode.addNode(download.fileName, "nt:file");
                    Node jcrNode = fileNode.addNode("jcr:content", "nt:resource");
                    ValueFactory factory = jcrNode.getSession().getValueFactory();
                    Binary binary = null;
                    binary = factory.createBinary(readFromUrl(download.url));
                    jcrNode.setProperty("jcr:data", binary);
                } catch (IOException e) {
                    LOGGER.error("Error downloading file", e);
                    if (downloadsNode.hasNode(download.fileName)) {
                        downloadsNode.getNode(download.fileName).remove();
                    }
                }
            }
        }
        return downloadsNode;
    }

    public Node writeImagesToNode(Node node) throws RepositoryException {
        Node imagesNode = null;
        if (images.isEmpty()) {
            return node;
        }
        if (node.hasNode("images")) {
            imagesNode = node.getNode("images");
        } else {
            imagesNode = node.addNode("images", "nt:folder");
        }
        for (Image image : images) {
            if (!imagesNode.hasNode(image.fileName)) {
                try {
                    Node fileNode = imagesNode.addNode(image.fileName, "nt:file");
                    Node jcrNode = fileNode.addNode("jcr:content", "nt:resource");
                    ValueFactory factory = jcrNode.getSession().getValueFactory();
                    Binary binary = null;
                    binary = factory.createBinary(readFromUrl(image.url));
                    jcrNode.setProperty("jcr:data", binary);
                } catch (IOException e) {
                    LOGGER.error("Error downloading image", e);
                    if (imagesNode.hasNode(image.fileName)) {
                        imagesNode.getNode(image.fileName).remove();
                    }
                }
            }
        }
        return imagesNode;
    }

    private InputStream readFromUrl(String url) throws IOException {
        return new URL(url).openStream();
    }

    private String findNodeName(String oldUrl) throws UnsupportedEncodingException {
        String[] url = oldUrl.split("/");
        String nodeName = URLDecoder.decode(url[url.length-1].split("\\.")[0], "UTF-8").trim();
        return nodeName.replaceAll("[^a-zA-Z0-9]", "");
    }
}
