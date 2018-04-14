package com.statoil.reinvent.components.menu;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.utils.ImageUtil;
import com.statoil.reinvent.utils.PageUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;

/**
 * A wrapper for page to set up a navigation structure.
 */
public class NavigationItem {

    private static final Logger logger = LoggerFactory.getLogger(NavigationItem.class);

    private static final String THUMBNAIL_EXT = "/_jcr_content/renditions/cq5dam.thumbnail.140.100.png";

    private final Page page;

    private final List<NavigationItem> listChildren;

    private final String iconPath;

    private Locale locale;

    private boolean hideInNav;

    public NavigationItem(Page page, ResourceResolver resourceResolver, Locale locale) {
        this.page = page;
        this.locale = locale;
        this.listChildren = findChildren(page, resourceResolver);
        this.iconPath = page.getProperties().get("iconPath", String.class);
        this.hideInNav = page.isHideInNav();
    }

    private List<NavigationItem> findChildren(Page page, ResourceResolver resourceResolver) {
        List<NavigationItem> children = new ArrayList<>();
        Iterator<Page> pageIterator = page.listChildren();
        while (pageIterator.hasNext()) {
            Page nextPage = pageIterator.next();
            if (!nextPage.isHideInNav()) {
                children.add(new NavigationItem(nextPage, resourceResolver, locale));
            }
        }
        return children;
    }

    private String findBannerImage(Page page, ResourceResolver resourceResolver) {
        try {
            Node pageContentNode = page.getContentResource().adaptTo(Node.class);
            Node bannerNode = pageContentNode.getNode("topbanner/banner");
            String path = bannerNode.getProperty("fileReference").getString();
            Resource resource = resourceResolver.getResource(path);
            return ImageUtil.getSrc(resource, ImageUtil.RENDITION_EXTRA_SMALL);
        } catch (RepositoryException | NullPointerException e) {
            logger.debug("Could not find image", e);
        }
        return null;
    }

    public boolean hasTag(String tagId) {
        if (page != null) {
            for (Tag tag : page.getTags()) {
                if (tag.getTagID().equals(tagId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isToolsPage() {
        return PageUtil.isToolsPage(page);
    }

    public List<NavigationItem> listChildren() {
        return listChildren;
    }

    public boolean isHasChildren() {
        return !listChildren.isEmpty();
    }

    public boolean isHideInNav() {
        return hideInNav;
    }

    public String getPath() {
        return page.getPath();
    }

    public String getHideInNav() {
        return Boolean.toString(page.isHideInNav());
    }

    public String getTitle() {
        if (page.getNavigationTitle() != null)
            return page.getNavigationTitle();
        return page.getTitle();
    }

    public String getIconPath() {
        return iconPath;
    }

}
