package com.statoil.reinvent.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.statoil.reinvent.models.LanguageSwitchSitesModel;
import com.statoil.reinvent.services.LanguageSwitchConfiguration;

@Component(service = LanguageSwitchPredefinedSites.class, immediate = true)
@Designate(ocd = LanguageSwitchConfiguration.class)
public class LanguageSwitchPredefinedSites {

	private List<LanguageSwitchSitesModel> preDefinedSites;

	@Activate
	@Modified
	protected void activate(final LanguageSwitchConfiguration languageSwitchConfiguration) {
		List<String> preDefinedSitesAsString = Arrays.asList(languageSwitchConfiguration.getPredefinedSites());
		preDefinedSites = new ArrayList<>();
		preDefinedSitesAsString.stream().forEach(e -> {
			String[] sitesData = e.split(";");
			preDefinedSites.add(new LanguageSwitchSitesModel(sitesData[0], sitesData[1], sitesData[2]));
		});
	}

	public List<LanguageSwitchSitesModel> getPreDefinedSites() {
		return preDefinedSites;
	}

}