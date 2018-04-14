package com.statoil.reinvent.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import com.statoil.reinvent.services.UserMapperService;

@Component
@Service(value = UserMapperService.class)
public class UserMapperServiceImpl implements UserMapperService {

	@Reference
	ResourceResolverFactory resolverFactory;

	@Override
	public ResourceResolver getJCRWriterService() throws LoginException {
		Map<String, Object> params = new HashMap<>();
		params.put(ResourceResolverFactory.SUBSERVICE, "writeUser");
		ResourceResolver writeService = resolverFactory.getServiceResourceResolver(params);
		return writeService;
	}

	@Override
	public ResourceResolver getJCRReadService() throws LoginException {
		Map<String, Object> params = new HashMap<>();
		params.put(ResourceResolverFactory.SUBSERVICE, "readUser");
		ResourceResolver readService = resolverFactory.getServiceResourceResolver(params);
		return readService;
	}

}