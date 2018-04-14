package com.statoil.reinvent.workflow.mailinglist.impl;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

import com.statoil.reinvent.models.BrandMasterConfigurationModel;
import com.statoil.reinvent.workflow.mailinglist.BrandMasterConfiguration;

@Component(label = "Brand Master Configuration", immediate = true, enabled = true, metatype = true, description = "This is used to configure Brand Master Mailing list SOAP configurations")
@Service
@Properties({
	@Property(name = "brand.master.client.secret", label = "Client Secret", description = "Provide brand master client secret"),
	@Property(name = "brand.master.password", label = "Password", description = "Provide brand master client password"),
	@Property(name = "brand.master.apn.id", label = "APN ID", description = "Provide brand master client apn id"), 
	@Property(name = "brand.master.oty.id", label = "OTY ID", description = "Provide brand master client oty id"),
	@Property(name = "brand.master.ptl.id", label = "PTL ID", description = "Provide brand master client ptl id"),
	@Property(name = "brand.master.authentication.url", label = "Authentication URL", description = "Provide brand master client authentication URL"),
	@Property(name = "brand.master.subscription.url", label = "Subscription URL", description = "Provide brand master client subscription URL"),})
public class BrandMasterConfigurationImpl implements BrandMasterConfiguration {

	private BrandMasterConfigurationModel brandMasterConfigurationModel; 
	
	@Override
	public BrandMasterConfigurationModel getBrandMasterConfigurations() {
		return brandMasterConfigurationModel;
	}
	
	@Activate
	protected void activate(ComponentContext ctx) {
		final Dictionary<?, ?> config = ctx.getProperties();
		brandMasterConfigurationModel = new BrandMasterConfigurationModel();
		brandMasterConfigurationModel.setClientSecret(PropertiesUtil.toString(config.get("brand.master.client.secret"), "436A925B124666CCE053F210630A046C"));
		brandMasterConfigurationModel.setPassword(PropertiesUtil.toString(config.get("brand.master.password"), "df823HISD!io8Hnsdf"));
		brandMasterConfigurationModel.setApnId(PropertiesUtil.toString(config.get("brand.master.apn.id"), "32"));
		brandMasterConfigurationModel.setOtyId(PropertiesUtil.toString(config.get("brand.master.oty.id"), "114961"));
		brandMasterConfigurationModel.setPtlId(PropertiesUtil.toString(config.get("brand.master.ptl.id"), "14414"));
		brandMasterConfigurationModel.setAuthenticationUrl(PropertiesUtil.toString(config.get("brand.master.authentication.url"), "https://develop.brandmaster.com/apigateway/apigateway.dll/SOAP_UNENCRYPTED?service=Authentication"));
		brandMasterConfigurationModel.setSubscriptionUrl(PropertiesUtil.toString(config.get("brand.master.subscription.url"), "https://develop.brandmaster.com/apigateway/apigateway.dll/SOAP_UNENCRYPTED?service=Subscription"));
	}

}
