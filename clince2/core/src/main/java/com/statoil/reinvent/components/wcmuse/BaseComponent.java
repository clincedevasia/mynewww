package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;

/**
 * Base component is an abstract class that all the component classes has to extend so that all
 * basic functionality will be included
 * 
 * @author Kalyan
 */
public abstract class BaseComponent extends WCMUsePojo {

	private static final Logger LOG = LoggerFactory.getLogger(BaseComponent.class);
	
	protected Page currentPage;
	protected ValueMap properties;
	protected ValueMap pageproperties;
	protected SlingScriptHelper slingScriptHelper;
	protected ResourceResolver resourceResolver;
	protected SlingHttpServletRequest request;
	protected SlingHttpServletResponse response;
	protected Resource resource;
	
	protected abstract void activated() throws Exception;
	
	private Locale locale;
	private String componentNodeName;
	private String componentNodePath;
	
	@Override
	public void activate() throws Exception {
		LOG.debug("inside abstract base component");
		currentPage = getCurrentPage();
		properties = getProperties();
		pageproperties = getPageProperties();
		slingScriptHelper = getSlingScriptHelper();
		resourceResolver = getResourceResolver();
		request = getRequest();
		response = getResponse();
		resource = getResource();
		locale = currentPage.getLanguage(true);
		componentNodeName = resource.getName();
		componentNodePath =  resource.getPath();
		this.activated();

	}

	public Locale getLocale() {
		return locale;
	}

	public String getComponentNodeName() {
		return componentNodeName;
	}

	public String getComponentNodePath() {
		return componentNodePath;
	}

}