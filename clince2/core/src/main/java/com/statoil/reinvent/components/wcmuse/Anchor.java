package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anchor extends BaseComponent {
	
	private static final Logger LOG = LoggerFactory.getLogger(Anchor.class);
	private String identifier;
	private String label;
	private List<String> classList = new ArrayList<>();

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside Anchor component wcmuse class");
		identifier = properties.get("anchor", String.class);
		label = properties.get("label", String.class);

		if (identifier != null)
			classList.add("anchor-container");
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getLabel() {
		return StringUtils.isBlank(label) ? identifier : label;
	}

	public String getClassList() {
		return StringUtils.join(classList, " ");
	}

}