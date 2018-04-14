package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeForm extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(SubscribeForm.class);
    private String space;
    private Locale language;


    @Override
	protected void activated() throws Exception {
    	LOG.debug("Inside subscribeform component wcmuse class");
		language = currentPage.getLanguage(true);
        space = properties.get("space", String.class);		
	}
    
    public String getLanguage() {
        return language.getLanguage();
    }

    public String getSpace(){
      return space;
    }

}
