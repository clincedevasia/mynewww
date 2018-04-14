package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.WCMMode;
import com.statoil.reinvent.utils.ImageUtil;

public class Image extends BaseComponent {

	private String link;
	private String title;
	private String type;
	private String space;
	private Locale language;

	private Resource resource;
	private List<String> classList = new ArrayList<String>();

	@Override
	protected void activated() throws Exception {
		boolean isEdit = WCMMode.fromRequest(request) == WCMMode.EDIT;
		boolean isDesign = WCMMode.fromRequest(request) == WCMMode.DESIGN;
		type = properties.get("type", String.class);
		title = properties.get("jcr:title", String.class);
		link = properties.get("fileReference", String.class);
		space = properties.get("space", String.class);
		if ((link == null || link.equals("")) && (isEdit || isDesign)) {
			classList.add("cq-dd-image");
			classList.add("cq-placeholder");
			classList.add("file");
		}

		classList.add("figure-module_image");
		language = currentPage.getLanguage(true);
		resource = resourceResolver.getResource(link);
	}

	public String getSpace() {
		return space;
	}

	public String getType() {
		return type;
	}

	public String getLink() {
		return link;
	}

	public String getTitle() {
		return title;
	}

	public String getSrc_large() {
		return ImageUtil.getSrc(resource, ImageUtil.RENDITION_LARGE);
	}

	public String getSrc_extralarge() {
		return ImageUtil.getSrc(resource, ImageUtil.RENDITION_EXTRA_LARGE);
	}

	public String getSrc_medium() {
		return ImageUtil.getSrc(resource, ImageUtil.RENDITION_MEDIUM);
	}

	public String getSrc_small() {
		return ImageUtil.getSrc(resource, ImageUtil.RENDITION_SMALL);
	}

	public String getSrc_extrasmall() {
		return ImageUtil.getSrc(resource, ImageUtil.RENDITION_EXTRA_SMALL);
	}

	public List<String> getClassList() {
		return classList;
	}

	public String getAlternateText() {
		return ImageUtil.getAlternateText(resource, language);
	}

}
