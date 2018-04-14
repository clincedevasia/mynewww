package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.statoil.reinvent.components.menu.NavigationItem;
import com.statoil.reinvent.components.wcmuse.BaseComponent;
import com.statoil.reinvent.utils.PageUtil;

public class TopNav extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(TopNav.class);
	private List<NavigationItem> items = new ArrayList<>();
	private String home;
	private Locale language;

	private Boolean disableSearch;
	private String searchUrl;
	private Boolean disableLanguage;
	private String languageUrl;

	private static final String TEMPLATE_PROPERTY = "cq:template";
	private static final String TEMPLATE_SEARCH = "/apps/statoil/templates/search";
	private static final String TEMPLATE_LANGUAGES = "/apps/statoil/templates/languages";

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inisde topnav component wcmuse class");
		Page rootPage = getRootPage();
		items = buildNavigationItems(rootPage);
		home = rootPage.getPath() + ".html";

		// The search page / languages page is located at level 2, besides the
		// front page(s), so it can't use the same logic as all the
		// other pages, for determining whether to display search and/or
		// language switch button(s).
		Page currentPage = getCurrentPage();

		if (PageUtil.isSearchPage(currentPage)) {
			Page frontPage = getFrontPage("search");
			setFeaturesFrom(frontPage);

			language = frontPage.getLanguage(true);
			languageUrl = "/content/statoil/languages.html?language=" + getLanguage();
			searchUrl = getRequest().getHeader("referer");
		} else if (PageUtil.isLanguagesPage(currentPage)) {
			Page frontPage = getFrontPage("languages");
			setFeaturesFrom(frontPage);

			language = frontPage.getLanguage(true);
			languageUrl = getRequest().getHeader("referer");
			searchUrl = "/content/statoil/search.html?language=" + getLanguage();
		} else {
			Page frontPage = currentPage.getAbsoluteParent(2);
			setFeaturesFrom(frontPage);

			language = currentPage.getLanguage(true);
			languageUrl = "/content/statoil/languages.html?language=" + getLanguage();
			searchUrl = "/content/statoil/search.html?language=" + getLanguage();
		}
	}

	public List<NavigationItem> getRootPageChildren() {
		return items;
	}

	public String getHome() {
		return home;
	}

	public String getLanguage() {
		return language.getLanguage();
	}

	/**
	 * Returns a boolean, indicating whether any section page(s) is visible (not
	 * hidden in navigation).
	 *
	 * @return Returns a boolean, indicating whether any section page(s) is
	 *         visible (not hidden in navigation).
	 */
	public boolean getVisibleItems() {
		boolean hasVisibleItems = false;

		List<NavigationItem> items = getRootPageChildren();

		for (NavigationItem item : items) {
			if (!item.isHideInNav()) {
				hasVisibleItems = true;
				break;
			}
		}

		return hasVisibleItems;
	}

	/**
	 * Returns a boolean, indicating whether the search is disabled or not.
	 * <p>
	 * The value is specified in the front page's page properties, in the
	 * Statoil section.
	 *
	 * @return A boolean, indicating whether the search is disabled or not.
	 */
	public boolean getDisableSearch() {
		return disableSearch != null && disableSearch.booleanValue();
	}

	/**
	 * Returns a boolean, indicating whether the language switch is disabled or
	 * not.
	 * <p>
	 * The value is specified in the front page's page properties, in the
	 * Statoil section.
	 *
	 * @return A boolean, indicating whether the language switch is disabled or
	 *         not.
	 */
	public boolean getDisableLanguage() {
		return disableLanguage != null && disableLanguage.booleanValue();
	}

	/**
	 * Returns a string (URL), which is the destination of the search or the
	 * referring URL when using non-desktop.
	 * <p>
	 * When using non-desktop it is possible to return to the referring URL.
	 *
	 * @return A string (URL), which is the destination of the search or the
	 *         referring URL when using non-desktop.
	 */
	public String getSearchUrl() {
		return searchUrl;
	}

	/**
	 * Returns a string (URL), which is the destination of the language switch
	 * or the referring URL when using non-desktop.
	 * <p>
	 * When using non-desktop it is possible to return to the referring URL.
	 *
	 * @return A string (URL), which is the destination of the language switch
	 *         or the referring URL when using non-desktop.
	 */
	public String getLanguageUrl() {
		return languageUrl;
	}

	/**
	 * Returns the root page for the current page.
	 *
	 * The language and search pages are generic and are shared across all
	 * languages of a given site, i.e. the English and Norwegian pages in the
	 * global site uses the same language and search pages. As the pages are
	 * generic, the language parameter is used (only on mobile) to determine the
	 * root page to be used for generic the navigation structure.
	 */
	private Page getRootPage() {
		if (!getCurrentPage().getProperties().containsKey(TEMPLATE_PROPERTY)) {
			return getCurrentPage().getAbsoluteParent(2);
		}

		String templateProperty = getCurrentPage().getProperties().get(TEMPLATE_PROPERTY, String.class);

		if (templateProperty.equals(TEMPLATE_SEARCH)) {
			return getPageManager().getPage(getCurrentPage().getPath().replace("search",
					getRequest().getRequestParameter("language").getString()));
		} else if (templateProperty.equals(TEMPLATE_LANGUAGES)) {
			return getPageManager().getPage(getCurrentPage().getPath().replace("languages",
					getRequest().getRequestParameter("language").getString()));
		} else {
			return getCurrentPage().getAbsoluteParent(2);
		}
	}

	private void setFeaturesFrom(Page page) {
		ValueMap values = page.getContentResource().getValueMap();
		disableSearch = values.get("disableSearch", Boolean.class);
		disableLanguage = values.get("disableLanguage", Boolean.class);
	}

	private List<NavigationItem> buildNavigationItems(Page page) {
		List<NavigationItem> navigationItems = new ArrayList<>();

		if (page != null) {
			Iterator<Page> childPages = page.listChildren();

			while (childPages.hasNext()) {
				Page childPage = childPages.next();
				if (childPage != null && !childPage.isHideInNav()) {
					navigationItems.add(new NavigationItem(childPage, getResourceResolver(), language));
				}
			}
		}

		return navigationItems;
	}

	private Page getFrontPage(String pageName) {
		String frontPagePath = getCurrentPage().getPath().replace(pageName,
				getRequest().getRequestParameter("language").getString());
		PageManager pageManager = getResourceResolver().adaptTo(PageManager.class);
		Page frontPage = pageManager.getPage(frontPagePath);
		return frontPage;
	}

}
