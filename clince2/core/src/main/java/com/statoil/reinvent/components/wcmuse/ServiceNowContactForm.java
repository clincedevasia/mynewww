package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

public class ServiceNowContactForm extends BaseComponent {

	private Locale language;

	@Override
	protected void activated() throws Exception {
		language = getCurrentPage().getLanguage(true);
	}

	public String getLanguage() {
		return language.getLanguage();
	}

}
