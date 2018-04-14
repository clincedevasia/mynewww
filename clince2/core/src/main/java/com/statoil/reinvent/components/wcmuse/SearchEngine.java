package com.statoil.reinvent.components.wcmuse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.request.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;
import com.statoil.reinvent.search.ArticlesSearch;
import com.statoil.reinvent.search.Search;
import com.statoil.reinvent.search.SiteSearch;

public class SearchEngine extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(SearchEngine.class);

	private String language;

	private String query;

	@Override
	protected void activated() throws Exception {
		LOG.debug("inside searchengine component wcmuse class");
		response.setContentType("application/json");
		language = getParameter("language");
		query = getParameter("query");

		JsonWriter jsonWriter = new JsonWriter(getResponse().getWriter());
		Search siteSearch = new SiteSearch(resourceResolver);
		Search articlesSearch = new ArticlesSearch(resourceResolver);
		jsonWriter.beginObject();

		if (StringUtils.isBlank(query))
			siteSearch.searchWithoutQuery(jsonWriter, language);
		else
			siteSearch.search(jsonWriter, query, language);
		articlesSearch.search(jsonWriter, query, language);

		jsonWriter.endObject();

	}

	private String getParameter(String key) {
		RequestParameter requestParameter = request.getRequestParameter(key);
		if (requestParameter != null) {
			return requestParameter.getString();
		}
		return null;
	}

}