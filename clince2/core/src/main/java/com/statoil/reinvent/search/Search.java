package com.statoil.reinvent.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.google.gson.stream.JsonWriter;
import com.statoil.reinvent.utils.SearchUtil;

/**
 * Abstract class to make configuring searches easier. Uses the QueryBuilder API from AEM.
 */
public abstract class Search {

    /**
     * Base path
     */
    public static final String PATH = "/content/statoil";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ResourceResolver resourceResolver;

    private String key;

    protected String language;

    public Search(String key, ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
        this.key = key;
    }

    /**
     * Builds a Map of predicates that should be used in the search. See QueryBuilder API for available predicates.
     *
     * @param query Search string entered into search
     * @param language Language of the page searched from
     * @return Map of predicates
     */
    abstract Map<String, String> predicates(String query, String language);

    /**
     * Validates a hit.
     *
     * @param hit The hit from the querybuilder
     * @return A map with the JSON output. Null if not valid.
     * @throws RepositoryException
     */
    abstract Map<String, String> validate(Resource resource) throws RepositoryException;
    /**
     * This method is used to get static result set if query is null
     * @param jsonWriter
     * @param language
     */
    public abstract void searchWithoutQuery(JsonWriter jsonWriter, String language) throws IOException;

    /**
     * Perform a search using QueryBuilder API and write the result to a JSONWriter.
     * Result will be appended to the key of the search.
     *
     * @param jsonWriter JSONWriter to write output to
     * @param query The query string to search for
     * @param language Language of the query
     * @throws RepositoryException
     * @throws IOException 
     */
    public void search(JsonWriter jsonWriter, String query, String language) throws RepositoryException, IOException {
        this.language = language;
        List<Hit> result = performQuery(query, language);
        List<Resource> resultResources = new ArrayList<>();
        for (Hit hit : result) {
        	resultResources.add(hit.getResource());
		}
        writeJson(jsonWriter, resultResources);
    }

    private List<Hit> performQuery(String query, String language) {
        String escapedQuery = SearchUtil.escapeQuery(query);
        Map<String, String> predicates = predicates(escapedQuery, language);

        QueryBuilder qb = resourceResolver.adaptTo(QueryBuilder.class);
        Session session = resourceResolver.adaptTo(Session.class);
        Query qry = qb.createQuery(PredicateGroup.create(predicates), session);

        if (!predicates.containsKey("p.limit")) {
            qry.setHitsPerPage(20L);
        }

        SearchResult result = qry.getResult();

        return result.getHits();
    }

    public void writeJson(JsonWriter jsonWriter, List<Resource> resources) throws IOException {
        jsonWriter.name(key).beginArray();
        for (Resource resource : resources) {
            Map<String, String> validatedResult = null;
            try {
                validatedResult = validate(resource);
            } catch (RepositoryException e) {
                logger.debug("Exception when validating search result", e);
            }
            if(validatedResult != null) {
                jsonWriter.beginObject();
                for (String key : validatedResult.keySet()) {
                    jsonWriter.name(key).value(validatedResult.get(key));
                }
                jsonWriter.endObject();
            }
        }
        jsonWriter.endArray();
    }
}
