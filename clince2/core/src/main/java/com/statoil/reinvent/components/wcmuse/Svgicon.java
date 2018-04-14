package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.components.wcmuse.BaseComponent;
import com.statoil.reinvent.editor.fields.CheckBox;
import com.statoil.reinvent.utils.ImageUtil;

public class Svgicon extends BaseComponent {
	
	private static final Logger LOG = LoggerFactory.getLogger(Svgicon.class);
	private String path;
	private String svgType;
	private Locale language;
	private boolean responsive;
	private String link;

	@Override
	protected void activated() throws Exception {
		LOG.info("Inside svgicon component wcmuse class");
		ValueMap properties = getProperties();
		path = properties.get("svgUrl", String.class);
		svgType = properties.get("./svgType", String.class);
		language = getCurrentPage().getLanguage(true);
		link = properties.get("./linkURL", String.class);
		if (link != null && !link.startsWith("http")) {
			link += ".html";
		}
		responsive = CheckBox.getValue(properties, "responsiveEnabled");
	}

	public String getLink() {
		return link;
	}

	public String getSvgType() {
		if (responsive)
			return "responsive-" + svgType;
		return svgType;
	}

	public String getAlternateText() {
		Resource resource = getResourceResolver().getResource(path);
		return ImageUtil.getAlternateText(resource, language);
	}

}