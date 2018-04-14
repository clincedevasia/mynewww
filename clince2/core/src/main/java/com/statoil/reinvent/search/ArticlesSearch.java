package com.statoil.reinvent.search;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.google.gson.stream.JsonWriter;
import com.statoil.reinvent.utils.SearchUtil;

public class ArticlesSearch extends Search {

	public static final String KEY = "news";

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public ArticlesSearch(ResourceResolver resourceResolver) {
		super(KEY, resourceResolver);
	}

	@Override
	Map<String, String> predicates(String query, String language) {
		Map<String, String> predicates = new HashMap<String, String>();
		String path = PATH;
		if (language != null) {
			path += "/" + language + "/news";
		}

		predicates.put("path", path);

		predicates.put("type", "cq:Page");
		predicates.put("p.limit", "20");

		if (StringUtils.isNotBlank(query)) {
			predicates.put("fulltext", query);
		} 
		predicates.put("path.exact", "true");
		predicates.put("orderby", "@jcr:content/published");
		predicates.put("orderby.sort", "desc");

		return predicates;
	}

	@Override
	Map<String, String> validate(Resource resource) throws RepositoryException {
		String title = SearchUtil.getTitle(resource);
		String date = SearchUtil.getFormattedDate(
				resource.adaptTo(Page.class).getContentResource().getValueMap().get("published", Date.class), language);
		String path = resourceResolver.map(resource.getPath());
		if (title != null && date != null) {
			Map<String, String> validatedHit = new HashMap<String, String>();
			validatedHit.put("title", title);
			validatedHit.put("date", date);
			validatedHit.put("path", path);
			return validatedHit;
		} else {
			return null;
		}
	}

	@Override
	public void searchWithoutQuery(JsonWriter jsonWriter, String language) {
		// This is handled through predicates in article search
	}
}
