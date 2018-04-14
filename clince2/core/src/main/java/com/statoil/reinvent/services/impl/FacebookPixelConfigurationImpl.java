package com.statoil.reinvent.services.impl;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

import com.statoil.reinvent.services.FacebookPixelConfiguration;

@Component(label = "Facebook pixel Configuration", immediate = true, enabled = true, metatype = true, description = "This configuration is responsible for server side validation of reCaptcha")
@Service
@Properties({
		@Property(name = "pixel.id", label = "Facebook Pixel Id", description = "Provide Facebook pixel ID") })
public class FacebookPixelConfigurationImpl implements FacebookPixelConfiguration {

	private String pixelId;
	
	@Override
	public String getPixelId() {
		return pixelId;
	}
	
	@Activate
	protected void activate(ComponentContext ctx) {
		final Dictionary<?, ?> config = ctx.getProperties();
		pixelId = PropertiesUtil.toString(config.get("pixel.id"), "978060852331781");
	}

}
