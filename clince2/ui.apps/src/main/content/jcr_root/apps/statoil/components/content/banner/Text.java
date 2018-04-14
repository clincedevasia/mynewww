package apps.statoil.components.content.banner;

import com.adobe.cq.sightly.WCMUsePojo;
import com.statoil.reinvent.utils.PropertiesUtil;

public class Text extends WCMUsePojo {

    private String title;
    private String subtitle;
    private Boolean disableText;
    private String language;

    private String siteTitle;

    public void activate() throws Exception {
        title = PropertiesUtil.getFirstUnemptyString(getCurrentPage().getTitle(), getCurrentPage().getNavigationTitle(), getCurrentPage().getPageTitle());
        subtitle = PropertiesUtil.getFirstUnemptyString(getCurrentPage().getProperties().get("subtitle", String.class));
        disableText = getProperties().get("disableText", Boolean.class);
        language = getCurrentPage().getLanguage(true).toString();

        siteTitle = PropertiesUtil.getFirstUnemptyString(getCurrentPage().getNavigationTitle(), getCurrentPage().getTitle());
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean getShowBelow(){
        return true;
    }

    public Boolean getDisableText() {
        return disableText;
    }

    public String getLanguage() {
        return language;
    }


    public String getSiteTitle() {
        return siteTitle;
    }

}
