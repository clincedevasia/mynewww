package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

public class ServiceNowProposeCandidates extends BaseComponent {

    private Locale language;

    public String getLanguage() {
        return language.getLanguage();
    }

	@Override
	protected void activated() throws Exception {
		 this.language = currentPage.getLanguage(true);
	}
}
