package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.WCMMode;
import com.statoil.reinvent.editor.fields.CheckBox;

public class Download extends BaseComponent {

	private String link = "";
	private String title;
	private String buttontext = "";
	private Boolean showTitle = false;

	private String colors;
	private boolean icon;
	// Initializes the button
	private ArrayList<String> classList = new ArrayList<String>();

	public boolean getShowTitle() {
		return showTitle != null && showTitle;
	}

	@Override
	protected void activated() throws Exception {
		boolean isEdit = WCMMode.fromRequest(request) == WCMMode.EDIT;
		boolean isDesign = WCMMode.fromRequest(request) == WCMMode.DESIGN;

		icon = CheckBox.getValue(properties, "icon");
		showTitle = properties.get("showTitle", Boolean.class);
		buttontext = properties.get("buttontext", String.class);
		colors = properties.get("colors", String.class);
		title = properties.get("jcr:title", String.class);
		link = properties.get("linkURL", String.class);
		classList.add("btn");
		classList.add(properties.get("size", String.class));

		if (StringUtils.isBlank(link) && isEdit || isDesign) {
			classList.add("cq-placeholder");
		} else {
			classList.add("download");
		}

		if (StringUtils.isNotBlank(colors)) {
			classList.add(colors);
		}
	}

	public String getButtontext() {
		return buttontext;
	}

	public String getLink() {
		return link;
	}

	public String getTitle() {
		return title;
	}

	public String getFilename() {
		Resource resource = resourceResolver.getResource(link);
		if (resource == null)
			return "";
		Asset asset = resource.adaptTo(Asset.class);
		return asset.getName() + "";
	}

	public String getClassList() {
		return StringUtils.join(classList, " ");
	}

	public String getFilesize() {

		Resource resource = resourceResolver.getResource(link);

		if (resource == null)
			return "";
		Asset asset = resource.adaptTo(Asset.class);
		return FileUtils.byteCountToDisplaySize(asset.getOriginal().getSize());

	}

	public String getIcon() {
		if (StringUtils.isNotBlank(link) && icon)
			return FilenameUtils.getExtension(link);
		return null;

	}

	public String getExtension() {
		if (StringUtils.isBlank(link))
			return "";
		String ext = FilenameUtils.getExtension(link);
		return ext.toUpperCase();
	}

}