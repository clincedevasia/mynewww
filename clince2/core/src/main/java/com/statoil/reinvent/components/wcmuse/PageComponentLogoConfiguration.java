package com.statoil.reinvent.components.wcmuse;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;

public class PageComponentLogoConfiguration extends BaseComponent {

	private static final String DEFAULT_LOGO_PATH = "/etc/designs/statoil/images/page/logo.png";
	private static final String DEFAULT_MINI_LOGO_PATH = "/etc/designs/statoil/images/page/statoil-mini-logo.png";
	private InheritanceValueMap inheritedProperties;
	private boolean noLogo;
	private String logoPath;
	private String miniLogoPath;
	private String logoImageClasses;
	
	@Override
	protected void activated() throws Exception {
		inheritedProperties = new HierarchyNodeInheritanceValueMap(currentPage.getContentResource());
		logoPath = inheritedProperties.getInherited("logoPath",String.class);
		miniLogoPath = inheritedProperties.getInherited("miniLogoPath",String.class);
		logoImageClasses = buildLogoImageClasses();
		noLogo = getNoLogoValue();
		
	}
	
	private String buildLogoImageClasses() {
		String isWideLogo = inheritedProperties.getInherited("wideLogo",String.class);
		if(StringUtils.isNotEmpty(isWideLogo)){
			StringJoiner stringJoiner = new StringJoiner(StringUtils.SPACE);
			stringJoiner.add("wide-logo");
			return stringJoiner.toString();
		}
		return StringUtils.EMPTY;
	}

	private boolean getNoLogoValue(){
		String noLogoValue = pageproperties.get("noLogo",String.class);
		if(StringUtils.isEmpty(noLogoValue)){
			return false;
		}else{
			return noLogoValue.equals("true") ? true : false;
		}
	}
	
	public boolean isNoLogo() {
		return noLogo;
	}
	
	public String getLogoPath() {
		return StringUtils.isEmpty(logoPath) ? DEFAULT_LOGO_PATH : logoPath;
	}

	public String getMiniLogoPath() {
		return StringUtils.isEmpty(miniLogoPath) ? DEFAULT_MINI_LOGO_PATH : miniLogoPath;
	}
	
	public String getLogoImageClasses() {
		return logoImageClasses;
	}


}
