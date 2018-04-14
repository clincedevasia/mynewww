package com.statoil.reinvent.components.list.queries;

import com.day.cq.wcm.api.PageManager;
import com.statoil.reinvent.queries.ListQuery;
import com.statoil.reinvent.queries.ListQueryResult;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

public class ChildPagesQuery {

    private ListQuery listQuery;
    private static final String TAG_PROPERTY = "jcr:content/@cq:tags";

    public ChildPagesQuery(ResourceResolver resourceResolver, PageManager pageManager) {
        this.listQuery = new ListQuery(resourceResolver, pageManager);
    }

    public ListQueryResult findChildPages(String path, String query, Long limit, Long offset, Map<String, String> additionPredicates, boolean deep) throws RepositoryException {
        Map<String, String> predicates = buildNewsTemplatePredicates(path, query, deep);
        return listQuery.findPages(getLongValue(limit), getLongValue(offset), predicates, additionPredicates);
    }

	public ListQueryResult findChildPagesAndRespectTags(String path, String query, Long limit, Long offset,
			Map<String, String> additionPredicates, boolean deep, String[] tagIds) throws RepositoryException {
		Map<String, String> predicates = buildNewsTemplatePredicates(path, query, deep);
		if (ArrayUtils.isNotEmpty(tagIds)) {
			predicates.put("group.p.or", "true");
			int i = 0;
			for (String tagId : tagIds) {
				predicates.put("group." + i + "_tagid", tagId);
				predicates.put("group." + i + "_tagid.property", TAG_PROPERTY);
				i++;
			}
		}
		predicates.putAll(getQueryPredicate(query));
		return listQuery.findPages(getLongValue(limit), getLongValue(offset), predicates, additionPredicates);
	}
	
	private Map<String, String> buildNewsTemplatePredicates(String path, String query, boolean deep) {
		Map<String, String> predicates = new HashMap<String, String>();
        predicates.put("path", path);
        if (!deep) {
            predicates.put("path.exact", "true");
        }
        predicates.put("type", "cq:Page");

        predicates.put("template.p.or", "true");
        predicates.put("template.1_property", "jcr:content/cq:template");
        predicates.put("template.1_property.value", "/apps/statoil/templates/news");
        predicates.put("template.2_property", "jcr:content/cq:template");
        predicates.put("template.2_property.value", "/apps/statoil/templates/newsarchive");

        predicates.putAll(getQueryPredicate(query));
		return predicates;
	}

    
    private long getLongValue(Long longValue) {
        if (longValue != null) {
            return longValue.longValue();
        } else {
            return 0L;
        }
    }

    private Map<String, String> getQueryPredicate(String query) {
        Map<String, String> queryPredicate = new HashMap<>();
        if (query != null) {
            queryPredicate.put("fulltext", "*" + query + "*");
        }
        return queryPredicate;
    }
}
