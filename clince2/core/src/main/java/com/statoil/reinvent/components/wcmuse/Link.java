package com.statoil.reinvent.components.wcmuse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Link extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(Link.class);
	private String title = "";
	private String link = "";
	private String extension = "";
	private String alignment;
	private boolean external;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside link component wcmuse class");
		title = properties.get("jcr:title", String.class);
		link = properties.get("linkURL", String.class);
		String externalVal = properties.get("external", String.class);
		alignment = properties.get("align", String.class);
		external = "yes".equals(externalVal);
		extension = "html";
	}

	public String getAlignment() {
		return alignment;
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		if (!extension.equals("")) {
			return link + "." + extension;
		}
		return link;
	}

	public String getTarget() {
		if (external) {
			return "_blank";
		}
		return null;
	}

}