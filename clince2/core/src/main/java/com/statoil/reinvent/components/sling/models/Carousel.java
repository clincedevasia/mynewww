package com.statoil.reinvent.components.sling.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import com.google.gson.Gson;
import com.statoil.reinvent.models.CarouselProperties;

@Model(adaptables = Resource.class)
public class Carousel {

	private List<CarouselProperties> slideProperties = new ArrayList<>();
	private List<String> slidesList;

	@Inject
	protected ResourceResolver resourceResolver;

	@Inject
	@Optional
	private String[] slides;

	@Inject
	@Optional
	private String spaceBelow;

	public String getSpaceBelow() {
		return spaceBelow;
	}

	public List<CarouselProperties> getSlideProperties() {
		return slideProperties;
	}

	@PostConstruct
	protected void init() {
		if (ArrayUtils.isNotEmpty(slides)) {
			slidesList = Arrays.asList(slides);
			slidesList.forEach(e -> slideProperties.add(new Gson().fromJson(e, CarouselProperties.class)));
			slideProperties.forEach(
					e -> e.setImageRenditions(new ImageRenditions(resourceResolver.getResource(e.getImageLink()))));
		}
	}

	public boolean isConfigured() {
		return slidesList != null;
	}

}
