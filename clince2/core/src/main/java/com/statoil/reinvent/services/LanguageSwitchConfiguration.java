package com.statoil.reinvent.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Statoil language switch configuration", description = "This configuration represents the list of sites that are available in language switch dropdown")
public @interface LanguageSwitchConfiguration {

	@AttributeDefinition(name = "Predefined Sites", description = "Specify all the sites that should be available in language switch in this format: SiteName;URL;contentPath")
	String[] getPredefinedSites() default {}; 

}