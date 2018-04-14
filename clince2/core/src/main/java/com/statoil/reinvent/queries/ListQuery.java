package com.statoil.reinvent.queries;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.wcm.api.PageManager;
import com.statoil.reinvent.components.list.ListItem;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

public class ListQuery {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PUBLISHED_PROPERTY = "@jcr:content/published";

    private ResourceResolver resourceResolver;
    private PageManager pageManager;

    public ListQuery(ResourceResolver resourceResolver, PageManager pageManager) {
        this.resourceResolver = resourceResolver;
        this.pageManager = pageManager;
    }

    public ListQueryResult findPages(long limit, long offset, Map<String, String>... predicateMaps) throws RepositoryException {
        boolean moreResults = false;
        Map<String, String> predicates = new HashMap<>();
        predicates.put("p.offset", Long.toString(offset));
        predicates.put("p.limit", Long.toString(limit));
        predicates.putAll(compilePredicates(predicateMaps));

        QueryBuilder qb = resourceResolver.adaptTo(QueryBuilder.class);
        Session session = resourceResolver.adaptTo(Session.class);
        Query qry = qb.createQuery(PredicateGroup.create(predicates), session);

        List<Hit> hits = qry.getResult().getHits();
        if (limit != 0 && qry.getResult().getTotalMatches() > (limit + offset)) {
            moreResults = true;
        }

        Query dateQry = qb.createQuery(PredicateGroup.create(firstYearPredicates(predicates)), session);
        List<Hit> dateHits = dateQry.getResult().getHits();

        Integer firstYear = null;
        if (dateHits.size() > 0) {
            firstYear = getFirstYear(dateHits.get(0));
        }

        ArrayList<ListItem> listItems = new ArrayList<>();
        for (Hit hit : hits) {
            listItems.add(convertHitToListItem(hit));
        }
        return new ListQueryResult(listItems, moreResults, firstYear);
    }

    private Integer getFirstYear(Hit firstYearHit) throws RepositoryException {
        ListItem hit = convertHitToListItem(firstYearHit);
        return hit.getCalendar().get(Calendar.YEAR);
    }

    private Map<String, String> firstYearPredicates(Map<String, String> predicates) {
        Map<String, String> firstYear = new HashMap<>();
        firstYear.put("path", predicates.get("path"));
        firstYear.put("type", predicates.get("type"));
        firstYear.put("orderby", PUBLISHED_PROPERTY);
        firstYear.put("orderby.sort", "asc");
        firstYear.put("property", PUBLISHED_PROPERTY);
        firstYear.put("property.operation", "exists");
        firstYear.put("p.limit", "1");
        firstYear.put("p.offset", "0");
        return firstYear;
    }

    private Map<String, String> compilePredicates(Map<String, String>[] predicateMaps) {
        Map<String, String> compiledPredicates = new HashMap<>();
        for (Map<String, String> predicateMap : predicateMaps) {
            compiledPredicates.putAll(predicateMap);
        }
        return compiledPredicates;
    }

    private ListItem convertHitToListItem(Hit hit) throws RepositoryException {
        return new ListItem(pageManager.getPage(hit.getNode().getPath()));
    }
}
