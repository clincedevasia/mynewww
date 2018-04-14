package com.statoil.reinvent.components.wcmuse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;

public class PageComponentMetadata extends BaseComponent {

	private static final String OVERRIDE_TOP_BANNER = "overrideTopBanner";
	private static final String HTML_EXTENSION = ".html";
	private static final String TOP_BANNER = "topbanner/banner";
	private static final String FILE_REFERENCE = "fileReference";
	private static final String TRANSFORM_EXTRA_LARGE_IMAGE_PNG = ".transform/extra-large/image.png";


	private String scheme;
	private String openGraphImageUrl;
	private String requestUrl;
	private String canonicalUrl;

	@Override
	protected void activated() throws Exception {
		scheme = request.getHeader("X-Forwarded-Proto");
		if (scheme == null) {
			scheme = request.getScheme();
		}
		requestUrl = sanitizePathToDomain(request.getRequestURI());
		canonicalUrl = resourceResolver.adaptTo(Externalizer.class).publishLink(resourceResolver, scheme, currentPage.getPath())+ HTML_EXTENSION;
		buildOpenGraphUrl();
	}
	
	private void buildOpenGraphUrl() {
		Optional<Resource> topBanner = Optional.ofNullable(resource.getChild(TOP_BANNER));
		Optional<String> overrideTopBanner = Optional.ofNullable(properties.get(OVERRIDE_TOP_BANNER, String.class));
		if (!overrideTopBanner.isPresent() && topBanner.isPresent() && StringUtils.isNoneBlank(getbannerImagePath(topBanner.get()))) {
			String bannerPath = getbannerImagePath(topBanner.get());
			openGraphImageUrl = sanitizePathToDomain(bannerPath) + TRANSFORM_EXTRA_LARGE_IMAGE_PNG;
		} else {
			openGraphImageUrl = requestUrl.substring(0, requestUrl.lastIndexOf(HTML_EXTENSION)) + ".thumb.1200.1200.png";
		}
	}

	private String getbannerImagePath(Resource path) {
		return path.getValueMap().get(FILE_REFERENCE, String.class);
	}

	private String sanitizePathToDomain(String path) {
		return scheme + "://" + request.getServerName() + path;
	}

	public String getOpenGraphImageUrl() {
		return openGraphImageUrl;
	}

	public String getUrl() {
		return requestUrl;

	}

	public String getRobots() {
		Page frontPage = currentPage.getAbsoluteParent(2);
		ValueMap values = frontPage.getContentResource().getValueMap();
		String robots = values.get("metaTagsRobots", String.class);

		return robots;
	}

	public String getTitle() throws URISyntaxException {
		List<String> list = new ArrayList<>();

		String title = StringUtils.isNotBlank(currentPage.getTitle()) ? currentPage.getTitle()
				: currentPage.getName();
		list.add(title);

		String pageTitle = currentPage.getPageTitle();
		if (StringUtils.isNotBlank(pageTitle)) {
			list.add(pageTitle);
		}

		String requestUrl = request.getRequestURL().toString();
		String domainName = getDomainName(requestUrl);
		list.add(domainName);

		return StringUtils.join(list, " - ");
	}

	public String getDomainName(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String domain = uri.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	public String getCanonicalUrl() {
		return canonicalUrl;
	}

}