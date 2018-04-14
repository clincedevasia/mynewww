package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

public class ServiceNowSupplyChain extends BaseComponent {

    private Locale language;

    @Override
	protected void activated() throws Exception {
		  this.language = currentPage.getLanguage(true);		
	}
    
    public String getLanguage() {
        return language.getLanguage();
    }

}
