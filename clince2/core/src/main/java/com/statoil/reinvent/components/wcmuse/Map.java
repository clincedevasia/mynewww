package com.statoil.reinvent.components.wcmuse;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.statoil.reinvent.editor.fields.Multifield;

public class Map extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(Map.class);
	private String angle;
	private String anglecolor;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside map component wcmuse class");
		angle = getProperties().get("angle", String.class);
		anglecolor = getProperties().get("anglecolor", String.class);
	}

	public String getAngle() {
		String angleClass = "";
		if (bottomAngle()) {
			angleClass += getBottomAngleClass();
		}
		if (topAngle()) {
			angleClass += " " + getTopAngleClass();
		}
		return angleClass;
	}

	private List<JsonObject> getJsonObjects(String relativePath) {
		return Multifield.getObjects(getProperties(), relativePath);
	}

	private String getJsonObjectString(JsonObject jsonObject, String property) {
		return jsonObject.has(property) ? jsonObject.get(property).getAsString() : StringUtils.EMPTY;
	}

	public String getMarkers() {
		List<JsonObject> jsonObjects = getJsonObjects("markers");

		StringBuilder markers = new StringBuilder();
		markers.append("[");

		for (JsonObject jsonObject : jsonObjects) {
			String title = getJsonObjectString(jsonObject, "title");
			String location = getJsonObjectString(jsonObject, "location");
			String content = getJsonObjectString(jsonObject, "content");

			markers.append("{\"title\": \"" + title + "\",\"location\": \"" + location + "\",\"content\": \"" + content
					+ "\"},");
		}

		markers.setLength(markers.length() - 1);
		markers.append("]");

		return markers.toString();
	}

	private boolean bottomAngle() {
		return "both".equals(angle) || "bottom".equals(angle);
	}

	private String getBottomAngleClass() {
		if ("both".equals(anglecolor) || "bottom".equals(anglecolor)) {
			return "overlay-bottom-alternate";
		}
		return "overlay-bottom";
	}

	private String getTopAngleClass() {
		if ("both".equals(anglecolor) || "top".equals(anglecolor)) {
			return "overlay-top-alternate";
		}
		return "overlay-top";
	}

	private boolean topAngle() {
		return "both".equals(angle) || "top".equals(angle);
	}

	public class Marker {

		private String title;
		private String location;
		private String content;

		public Marker(String title, String location, String content) {
			this.title = title;
			this.location = location;
			this.content = content;
		}

		public String getTitle() {
			return title;
		}

		public String getLocation() {
			return location;
		}

		public String getContent() {
			return content;
		}

	}

}