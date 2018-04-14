package com.statoil.reinvent.utils;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringUtils;

public class PageUtil {

    public final static String PANEL_TEMPLATE_PATH ="/apps/statoil/templates/panel";
    public final static String SEARCH_TEMPLATE_PATH ="/apps/statoil/templates/search";
    public final static String LANGUAGES_TEMPLATE_PATH ="/apps/statoil/templates/languages";
    public final static String TOOLS_TEMPLATE_PATH = "/apps/statoil/templates/tools";
    public final static String TOPIC_TEMPLATE_PATH ="/apps/statoil/templates/topic";
    public final static int SECTION_LEVEL = 3;

    public static boolean isPanelPage(Page page){
        return isPageBasedOnTemplate(page, PANEL_TEMPLATE_PATH);
    }

    public static boolean isSearchPage(Page page) {
        return isPageBasedOnTemplate(page, SEARCH_TEMPLATE_PATH);
    }

    public static boolean isLanguagesPage(Page page) {
        return isPageBasedOnTemplate(page, LANGUAGES_TEMPLATE_PATH);
    }

    public static boolean isToolsPage(Page page) {
        if (page == null) {
            return false;
        }

        Page parentSectionPage = page.getAbsoluteParent(SECTION_LEVEL);

        if (parentSectionPage == null) {
            return isPageBasedOnTemplate(page, TOOLS_TEMPLATE_PATH);
        } else {
            return isPageBasedOnTemplate(parentSectionPage, TOOLS_TEMPLATE_PATH);
        }
    }

    public static boolean isTopicPage(Page page) {
        return isPageBasedOnTemplate(page, TOPIC_TEMPLATE_PATH);
    }

    private static String getTemplate(Page page) {
        // Read rights to /apps was removed for the anonymous user in CQ 5.5, which results in page.getTemplate
        // returning null. So we retrieve the template without using the method.
        return page.getProperties().get("cq:template", String.class);
    }

    private static boolean isPageBasedOnTemplate(Page page, String templatePath) {
        if (page == null) {
            return false;
        }

        String template = getTemplate(page);

        if (StringUtils.isBlank(template)) {
            return false;
        }

        return template.equals(templatePath);
    }
}
