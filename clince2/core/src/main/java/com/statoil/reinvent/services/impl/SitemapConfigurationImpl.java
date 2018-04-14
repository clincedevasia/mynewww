package com.statoil.reinvent.services.impl;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

import com.statoil.reinvent.services.SitemapConfiguration;

/**
 * This implementation class provides OSGi configuration which is of factory type.
 * So this can be used to configure multiple applications. For statoil this is used 
 * to provide different sitemap paths for different DNS names. For example www.statoil.com has
 * two language paths. So the response of /sitemap.xml request will be retrieved with configured paths.
 * 
 * @author kavarana
 *
 */
@Component(label = "Sitemap Factroy Configuration", immediate = true, enabled = true, metatype = true, description = "This is factory configuration which acts as a interface to generate sitemap", configurationFactory = true)
@Service
@Properties({
		@Property(name = "sitemap.server.name", label = "Server Name", description = "Provide DNS name of the server. For example www.statoil.com"),
		@Property(name = "sitemap.language.paths", label = "Languages paths", description = "Provide languages paths under that server name. For example /content/statoil/en", cardinality= 5) 
		})
public class SitemapConfigurationImpl implements SitemapConfiguration {
	
	private String serverName;
	private List<String> languagesPaths;

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public List<String> getLanguagePaths() {
		return languagesPaths;
	}
	
	@Activate
	protected void activate(ComponentContext ctx) {
		final Dictionary<?, ?> config = ctx.getProperties();
		serverName = PropertiesUtil.toString(config.get("sitemap.server.name"), null);
		languagesPaths = Arrays.asList(PropertiesUtil.toStringArray(config.get("sitemap.language.paths")));
	}
	
}
