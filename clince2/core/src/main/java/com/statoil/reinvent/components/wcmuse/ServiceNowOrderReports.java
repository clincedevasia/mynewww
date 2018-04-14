package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.day.cq.dam.api.Asset;

public class ServiceNowOrderReports extends BaseComponent {
	private String heading;
	private String annualReportPath;
	private String prospectusReportPath;
	private String statutoryReportPath;
	private Locale language;

	@Override
	protected void activated() throws Exception {
		heading = properties.get("heading", String.class);
		annualReportPath = properties.get("annualReportPath", String.class);
		prospectusReportPath = properties.get("prospectusReportPath", String.class);
		statutoryReportPath = properties.get("statutoryReportPath", String.class);
		this.language = getCurrentPage().getLanguage(true);
	}

	public String getHeading() {
		return heading;
	}

	public String getAnnualReportPath() {
		return getAssetName(annualReportPath);
	}

	public String getProspectusReportPath() {
		return getAssetName(prospectusReportPath);
	}

	public String getStatutoryReportPath() {
		return getAssetName(statutoryReportPath);
	}

	public String getLanguage() {
		return language.getLanguage();
	}

	private String getAssetName(String path) {
		Resource resource = getRequest().getResourceResolver().getResource(path);

		if (resource == null) {
			return StringUtils.EMPTY;
		}

		Asset asset = resource.adaptTo(Asset.class);
		return asset.getPath();
	}

}
