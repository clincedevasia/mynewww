package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.WCMMode;
import com.statoil.reinvent.utils.ImageUtil;

public class AdaptiveImage extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(AdaptiveImage.class);
    private String largeFileReference;
    private String mediumFileReference;
    private String smallFileReference;

    private final String PLACEHOLDER_IMG="/etc/designs/statoil/images/0.gif";
    private boolean empty = false;
    private Resource resource;
    private ArrayList<String> classList = new ArrayList<String>();
    private Locale language;

    @Override
	protected void activated() throws Exception {
    	LOG.debug("Inside adaptive image component wcmuse class");
    	 boolean isEdit = WCMMode.fromRequest(getRequest()) == WCMMode.EDIT;
         boolean isDesign = WCMMode.fromRequest(getRequest()) == WCMMode.DESIGN;

         largeFileReference = getProperties().get("fileReference1", String.class);
         mediumFileReference = getProperties().get("fileReference2", String.class);
         smallFileReference = getProperties().get("fileReference3", String.class);

         if((largeFileReference ==null || largeFileReference.equals("")) && (isEdit|| isDesign)){
             classList.add("cq-dd-image");
             classList.add("cq-placeholder");
             classList.add("file");
            
             empty=true;
         }
         classList.add("adaptive-module_image");
         ResourceResolver requestResolver = getRequest().getResourceResolver();

         resource = requestResolver.getResource(largeFileReference);
         language = getCurrentPage().getLanguage(true);
		
	}

    public String getSrc(String rendition){
        if(resource==null || empty)
            return PLACEHOLDER_IMG;
        return rendition;
    }


    public String getSrc_large () {
        return ImageUtil.getSrc(largeFileReference, ImageUtil.RENDITION_LARGE);
    }


    public String getSrc_medium () {
        return ImageUtil.getSrc(mediumFileReference, ImageUtil.RENDITION_MEDIUM);
    }

    public String getSrc_small () {
        return ImageUtil.getSrc(smallFileReference, ImageUtil.RENDITION_SMALL);
    }


    public String getClassList() {
        return StringUtils.join(classList, " ");
    }

    public String getAlternateText() {
        return ImageUtil.getAlternateText(resource, language);
    }

}