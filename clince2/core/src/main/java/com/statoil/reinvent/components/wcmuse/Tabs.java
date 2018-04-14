package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Tabs extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(Tabs.class);
	private List<String> tabListing;
	private String space;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside tabs component wcmuse class");
		tabListing = new ArrayList<>();
		String[] tabs = getProperties().get("tabs", String[].class);
		if (tabs != null && tabs.length > 0) {
			for (String singleTab : tabs) {
				JsonObject tabNameJSON = new Gson().fromJson(singleTab, JsonObject.class);
				if (tabNameJSON.has("tabName") && !tabNameJSON.get("tabName").getAsString().isEmpty()) {
					tabListing.add(tabNameJSON.get("tabName").getAsString());
				}
			}
		}
		space = getProperties().get("space", String.class);
	}

	public List<String> getTabListing() {
		return tabListing;
	}

	public String getSpace() {
		return space;
	}

}