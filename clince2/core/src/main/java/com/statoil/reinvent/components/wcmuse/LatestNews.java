package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;
import com.statoil.reinvent.components.list.ListItem;
import com.statoil.reinvent.components.list.queries.ChildPagesQuery;
import com.statoil.reinvent.editor.fields.CheckBox;
import com.statoil.reinvent.editor.fields.Multifield;
import com.statoil.reinvent.queries.ListQueryResult;
import com.statoil.reinvent.queries.TagQuery;
import com.statoil.reinvent.queries.TagQueryResult;
import com.statoil.reinvent.services.UserMapperService;
import com.statoil.reinvent.utils.RequestParameterUtil;

public class LatestNews extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(LatestNews.class);

	private ResourceResolver resourceResolver;
	private String nodeName;
	private String nodePath;

	private boolean childPages;
	private boolean staticPages;
	private boolean showMoreButton;
	private boolean deepSearch;
	private boolean hideDate;

	private String title;

	private Page linkPage;

	private long offset;
	private boolean moreResults;

	private List<ListItem> list;

	private String parentPage;

	private Locale language;

	String[] tags;

	private RequestParameterUtil requestParameterUtil;

	private String space;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside latestnews component wcmuse class");
		resourceResolver = slingScriptHelper.getService(UserMapperService.class).getJCRReadService();
		this.requestParameterUtil = new RequestParameterUtil(request);

		// List Identifier
		this.nodeName = findNodeName();
		this.nodePath = resource.getPath();

		// List settings
		this.title = properties.get("jcr:moduleTitle", String.class);
		this.parentPage = properties.get("parentPage", String.class);
		this.linkPage = findLinkedPage(properties.get("linkUrl", String.class));
		String listFrom = properties.get("listFrom", String.class);
		this.childPages = "children".equals(listFrom);
		this.staticPages = "static".equals(listFrom);
		this.showMoreButton = CheckBox.getValue(properties, "moreButton");
		this.deepSearch = CheckBox.getValue(properties, "deep");
		this.hideDate = CheckBox.getValue(properties, "hideDate");

		this.language = currentPage.getLanguage(true);
		this.tags = properties.get("tags", String[].class);

		// Get parameters
		this.offset = requestParameterUtil.getLong("offset", 0L);
		this.list = findPages();
		space = properties.get("space", String.class);
		/*
		 * Closing resource resolver which is opened 
		 */
		if (resourceResolver != null && resourceResolver.isLive()) {
			resourceResolver.close();
		}
	}

	public String getSpace() {
		return space;
	}

	private Page findLinkedPage(String path) {
		if (StringUtils.isNotBlank(path)) {
			return getPageManager().getPage(path);
		}
		return null;
	}

	private String findNodeName() throws RepositoryException {
		Node listNode = getResource().adaptTo(Node.class);
		return listNode.getName();
	}

	public String getLanguage() {
		return language.getLanguage();
	}

	private List<ListItem> findPages() throws RepositoryException {
		if (staticPages) {
			return createStaticPages();
		} else if (childPages && tags != null) {
			return findTaggedPages();
		} else if (childPages) {
			return findChildPages();
		}
		return new ArrayList<>();
	}

	private List<ListItem> createStaticPages() throws RepositoryException {
		List<JsonObject> pages = Multifield.getObjects(getProperties(), "pages");
		List<ListItem> listItems = new ArrayList<>();
		long i = 0;
		for (JsonObject pageJson : pages) {
			if (i >= offset && listItems.size() < 3) {
				listItems.add(createListItem(pageJson));
			}
			i++;
		}
		if (offset + listItems.size() < pages.size()) {
			this.moreResults = true;
		}
		return listItems;
	}

	private ListItem createListItem(JsonObject jsonObject) throws RepositoryException {
		Page internalPage = getPageManager().getPage(jsonObject.get("link").getAsString());
		return new ListItem(internalPage);
	}

	private List<ListItem> findTaggedPages() throws RepositoryException {
		TagQuery tagQuery = new TagQuery(resourceResolver, getPageManager());
		TagQueryResult result = tagQuery.findTaggedPages(this.parentPage, true, 3L, offset, this.tags);
		this.moreResults = result.hasMore;
		List<ListItem> listItems = new ArrayList<>();
		for (Page page : result.results) {
			listItems.add(new ListItem(page));
		}
		return listItems;
	}

	private List<ListItem> findChildPages() throws RepositoryException {
		Map<String, String> sortByDate = new HashMap<>();
		sortByDate.put("orderby", "@jcr:content/published");
		sortByDate.put("orderby.sort", "desc");
		ChildPagesQuery childPagesQuery = new ChildPagesQuery(resourceResolver, getPageManager());
		ListQueryResult listQueryResult = childPagesQuery.findChildPages(parentPage, null, 3L, offset, sortByDate,
				this.deepSearch);
		moreResults = listQueryResult.hasMore;
		return listQueryResult.results;
	}

	public List<ListItem> getList() {
		return list;
	}

	public boolean isMoreResults() {
		return moreResults;
	}

	public boolean isEmpty() {
		return list == null || list.isEmpty();
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

	public boolean isHideDate() {
		return hideDate;
	}

	public String getLinkText() {
		if (linkPage != null) {
			return linkPage.getTitle();
		}
		return null;
	}

	public String getLinkUrl() {
		if (linkPage != null) {
			return linkPage.getPath() + ".html";
		}
		return null;
	}

	public boolean isShowMoreButton() {
		return showMoreButton;
	}

}