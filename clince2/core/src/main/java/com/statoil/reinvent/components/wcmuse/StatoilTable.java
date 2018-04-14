package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.statoil.reinvent.editor.fields.Multifield;

public class StatoilTable extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(StatoilTable.class);

	private List<String> headers;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside statoiltable component wcmuse class");
		List<JsonObject> headings = Multifield.getObjects(getProperties(), "columnheaders");
		headers = new ArrayList<>();
		for (JsonObject jsonObject : headings) {
			headers.add(jsonObject.get("title").getAsString());
		}
	}
	
	public List<String> getHeaders() {
		return headers;
	}

}