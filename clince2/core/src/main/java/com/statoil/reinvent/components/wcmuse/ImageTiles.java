package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.google.gson.JsonObject;
import com.statoil.reinvent.components.image.Image;
import com.statoil.reinvent.editor.fields.Multifield;

public class ImageTiles extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(ImageTiles.class);
	
	private List<Description> descriptions;
	private Locale language;
	private String space;
	
	private static final int MAX_IMAGE_NUMBER = 4;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside imagetitle component wcmuse class");
		List<JsonObject> jsonObjects = Multifield.getObjects(getProperties(), "images");
		descriptions = createDescriptions(jsonObjects);
		language = getCurrentPage().getLanguage(true);
		space = getProperties().get("space", String.class);

	}

	private List<Description> createDescriptions(List<JsonObject> jsonObjects) {
		List<Description> descriptionList = new ArrayList<>();
		int i = 0;
		for (JsonObject jsonObject : jsonObjects) {
			if (i < MAX_IMAGE_NUMBER) {
				Asset imageAsset = getResourceResolver().getResource(jsonObject.get("image").getAsString())
						.adaptTo(Asset.class);
				descriptionList
						.add(new Description(new Image(imageAsset, language), jsonObject.get("description").getAsString()));
				i++;
			} else {
				break;
			}
		}
		return descriptionList;
	}

	public String getCountClass() {
		if (descriptions.size() == 2) {
			return "duo";
		} else if (descriptions.size() == 3) {
			return "tri";
		}
		return null;
	}

	public List<Description> getDescriptions() {
		return descriptions;
	}

	public String getLanguage() {
		return language.getLanguage();
	}

	public String getSpace() {
		return space;
	}

	public static class Description {

		private String description;
		private String photographer;
		private String aria;
		private Image image;

		public Description(Image image, String description) {
			this.image = image;
			this.description = description;
			this.photographer = image.getPhotographer();
			this.aria = image.getUuid();
			this.image.setAriaDescriptionId(aria);
		}

		public String getDescription() {
			return description;
		}

		public String getPhotographer() {
			return photographer;
		}

		public String getAria() {
			return aria;
		}

		public Image getImage() {
			return image;
		}
	}

}
