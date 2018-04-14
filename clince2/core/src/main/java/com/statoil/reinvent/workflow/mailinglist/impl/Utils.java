package com.statoil.reinvent.workflow.mailinglist.impl;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static String INGRESS_NODE = "par/text";

    public static String getIngress(Page page) {
        final Resource resource = page.getContentResource(INGRESS_NODE);

        if (resource == null) {
            throw new IllegalArgumentException("Could not find ingress on page: " + page.getPath());
        }

        ValueMap properties = resource.adaptTo(ValueMap.class);

        if (properties.isEmpty()) {
            throw new IllegalArgumentException("Could not adapt resource to ValueMap");
        }

        String text = properties.get("text", String.class);
        String textWithoutH3AndLinebreaks = text.replace("<h3>", StringUtils.EMPTY).replace("</h3>", StringUtils.EMPTY).replace("\r\n", " ").replace("\n", " ").trim();
        String unescapedHtml = StringEscapeUtils.unescapeHtml(textWithoutH3AndLinebreaks);

        return unescapedHtml;
    }

    public static Page getPage(String payloadPath, ResourceResolver resourceResolver) {
        Resource resource = resourceResolver.getResource(payloadPath);
        Page page = resource.adaptTo(Page.class);

        return page;
    }
    
    public static String setEmptyonNull(String text){
    	if(text ==  null){
    		return StringUtils.EMPTY;
    	}
		return text;
    }
}
