package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceNowCareerFair extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceNowCareerFair.class);
    private Locale language;

    @Override
	protected void activated() throws Exception {
    	LOG.debug("Inside servicenowcareerfair component wcmuse class");
		this.language = getCurrentPage().getLanguage(true);
	}
    
    public String getLanguage() {
        return language.getLanguage();
    }

}
