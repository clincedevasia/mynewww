package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

import com.statoil.reinvent.editor.fields.CheckBox;
import com.statoil.reinvent.services.FacebookPixelConfiguration;
import com.statoil.reinvent.utils.PageUtil;

public class PageComponentPageProperties extends BaseComponent {

	private Locale language;
	private boolean toolsPage;
	private boolean searchPage;
	private String facebookPixelId;
	private boolean useCdn;
	private String cdnUrl;

	@Override
	protected void activated() throws Exception {
		language = currentPage.getLanguage(true);
		toolsPage = PageUtil.isToolsPage(currentPage);
		searchPage = PageUtil.isSearchPage(currentPage);
		facebookPixelId = slingScriptHelper.getService(FacebookPixelConfiguration.class).getPixelId();
		useCdn = CheckBox.getValue(properties, "usecdn");
        cdnUrl = properties.get("cdnurl", String.class);
	}

	public String getLanguage() {
		return language.getLanguage();
	}

	public boolean isToolsPage() {
		return toolsPage;
	}

	public boolean isSearchPage() {
		return searchPage;
	}

	public int getPageDepth() {
		return getCurrentPage().getDepth();
	}

	public String getFacebookPixelId() {
		return facebookPixelId;
	}
	
	public boolean getUseCdn() {
        return useCdn;
    }

    public String getCdnUrl(){
        return cdnUrl;
    }


}
