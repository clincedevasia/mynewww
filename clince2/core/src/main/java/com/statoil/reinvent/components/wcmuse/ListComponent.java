package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;
import com.statoil.reinvent.components.list.ListItem;
import com.statoil.reinvent.components.list.queries.ChildPagesQuery;
import com.statoil.reinvent.editor.fields.CheckBox;
import com.statoil.reinvent.editor.fields.Multifield;
import com.statoil.reinvent.queries.ListQueryResult;
import com.statoil.reinvent.utils.RequestParameterUtil;

public class ListComponent extends BaseComponent {

	private static Logger LOG = LoggerFactory.getLogger(ListComponent.class);

	private String currentPagePath;
	private String displayType;
	private String query;
	private String rootPage;
	private String nodeName;
	private String title;

	private Long limit;

	private boolean childPages;
	private boolean staticPages;
	private boolean deep;
	private boolean disableImages;
	private boolean sortEnabled;
	private boolean filterEnabled;
	private boolean searchEnabled;
	private boolean tilesEnabled;
	private boolean moreResults;

	private String sort;
	private Long offset;
	private Set<String> allTags;
	private List<ListItem> list;
	private Locale language;
	private RequestParameterUtil requestParameterUtil;
	private String nodePath;
	private String space;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside listcomponent component wcmuse class");

		this.allTags = new HashSet<>();
		this.requestParameterUtil = new RequestParameterUtil(getRequest());
		nodePath = getResource().getPath();

		// List Identifier
		this.nodeName = findNodeName();

		// List settings
		this.title = properties.get("jcr:moduleTitle", String.class);
		this.displayType = properties.get("displayAs", String.class);
		this.currentPagePath = currentPage.getPath();
		this.limit = properties.get("limit", Long.class);
		this.sortEnabled = CheckBox.getValue(properties, "sortEnabled");
		this.filterEnabled = CheckBox.getValue(properties, "filterEnabled");
		this.searchEnabled = CheckBox.getValue(properties, "searchEnabled");
		this.tilesEnabled = CheckBox.getValue(properties, "tiles");
		this.disableImages = CheckBox.getValue(properties, "disableImages");

		this.deep = CheckBox.getValue(properties, "deep");

		String listFrom = properties.get("listFrom", String.class);
		this.childPages = "children".equals(listFrom);
		this.staticPages = "static".equals(listFrom);

		this.language = getCurrentPage().getLanguage(true);
		this.rootPage = properties.get("parentPage", String.class);

		// Get parameters
		this.offset = requestParameterUtil.getLong("offset", 0L);
		this.query = requestParameterUtil.getString("query");
		this.sort = requestParameterUtil.getString("sort");

		this.list = findPages();
		this.allTags = findAllTags(this.list);

		space = getProperties().get("space", String.class);
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
		if (staticPages) {
			return createStaticPages();
		} else if (childPages && deep) {
			return findDeepChildPages();
		} else if (childPages) {
			return findShallowChildPages();
		}
		return new ArrayList<>();
	}

	private List<ListItem> createStaticPages() throws RepositoryException {
		List<JsonObject> pages = Multifield.getObjects(getProperties(), "pages");
		List<ListItem> listItems = new ArrayList<>();
		for (JsonObject pageJson : pages) {
			listItems.add(createListItem(pageJson));
		}
		return listItems;
	}

	private ListItem createListItem(JsonObject jsonObject) throws RepositoryException {
		if (!jsonObject.get("link").getAsString().startsWith("http")) {
			Page internalPage = getPageManager().getPage(jsonObject.get("link").getAsString());
			return new ListItem(internalPage, jsonObject, getResourceResolver());
		} else {
			return new ListItem(jsonObject, getResourceResolver());
		}
	}

	private List<ListItem> findDeepChildPages() throws RepositoryException {
		ChildPagesQuery childPagesQuery = new ChildPagesQuery(getResourceResolver(), getPageManager());
		ListQueryResult listQueryResult = childPagesQuery.findChildPages(rootPage, this.query, limit, this.offset,
				getSortMethod(), true);
		moreResults = listQueryResult.hasMore;
		return listQueryResult.results;
	}

	private List<ListItem> findShallowChildPages() throws RepositoryException {
		Page rootPage = getPageManager().getPage(this.rootPage);
		Iterator<Page> childPages = rootPage.listChildren();
		List<ListItem> pageList = new ArrayList<>();
		int i = 0;
		while (childPages.hasNext()) {
			if ((i >= offset && pageList.size() < limit) || limit == 0) {
				pageList.add(new ListItem(childPages.next()));
			} else {
				childPages.next();
			}
			i++;
		}
		if (i > offset + pageList.size()) {
			moreResults = true;
		}
		return pageList;
	}

	private String findNodeName() throws RepositoryException {
		Node listNode = getResource().adaptTo(Node.class);
		return listNode.getName();
	}

	protected Map<String, String> getSortMethod() {
		Map<String, String> sortPredicate = new HashMap<>();
		if ("title".equals(getSort())) {
			sortPredicate.put("orderby", "@jcr:content/jcr:title");
			return sortPredicate;
		} else {
			sortPredicate.put("orderby", "@jcr:content/jcr:created");
			sortPredicate.put("orderby.sort", "desc");
			return sortPredicate;
		}
	}

	public String getSpace() {
		return space;
	}

	public String getLanguage() {
		return language.getLanguage();
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

	public String getOffset() {
		return offset.toString();
	}

	public boolean isEmpty() {
		return list == null || list.isEmpty();
	}

	public boolean isFilterEnabled() {
		return childPages && filterEnabled;
	}

	public boolean isTilesEnabled() {
		return tilesEnabled;
	}

	public boolean isSortEnabled() {
		return childPages && sortEnabled;
	}

	public boolean isSearchEnabled() {
		return childPages && searchEnabled;
	}

	public boolean isDisableImages() {
		return disableImages;
	}

	public String getTitle() {
		return title;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getNodePath() {
		return nodePath;
	}

}