package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.Predicate;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.components.list.ListItem;
import com.statoil.reinvent.components.list.queries.ChildPagesQuery;
import com.statoil.reinvent.queries.ListQueryResult;
import com.statoil.reinvent.queries.TagQuery;
import com.statoil.reinvent.queries.TagQueryResult;
import com.statoil.reinvent.services.UserMapperService;
import com.statoil.reinvent.utils.RequestParameterUtil;
import com.statoil.reinvent.utils.SearchUtil;

public class NewsRoomList extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(NewsRoomList.class);

	private ResourceResolver resourceResolver;
	private String year;
	private List<String> years;

	private Integer firstYear;

	private String currentPagePath;
	private String displayType;
	private Long limit;
	private String query;
	private String rootPage;

	private String nodeName;
	private String nodePath;

	private String title;

	private boolean moreResults;

	private String sort;
	private long offset;

	private Set<String> allTags;
	private List<ListItem> list;

	private Locale language;
	private String[] tags;

	private RequestParameterUtil requestParameterUtil;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside newroomlist component wcmuse class");
		resourceResolver = slingScriptHelper.getService(UserMapperService.class).getJCRReadService();
		this.allTags = new HashSet<>();
		this.requestParameterUtil = new RequestParameterUtil(getRequest());

		// List settings
		this.title = properties.get("jcr:moduleTitle", String.class);
		this.displayType = "news";
		this.currentPagePath = getCurrentPage().getPath();
		this.limit = properties.get("limit", Long.class);

		this.language = getCurrentPage().getLanguage(true);
		this.rootPage = properties.get("parentPage", String.class);

		// Get parameters
		this.offset = requestParameterUtil.getLong("offset", 0L);
		if (StringUtils.isNotBlank(requestParameterUtil.getString("query"))) {
			String encodedQuery = requestParameterUtil.getString("query");
			this.query = SearchUtil.escapeQuery(encodedQuery);
			LOG.debug("query in newsroomlist is " + this.query);
		}

		this.sort = requestParameterUtil.getString("sort");
		this.year = requestParameterUtil.getString("year");

		this.nodeName = findNodeName();
		this.nodePath = getResource().getPath();

		this.tags = properties.get("tags", String[].class);
		this.list = findChildPagesWithTags();
		this.allTags = findAllTags(this.list);
		this.years = allYears();

		/*
		 * Closing resource resolver which is opened
		 */
		if (resourceResolver != null && resourceResolver.isLive()) {
			resourceResolver.close();
		}
	}

	private String findNodeName() throws RepositoryException {
		Node listNode = getResource().adaptTo(Node.class);
		return listNode.getName();
	}

	private List<String> allYears() throws RepositoryException {
		if (firstYear != null) {
			List<String> yearsList = new ArrayList<>();
			Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
			if (firstYear < currentYear) {
				for (int i = 0; i < currentYear - firstYear; i++) {
					yearsList.add(0, Integer.toString(firstYear + i));
				}
				return yearsList;
			}
		}
		return null;
	}

	public Map<String, String> getSortMethod() {
		Map<String, String> sortPredicate = new HashMap<>();

		sortPredicate.put("orderby", "@jcr:content/published");
		sortPredicate.put("orderby.sort", Predicate.SORT_DESCENDING);

		if (StringUtils.isNotEmpty(this.year)) {
			sortPredicate.put("daterange.property", "@jcr:content/published");
			sortPredicate.put("daterange.lowerBound", this.year + "-01-01");
			sortPredicate.put("daterange.lowerOperation", ">=");
			sortPredicate.put("daterange.upperBound", this.year + "-12-31");
			sortPredicate.put("daterange.upperOperation", "<=");
		}

		return sortPredicate;
	}

	public List<String> getYears() {
		return years;
	}

	public String getYear() {
		return year;
	}

	public String getLanguage() {
		return language.getLanguage();
	}

	private Set<String> findAllTags(List<ListItem> listItems) {
		Set<String> tagSet = new HashSet<>();
		if (listItems == null || listItems.isEmpty()) {
			return tagSet;
		}
		for (ListItem listItem : listItems) {
			tagSet.addAll(listItem.getCurrentTags());
		}
		return tagSet;
	}

	private List<ListItem> findPages() throws RepositoryException {
		if (tags == null) {
			return findChildPages();
		} else {
			return findTaggedChildPages();
		}
	}

	private List<ListItem> findChildPages() throws RepositoryException {
		ChildPagesQuery childPagesQuery = new ChildPagesQuery(resourceResolver, getPageManager());
		ListQueryResult listQueryResult = childPagesQuery.findChildPages(rootPage, this.query, limit,
				requestParameterUtil.getLong("offset"), getSortMethod(), true);
		moreResults = listQueryResult.hasMore;
		firstYear = listQueryResult.firstYear;
		return listQueryResult.results;
	}

	private List<ListItem> findChildPagesWithTags() throws RepositoryException {
		ChildPagesQuery childPagesQuery = new ChildPagesQuery(resourceResolver, getPageManager());
		ListQueryResult listQueryResult = childPagesQuery.findChildPagesAndRespectTags(rootPage, this.query, limit,
				requestParameterUtil.getLong("offset"), getSortMethod(), true, tags);
		moreResults = listQueryResult.hasMore;
		firstYear = listQueryResult.firstYear;
		return listQueryResult.results;
	}

	private List<ListItem> findTaggedChildPages() throws RepositoryException {
		TagQuery tagQuery = new TagQuery(resourceResolver, getPageManager());
		TagQueryResult result = tagQuery.findTaggedPages(rootPage, true, limit, offset, this.tags);
		this.moreResults = result.hasMore;
		List<ListItem> listItems = new ArrayList<>();
		for (Page page : result.results) {
			listItems.add(new ListItem(page));
		}
		return listItems;
	}

	public List<ListItem> getList() {
		return list;
	}

	public Set<String> getTags() {
		return allTags;
	}

	public String getDisplayType() {
		return displayType;
	}

	public String getCurrentPagePath() {
		return currentPagePath;
	}

	public String getQuery() {
		return query;
	}

	public String getSort() {
		return sort;
	}

	public boolean isMoreResults() {
		return moreResults;
	}

	public long getOffset() {
		return offset;
	}

	public boolean isEmpty() {
		return list == null || list.isEmpty();
	}

	public String getTitle() {
		return title;
	}

	protected RequestParameterUtil getRequestParameterUtil() {
		return requestParameterUtil;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getNodePath() {
		return nodePath;
	}

}
