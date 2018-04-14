package com.statoil.reinvent.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;
import com.statoil.reinvent.utils.ImageUtil;
import com.statoil.reinvent.utils.SearchUtil;

public class SiteSearch extends Search {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String KEY = "site";

	public SiteSearch(ResourceResolver resourceResolver) {
		super(KEY, resourceResolver);
	}

	@Override
	Map<String, String> predicates(String query, String language) {
		Map<String, String> predicates = new HashMap<String, String>();
		String path = PATH;
		if (language != null) {
			path += "/" + language;
		}
		predicates.put("group.p.not", "true");
		predicates.put("group.path", path + "/news");
		predicates.put("group.path.self", "true");
		predicates.put("path", path);
		predicates.put("type", "cq:Page");
		predicates.put("p.limit", "20");
		predicates.put("fulltext", query);
		predicates.put("orderby", "@jcr:score");
		predicates.put("orderby.sort", "desc");

		return predicates;
	}

	@Override
	Map<String, String> validate(Resource resource) throws RepositoryException {
		String title = SearchUtil.getTitle(resource);
		String description = SearchUtil.getDescription(resource);
		Resource thumbnail = getThumbnail(resource.adaptTo(Node.class));
		String path = resourceResolver.map(resource.getPath());
		if (title != null && description != null) {
			Map<String, String> validatedHit = new HashMap<String, String>();
			if (thumbnail != null) {
				validatedHit.put("thumbnail", thumbnail.getPath());
				validatedHit.put("alt", getAlternateText(thumbnail));
			}
			validatedHit.put("path", path);
			validatedHit.put("title", title);
			validatedHit.put("description", description);
			return validatedHit;
		} else {
			return null;
		}
	}

	private Resource getThumbnail(Node node) {
		String file = null;
		try {
			Node bannerNode = node.getNode("jcr:content").getNode("topbanner").getNode("banner");
			if (bannerNode.hasProperty("fileReference")) {
				String fileReference = bannerNode.getProperty("fileReference").getString();
				if (StringUtils.isNotBlank(fileReference)) {
					file = fileReference;
				}
			} else {
				file = node.getNode("jcr:content").getProperty("bannerImage").getString();
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
			return null;
		}
		if (file == null) {
			return null;
		}
		return resourceResolver.getResource(file);
	}

	private String getAlternateText(Resource imageResource) {
		return ImageUtil.getAlternateText(imageResource, language);
	}

	@Override
	public void searchWithoutQuery(JsonWriter jsonWriter, String language) throws IOException {
		this.language = language;
		List<String> staticPaths = SearchUtil.getStaticSiteResults(language);
		List<Resource> resources = new ArrayList<>();
		for (String staticPath : staticPaths) {

			resources.add(resourceResolver.getResource(staticPath));
		}
		writeJson(jsonWriter, resources);
	}
}
