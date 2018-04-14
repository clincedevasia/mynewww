package apps.statoil.components.content.bannerinline;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.WCMMode;
import com.statoil.reinvent.utils.ImageUtil;

public class Image extends WCMUsePojo {


    private String link;
    private String title;
    private String type;
    private String shadow;
    private String angle;
    private String anglecolor;
    private String color;
    private String space;
    private String alignContent;

    private Resource resource;
    private ArrayList<String> classList = new ArrayList<String>();
    private String language;
    private Boolean useButton=false;

    public void activate() throws Exception {

        boolean isEdit = WCMMode.fromRequest(getRequest()) == WCMMode.EDIT;
        boolean isDesign = WCMMode.fromRequest(getRequest()) == WCMMode.DESIGN;
        type =   getProperties().get("type", String.class);
        title = getProperties().get("jcr:title", String.class);
        link = getProperties().get("fileReference", String.class);
        shadow = getProperties().get("shadow", String.class);
        angle= getProperties().get("angle", String.class);
        anglecolor= getProperties().get("anglecolor", String.class);
        color = getProperties().get("color", String.class);
        useButton = getProperties().get("buttonInside", Boolean.class);
        space = getProperties().get("space", String.class);
        alignContent = getProperties().get("alignContent", String.class);

        if((link==null || link.equals("")) && (isEdit|| isDesign)){
            classList.add("cq-dd-image");
            classList.add("cq-placeholder");
            classList.add("file");
        }
        classList.add("figure-module_image");
        ResourceResolver requestResolver = getRequest().getResourceResolver();
        resource = requestResolver.getResource(link);
        language = getCurrentPage().getLanguage(true).getLanguage();
    }

    public String getSpace(){
        return space;
    }
    public String getAlignContent() {

      if (StringUtils.isBlank(alignContent)) {
          return "align-center"; // Default.
      }

      return alignContent; };

    public boolean getUseButton(){
        return useButton!=null && useButton.booleanValue();
    }

    public String getUseButtonClass(){
        return getUseButton() ? " button-inside" : " text-inside";
    }

    public String getShadow(){
        return shadow;
    }

    private boolean bottomAngle(){
        return "both".equals(angle) || "bottom".equals(angle);
    }
    private boolean topAngle(){
        return "both".equals(angle) || "top".equals(angle);
    }

    public String getAngle() {
        String angleClass = "";
        if (bottomAngle()) {
            angleClass += " " + getBottomAngleClass();
        }
        if (topAngle()) {
            angleClass += " " + getTopAngleClass();
        }
        return angleClass;
    }

    private String getTopAngleClass() {
        if ("both".equals(anglecolor) || "top".equals(anglecolor)) {
            return "overlay-top-alternate";
        }
        return "overlay-top";
    }

    private String getBottomAngleClass() {
        if ("both".equals(anglecolor) || "bottom".equals(anglecolor)) {
            return "overlay-bottom-alternate";
        }
        return "overlay-bottom";
    }


    public String getColor(){
        return color;
    }

    public String getType(){
        return type;
    }
    public String getLink() {
        return link;
    }

    public String getTitle(){
        return title;
    }

    public String getSrc_large () {
        return ImageUtil.getSrc(resource, ImageUtil.RENDITION_LARGE);
    }

    public String getSrc_extralarge (){
        return ImageUtil.getSrc(resource, ImageUtil.RENDITION_EXTRA_LARGE);
    }

    public String getSrc_medium () {
        return ImageUtil.getSrc(resource, ImageUtil.RENDITION_MEDIUM);
    }

    public String getSrc_small () {
        return ImageUtil.getSrc(resource, ImageUtil.RENDITION_SMALL);
    }

    public String getSrc_extrasmall () {
        return ImageUtil.getSrc(resource, ImageUtil.RENDITION_EXTRA_SMALL);
    }

    public String getLanguage() {
        return language;
    }

    public String getClassList() {
        return StringUtils.join(classList, " ");
    }

    public String getAlternateText() {
        return ImageUtil.getAlternateText(resource, language);
    }

}
