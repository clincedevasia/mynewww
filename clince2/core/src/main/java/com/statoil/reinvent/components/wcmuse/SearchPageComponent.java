package com.statoil.reinvent.components.wcmuse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchPageComponent extends BaseComponent {

	private final Logger LOG = LoggerFactory.getLogger(SearchPageComponent.class);
	private static final String LANGUAGE = "language";
	private String language;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside search page component");
		language = request.getRequestParameter(LANGUAGE).getString();
	}

	public String getLanguage() {
		return language;
	}

}