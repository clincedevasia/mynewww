package com.statoil.reinvent.queries;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagQuery {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TAG_PROPERTY = "jcr:content/@cq:tags";

    private final ResourceResolver resourceResolver;
    private final PageManager pageManager;

    public TagQuery(ResourceResolver resourceResolver, PageManager pageManager) {
        this.resourceResolver = resourceResolver;
        this.pageManager = pageManager;
    }

    public TagQueryResult findTaggedPages(String path, boolean or, String... tagIds) throws RepositoryException {
        return this.findTaggedPages(path, or, 0L, 0L, tagIds);
    }

    public TagQueryResult findTaggedPages(String path, boolean or, Long limit, Long offset, String... tagIds) throws RepositoryException {
        Map<String, String> predicates = new HashMap<>();
        predicates.put("path", path);
        predicates.put("type", "cq:Page");
        predicates.put("orderby", "@jcr:content/published");
        predicates.put("orderby.sort", "desc");
        if (limit != null && limit > 0) {
            predicates.put("p.limit", Long.toString(limit));
        }
        if (offset != null && offset > 0) {
            predicates.put("p.offset", Long.toString(offset));
        }
        if (or) {
            predicates.put("group.p.or", "true");
        } else {
            predicates.put("group.p.and", "true");
        }
        int i = 0;
        for (String tagId : tagIds) {
            predicates.put("group." + i + "_tagid", tagId);
            predicates.put("group." + i + "_tagid.property", TAG_PROPERTY);
            i++;
        }

        QueryBuilder qb = resourceResolver.adaptTo(QueryBuilder.class);
        Session session = resourceResolver.adaptTo(Session.class);
        Query qry = qb.createQuery(PredicateGroup.create(predicates), session);
        List<Hit> hits = qry.getResult().getHits();

        boolean moreResults = false;
        if (limit != 0 && qry.getResult().getTotalMatches() > (limit + offset)) {
            moreResults = true;
        }

        ArrayList<Page> pages = new ArrayList<>();
        for (Hit hit : hits) {
            pages.add(pageManager.getPage(hit.getNode().getPath()));
        }

        return new TagQueryResult(pages, moreResults);
    }
    
    public List<Page> findSortedTaggedPages(String path, boolean or, String fieldToOrderBy, String sortOrder, String... tagIds) throws RepositoryException {
        Map<String, String> predicates = new HashMap<>();
        predicates.put("path", path);
        predicates.put("type", "cq:Page");

        if (or) {
            predicates.put("group.p.or", "true");
        } else {
            predicates.put("group.p.and", "true");
        }

        int i = 0;

        for (String tagId : tagIds) {
            predicates.put("group." + i + "_tagid", tagId);
            predicates.put("group." + i + "_tagid.property", TAG_PROPERTY);
            i++;
        }

        predicates.put("property", "jcr:content/cq:lastModified");
        predicates.put("property.operation", "exists");

        predicates.put("orderby", fieldToOrderBy);
        predicates.put("orderby.sort", sortOrder);

        QueryBuilder qb = resourceResolver.adaptTo(QueryBuilder.class);
        Session session = resourceResolver.adaptTo(Session.class);
        Query qry = qb.createQuery(PredicateGroup.create(predicates), session);
        List<Hit> hits = qry.getResult().getHits();
        ArrayList<Page> pages = new ArrayList<>();
        for (Hit hit : hits) {
            pages.add(pageManager.getPage(hit.getNode().getPath()));
        }
        return pages;
    }
    
    public List<Page> findSortedPagesExcludingsTags(String path, boolean or, String fieldToOrderBy, String sortOrder, String... tagIds) throws RepositoryException {
        Map<String, String> predicates = new HashMap<>();
        predicates.put("path", path);
        predicates.put("type", "cq:Page");
        predicates.put("group.p.not", "true");

        if (or) {
            predicates.put("group.p.or", "true");
        } else {
            predicates.put("group.p.and", "true");
        }

        int i = 0;
        for (String tagId : tagIds) {
            predicates.put("group." + i + "_tagid", tagId);
            predicates.put("group." + i + "_tagid.property", TAG_PROPERTY);
            i++;
        }

        predicates.put("group." + i + "_property", "jcr:content/cq:lastModified");
        predicates.put("group." + i + "_property.operation", "exists");

        predicates.put("orderby", fieldToOrderBy);
        predicates.put("orderby.sort", sortOrder);

        QueryBuilder qb = resourceResolver.adaptTo(QueryBuilder.class);
        Session session = resourceResolver.adaptTo(Session.class);
        Query qry = qb.createQuery(PredicateGroup.create(predicates), session);
        List<Hit> hits = qry.getResult().getHits();
        logger.info("Searched for pages not Tagged, found {} hits", hits.size());
        ArrayList<Page> pages = new ArrayList<>();
        for (Hit hit : hits) {
            pages.add(pageManager.getPage(hit.getNode().getPath()));
        }
        return pages;
    }

    public TagQueryResult findTaggedPages(String path, String... tagIds) throws RepositoryException {
        return findTaggedPages(path, false, tagIds);
    }
}
