package com.statoil.reinvent.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

import com.statoil.reinvent.services.SitemapConfiguration;
import com.statoil.reinvent.services.SitemapFactory;

/**
 * This Factory implementation is used to get the list of configured sitemap in OSGi and convert
 * them to map. So by passing the DNS to the sitemapFacorty service we can retrieve the list of language paths
 * that are configured.
 * 
 * @author kavarana
 *
 */
@Component
@Service
public class SitemapFactoryImpl implements SitemapFactory {
	
	@Reference(bind = "bind", unbind = "unbind", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private SitemapConfiguration sitemapConfiguration; 

	
	Map<String, List<String>> sitemapConfigurationList;
	
	protected void bind(SitemapConfiguration sitemapConfiguration) {
		if (sitemapConfigurationList == null) {
			sitemapConfigurationList = new HashMap<>();
		}
		sitemapConfigurationList.put(sitemapConfiguration.getServerName(), sitemapConfiguration.getLanguagePaths());
	}

	protected void unbind(SitemapConfiguration sitemapConfiguration) {
		sitemapConfigurationList.remove(sitemapConfiguration);
	}

	@Override
	public List<String> getSitemapConfigurations(String serverName) {	
		return sitemapConfigurationList.get(serverName);
	}

}
