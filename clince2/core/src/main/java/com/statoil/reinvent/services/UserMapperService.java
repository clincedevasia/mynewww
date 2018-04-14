package com.statoil.reinvent.services;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;

public interface UserMapperService {
	ResourceResolver getJCRWriterService() throws LoginException;

	ResourceResolver getJCRReadService() throws LoginException;
}