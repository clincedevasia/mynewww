package com.statoil.reinvent.utils;

import com.day.cq.dam.api.Asset;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Locale;

public class ImageUtil {

    private static final String middlePath=".transform/";
    private static final String endPath="/image";
    public static final String RENDITION_EXTRA_SMALL="extra-small";
    public static final String RENDITION_SMALL="small";
    public static final String RENDITION_MEDIUM="medium";
    public static final String RENDITION_LARGE="large";
    public static final String RENDITION_EXTRA_LARGE="extra-large";
    public static final String RENDITION_LIST="list";
    public static final String PLACEHOLDER_IMG="/etc/designs/statoil/images/page/banner-placeholder.png";

    public static String getSrc(Resource resource, String rendition){
        if(resource==null)
            return PLACEHOLDER_IMG;
        return getSrc(resource.getPath(), rendition);
    }

    public static String getSrc(String resourcePath, String rendition){
        return getSrc(resourcePath, rendition, true);
    }

    public static String getSrc(String resourcePath, String rendition, boolean placeholder) {
        return getSrc(resourcePath, rendition, placeholder, new String[0]);
    }

    public static String getSrc(String resourcePath, String rendition, boolean placeholder, String... transforms) {
        if((resourcePath==null || resourcePath.equals("")) && placeholder) {
            return PLACEHOLDER_IMG;
        } else if (resourcePath==null || resourcePath.equals("")) {
            return null;
        }
        String ext = FileUtil.getExtension(resourcePath);
        if(ext!=null && ext.toLowerCase().equals("svg")){
            return resourcePath;
        }
        String s = resourcePath + middlePath + rendition;
        for (String transform : transforms) {
            s += "/" + transform;
        }
        s += endPath + "." + ext;
        return s.replaceAll(" ", "%20").replaceAll(",","%2C");
    }

    public static String getAlternateText(Asset asset, String language) {
        if (asset != null) {
            return getAlternateText(asset.adaptTo(Resource.class), language);
        }
        return null;
    }

    public static String getAlternateText(Resource resource, String language) {
        if (resource != null) {
            ValueMap metadataValueMap = resource.getChild("jcr:content").getChild("metadata").getValueMap();
            if(metadataValueMap!=null){
                String altText = metadataValueMap.get("alt-"+language, String.class);
                if (StringUtils.isNotBlank(altText)) {
                    return altText;
                }
                return metadataValueMap.get("alt-en", String.class);
            }

        }
        return null;
    }

    public static String getAlternateText(Asset asset, Locale locale) {
        if(locale!=null){
            return getAlternateText(asset, locale.getLanguage());
        }
        return null;
    }

    public static String getAlternateText(Resource resource, Locale locale) {
        return getAlternateText(resource, locale.getLanguage());
    }
}
