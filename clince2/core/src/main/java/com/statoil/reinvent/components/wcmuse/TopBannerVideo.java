package com.statoil.reinvent.components.wcmuse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.components.sling.models.ImageRenditions;
import com.statoil.reinvent.components.wcmuse.BaseComponent;

public class TopBannerVideo extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(TopBannerVideo.class);
	private String angle;
	private String anglecolor;
	private String posterPath;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside topbannervideo component wcmuse class");
		angle = getProperties().get("angle", String.class);
		anglecolor = getProperties().get("anglecolor", String.class);
		posterPath = new ImageRenditions(resourceResolver.getResource(properties.get("poster", String.class))).getExtraLarge();
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

	public String getPosterPath() {
		return posterPath;
	}

	private boolean topAngle() {
		return "both".equals(angle) || "top".equals(angle);
	}

}
