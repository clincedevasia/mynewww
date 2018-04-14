package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;
import com.statoil.reinvent.editor.fields.Multifield;

public class Footer extends BaseComponent {

    private static final Logger LOG = LoggerFactory.getLogger(Footer.class);

    private List<LinkItem> links;
    private List<LinkItem> linksTwo;
    private List<LinkItem> socialLinks;
    private String title;
    private String copyright;

    @Override
	protected void activated() throws Exception {
    	LOG.debug("Inside Footer component wcmuse class");
        try {
            Node footerNode = findFooterNode();
            Property titleProperty = footerNode.getProperty("title");

            if (titleProperty != null) {
                title = titleProperty.getString();
            } else {
                title = "No title";
            }

            createLinks(footerNode);
            populateSocialLinks(footerNode);
        } catch (PathNotFoundException e) {
            title = "No title";
        }

        try{
            Node footerNode = findFooterNode();

            Property copyrightProperty = footerNode.getProperty("copyright");
            if(copyrightProperty!=null){
                copyright= copyrightProperty.getString();
            }else{
                int year = Calendar.getInstance().get(Calendar.YEAR);
                copyright = "Copyright © "+year+" Statoil ASA";
            }
        }catch (PathNotFoundException e){
            int year = Calendar.getInstance().get(Calendar.YEAR);
            copyright = "Copyright © "+year+" Statoil ASA";
        }
	}

    private Node findFooterNode() throws Exception {
        Page rootPage = getCurrentPage().getAbsoluteParent(2);
        Node rootNode = rootPage.getContentResource().adaptTo(Node.class);
        return rootNode.getNode("footer");
    }

    private void createLinks(Node footerNode) {
        links = new ArrayList<>();
        linksTwo = new ArrayList<>();
        List<JsonObject> jsonObjects = Multifield.getObjects(footerNode, "links");

        //Split the links into two lists for mobile and desktop view
        for (int i = 0; i < jsonObjects.size(); i++) {
            if (i + 1 <= (int) Math.ceil(jsonObjects.size() / 2.0)) {
                links.add(new LinkItem(jsonObjects.get(i)));
            } else {
                linksTwo.add(new LinkItem(jsonObjects.get(i)));
            }
        }
    }


    private void populateSocialLinks(Node footerNode) throws Exception {
        socialLinks = new ArrayList<>();
        String[] channels = {"facebook", "twitter", "instagram", "youtube", "linkedin","google-plus"};
        for (String channel : channels) {
            LinkItem socialLinkItem = createSocialLink(channel, footerNode);
            if (socialLinkItem != null) {
                socialLinks.add(socialLinkItem);
            }
        }
    }

    private LinkItem createSocialLink(String name, Node node) throws Exception {
        Property key;
        Property keyUrl;
        try {
            key = node.getProperty(name);
            keyUrl = node.getProperty(name + "Url");
        } catch (PathNotFoundException e) {
            return null;
        }
        if (key != null && "true".equals(key.getString()) && keyUrl != null) {
            return new LinkItem(name, keyUrl.getString());
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public String getCopyright(){
        return copyright;
    }

    public List<LinkItem> getLinks() {
        return links;
    }

    public List<LinkItem> getLinksTwo() {
        return linksTwo;
    }

    public List<LinkItem> getSocialLinks() {
        return socialLinks;
    }

    public boolean isShowSocialLinks() {
        return socialLinks != null && !socialLinks.isEmpty();
    }

    public static class LinkItem {
        private String linkTitle;
        private String linkUrl;

        public LinkItem(String linkTitle, String linkUrl) {
            this.linkTitle = linkTitle;
            this.linkUrl = linkUrl;
        }

        public LinkItem(JsonObject jsonObject) {
            this.linkTitle = jsonObject.get("title").getAsString();
            this.linkUrl = jsonObject.get("link").getAsString()+".html";
        }

        public String getLinkTitle() {
            return linkTitle;
        }

        public String getLinkUrl() {
            return linkUrl;
        }
    }

}
