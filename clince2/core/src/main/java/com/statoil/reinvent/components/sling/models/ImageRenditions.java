package com.statoil.reinvent.components.sling.models;

import org.apache.sling.api.resource.Resource;

import com.statoil.reinvent.utils.ImageUtil;

public class ImageRenditions {
	private String extraSmallImage;
	private String smallImage;
	private String medium;
	private String large;
	private String extraLarge;
		
	public ImageRenditions(Resource resource) {
		this.extraSmallImage = ImageUtil.getSrc(resource,  ImageUtil.RENDITION_EXTRA_SMALL);
		this.smallImage = ImageUtil.getSrc(resource,  ImageUtil.RENDITION_SMALL);
		this.medium = ImageUtil.getSrc(resource,  ImageUtil.RENDITION_MEDIUM);
		this.large = ImageUtil.getSrc(resource,  ImageUtil.RENDITION_LARGE);
		this.extraLarge = ImageUtil.getSrc(resource,  ImageUtil.RENDITION_EXTRA_LARGE);
	}
	
	public String getExtraSmallImage() {
		return extraSmallImage;
	}
	public String getSmallImage() {
		return smallImage;
	}
	public String getMedium() {
		return medium;
	}
	public String getLarge() {
		return large;
	}
	public String getExtraLarge() {
		return extraLarge;
	}
	
	
}
