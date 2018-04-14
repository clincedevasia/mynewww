package com.statoil.reinvent.services.impl;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

import com.statoil.reinvent.services.ServicenowEndPointConfiguration;

@Component(label = "Servicenow endpoing configuration", immediate = true, enabled = true, metatype = true, description = "This configuration is responsible conneting to servicenow environments based on AEM run modes")
@Service
@Properties({
		@Property(name = "servicenow.environment.url", label = "Servicenow environment URL", description = "Provide servicenow environment URL. For Example: 'https://statoiltest.service-now.com' in case of test environment"),
		@Property(name = "servicenow.credentials", label = "Servicenow Credentials", description = "Provice servicenow credentials"),
		@Property(name = "servicenow.catalog.item", label = "Servicenow Catalog Item", description = "Provide catalog item"), })
public class ServicenowEndpointConfigurationImpl implements ServicenowEndPointConfiguration {

	private String environmentUrl;
	private String credentials;
	private String catalogItem;
	
	@Override
	public String getEnvironmentUrl() {
		return environmentUrl;
	}

	@Override
	public String getCredentials() {
		return credentials;
	}

	@Override
	public String getCatalogItem() {
		return catalogItem;
	}
	
	@Activate
	protected void activate(ComponentContext ctx) {
		final Dictionary<?, ?> config = ctx.getProperties();
		environmentUrl = PropertiesUtil.toString(config.get("servicenow.environment.url"), "https://statoiltest.service-now.com");
		credentials = PropertiesUtil.toString(config.get("servicenow.credentials"), "U3RhdG9pbERvdENvbTp4TkVeZ1E0VQ==");
		catalogItem = PropertiesUtil.toString(config.get("servicenow.catalog.item"), "e8139ed7db2a6200aa5ff1eabf961987");
	}

	
}
