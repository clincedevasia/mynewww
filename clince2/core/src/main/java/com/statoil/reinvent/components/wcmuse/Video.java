package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.WCMMode;
import com.statoil.reinvent.components.wcmuse.BaseComponent;
import com.statoil.reinvent.utils.ImageUtil;

public class Video extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(Video.class);
	private String link = "";
	private String title;
	private String posterURL = "";
	private String space;

	private ArrayList<String> classList = new ArrayList<String>();

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside video component wcmuse class");
		boolean isEdit = WCMMode.fromRequest(getRequest()) == WCMMode.EDIT;
		boolean isDesign = WCMMode.fromRequest(getRequest()) == WCMMode.DESIGN;

		title = getProperties().get("jcr:title", String.class);
		link = getProperties().get("linkURL", String.class);
		posterURL = getProperties().get("posterURL", String.class);
		space = getProperties().get("space", String.class);

		classList.add("video");

		if (link == null && isEdit || isDesign) {
			classList.add("cq-placeholder");
		}
	}

	public String getPosterURL() {
		return ImageUtil.getSrc(posterURL, ImageUtil.RENDITION_LARGE);
	}

	public String getLink() {
		return link;
	}

	public String getSpace() {
		return space;
	}

	public String getTitle() {
		return title;
	}

	public String getClassList() {
		return StringUtils.join(classList, " ");
	}

}
