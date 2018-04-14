package com.statoil.reinvent.components.wcmuse;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.components.wcmuse.BaseComponent;

public class Topbanner extends BaseComponent {

	private final Logger LOG = LoggerFactory.getLogger(Topbanner.class);
	private boolean rendered;
	private String backgroundColor;
	private String mode;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside topbanner component wcmuse class");
		mode = getProperties().get("mode", String.class);
		backgroundColor = getProperties().get("backgroundColor", String.class);

		boolean isEdit = getWcmMode().isEdit();
		Node node = getResource().adaptTo(Node.class);
		Node parNode = null;

		try {
			parNode = node.getNode("par");
		} catch (PathNotFoundException e) {
			LOG.error("Path not found", e);
		}

		boolean hasParNode = parNode != null && parNode.hasNodes();

		if (hasParNode || isEdit) {
			rendered = true;
		}
	}

	public boolean isRendered() {
		return rendered;
	}

	public String getMode() {
		return mode;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

}
