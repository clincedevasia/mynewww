package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.statoil.reinvent.utils.PageUtil;

public class Button extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(Button.class);
	private String alignment;
	private String anchor;
	private String colors;
	private boolean external;
	private String link;
	private String margin;
	private String title;

	private ArrayList<String> classList = new ArrayList<String>();

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside button component wcmuse class");
		WCMMode wcmMode = WCMMode.fromRequest(request);
		boolean isEdit = wcmMode == WCMMode.EDIT;
		boolean isDesign = wcmMode == WCMMode.DESIGN;

		alignment = properties.get("align", String.class);
		anchor = properties.get("anchor", String.class);
		colors = properties.get("colors", String.class);
		external = "yes".equals(properties.get("external", String.class));
		link = properties.get("linkURL", String.class);
		margin = properties.get("space", String.class);
		title = properties.get("jcr:title", String.class);

		classList.add("btn");

		String size = properties.get("size", String.class);
		classList.add(size);

		if ((StringUtils.isBlank(link) || StringUtils.isBlank(title)) && (isEdit || isDesign)) {
			classList.add("cq-placeholder");
		}
		if (!StringUtils.isBlank(link)) {
			classList.add("default");
		}
		Page page = getPageManager().getPage(link);
		if (PageUtil.isPanelPage(page)) {
			classList.add("panel");
		}
		if (StringUtils.isNotBlank(colors)) {
			classList.add(colors);
		}
	}

	public String getAlignment() {
		if (StringUtils.isBlank(alignment)) {
			return StringUtils.EMPTY;
		}

		return alignment;
	}

	public String getAnchor() {
		if (StringUtils.isBlank(anchor)) {
			return StringUtils.EMPTY;
		}

		return "#" + anchor;
	}

	public String getClassList() {
		return StringUtils.join(classList, " ");
	}

	public String getLink() {
		if (StringUtils.isBlank(link)) {
			return StringUtils.EMPTY;
		}

		// Page specified using Pathbrowser starts with /. Appends .html to also
		// work on publisher.
		if (link.startsWith("/")) {
			return link + ".html";
		}

		return link;
	}

	public String getMargin() {
		if (StringUtils.isBlank(margin)) {
			return StringUtils.EMPTY;
		}

		return margin;
	}

	public String getTarget() {
		if (external) {
			return "_blank";
		}

		return null;
	}

	public String getTitle() {
		if (StringUtils.isBlank(title)) {
			return StringUtils.EMPTY;
		}

		return title;
	}

}
