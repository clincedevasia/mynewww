package com.statoil.reinvent.editor.fields;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Helper class for multifield in AEM
 */
public class Multifield {

	private static final Logger logger = LoggerFactory.getLogger(Multifield.class);

	private Multifield() {
	}

	/**
	 * Parses the field in a multifield node and returns an array of size 0-n.
	 * Returns an empty array if nothing is found.
	 *
	 * Throws JSON exception if there's an error in the JSON formatting.
	 *
	 * @param node
	 * @param propertyPath
	 * @return
	 */
	public static List<JsonObject> getObjects(Node node, String propertyPath) {
		List<JsonObject> jsonObjects = new ArrayList<>();
		try {
			Property property = node.getProperty(propertyPath);
			if (property.isMultiple()) {
				Value[] values = property.getValues();
				for (Value value : values) {
					jsonObjects.add(new Gson().fromJson(value.getString(), JsonObject.class));
				}
			} else {
				jsonObjects.add(new Gson().fromJson(property.getString(), JsonObject.class));
			}
			jsonObjects.forEach(e -> logger.info(e.toString().toString()));
		} catch (RepositoryException e) {
			logger.debug("Could not get property", e);
		}
		return jsonObjects;
	}

	public static List<JsonObject> getObjects(ValueMap properties, String propertyName) {
		String[] values = properties.get(propertyName, String[].class);
		List<JsonObject> jsonObjects = new ArrayList<>();
		if (values != null) {
			for (String jsonValue : values) {
				jsonObjects.add(new Gson().fromJson(jsonValue, JsonObject.class));
			}
		}
		return jsonObjects;
	}

}
