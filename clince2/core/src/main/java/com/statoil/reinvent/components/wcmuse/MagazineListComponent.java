package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.QueryBuilder;
import com.statoil.reinvent.constants.StatoilConstants;
import com.statoil.reinvent.models.MagazineTag;
import com.statoil.reinvent.services.AbstractPageList;
import com.statoil.reinvent.services.ListFactory;
import com.statoil.reinvent.utils.GenericUtil;

public class MagazineListComponent extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(MagazineListComponent.class);
	
	private AbstractPageList abstractPageList;
	private QueryBuilder queryBuilder;
	private List<?> filteredList = new ArrayList<>();
	
	private List<MagazineTag> allTags = new ArrayList<>();
	private Integer totalResultsCount;
	private boolean showMoreOption;
	
	@Override
	protected void activated() throws Exception {
		ListFactory listFactory = slingScriptHelper.getService(ListFactory.class);
		queryBuilder = slingScriptHelper.getService(QueryBuilder.class);
		Optional<String> listFrom = Optional.ofNullable(properties.get(StatoilConstants.List.LIST_FROM, String.class));
		String displayAs = properties.get(StatoilConstants.List.DISPLAY_AS, String.class);
		Optional<String> orderBy = Optional.ofNullable(properties.get(StatoilConstants.List.ORDER_BY, String.class));
		Optional<String> sortOrder = Optional.ofNullable(properties.get(StatoilConstants.List.SORT_ORDER, String.class));
		Optional<String> limit = Optional.ofNullable(properties.get(StatoilConstants.List.LIMIT, String.class));
		String offSet = request.getParameter("offset");
		String filterTag = request.getParameter("filterTag");
		abstractPageList = listFactory.getPageList(displayAs);
		if(listFrom.isPresent()){
			buildListFrom(listFrom.get());
			allTags = abstractPageList.getAllTags()
					  .stream()
					  .map( e -> new MagazineTag(e.getTitle(this.getLocale()), e.getTagID()))
					  .collect(Collectors.toList());
			filteredList = abstractPageList.filterByTag(filterTag)
										   .orderPagesBy(orderBy.orElse(JcrConstants.JCR_TITLE))
										   .sortOrder(sortOrder.orElse("desc"))
										   .offSet(offSet)
										   .limitPages(Long.parseLong(limit.orElse("0")))
										   .display();
			
			totalResultsCount = abstractPageList.getTotalResultCount();
			showMoreOption = setShowMoreOption(offSet,limit);
		}
	}

	/**
	 * This method returns AbstractPageList object with injected list of pages based in selection
	 * without filters
	 * @param listFrom
	 * @throws RepositoryException
	 */
	private void buildListFrom(String listFrom) throws RepositoryException {

		switch (listFrom) {
		case StatoilConstants.List.LIST_CHILDREN:
			Optional<String> parentPagePath = Optional.ofNullable(properties.get(StatoilConstants.List.PARENT_PAGE, String.class));
			abstractPageList.getChildPages(parentPagePath.orElse(currentPage.getPath()), resourceResolver);
			break;
		case StatoilConstants.List.LIST_STATIC:
			Property pages = properties.get(StatoilConstants.List.PAGES, Property.class);
			List<String> pagePaths = GenericUtil.getPropertyValuesAsList(pages);
			abstractPageList.getFixedPages(pagePaths, resourceResolver);
			break;
		case StatoilConstants.List.LIST_TAGS:
			Optional<String> tagsSearchRoot = Optional.ofNullable(properties.get(StatoilConstants.List.TAGS_SEARCH_ROOT, String.class));
			String tagMatch =properties.get(StatoilConstants.List.TAG_MATCH, String.class);
			Property tags = properties.get(StatoilConstants.List.LIST_TAGS, Property.class);
			List<String> selectedTagsList = GenericUtil.getPropertyValuesAsList(tags);
			abstractPageList.getTaggedPages(tagsSearchRoot.orElse(currentPage.getPath()), selectedTagsList, tagMatch, request);
			break;
		case StatoilConstants.List.LIST_SEARCH:
			String query = properties.get(StatoilConstants.List.SEARCH_QUERY, String.class);
			Optional<String> searchIn = Optional.ofNullable(properties.get(StatoilConstants.List.SEARCH_IN, String.class));
			abstractPageList.getPagesBySearch(searchIn.orElse(GenericUtil.getFirstLevelAbsoluteParent(currentPage).getPath()), query, request);
			break;
		case StatoilConstants.List.LIST_ADV_SEARCH:
			String savedQuery = properties.get(StatoilConstants.List.SAVED_QUERY, String.class);
			abstractPageList.getPagesByAdvSearch(savedQuery, queryBuilder, request);
			break;
		default:
			LOG.error("Invalid list from option");
			break;
		}
	}
	/**
	 * This method show more option true or false 
	 * which will be used in frontend
	 * @return
	 */
	private boolean setShowMoreOption(String offSet, Optional<String> limit) {
		if(StringUtils.isNotEmpty(offSet))
			return Integer.parseInt(offSet) + Integer.parseInt(limit.orElse("0")) < totalResultsCount;
		else{
			if(limit.isPresent() && !limit.get().equals("0"))
				return Integer.parseInt(limit.get()) < totalResultsCount;
			else
				return false;
		}
	}
	/**
	 * This method is used in HTL component
	 * @return
	 */
	public List<?> getFilteredList() {
		return filteredList;
	}

	/**
	 * Gives total result count
	 * @return
	 */
	public Integer getTotalResultsCount() {
		return totalResultsCount;
	}
	/**
	 * Show more options for pagination in frontend
	 * @return
	 */
	public boolean isShowMoreOption() {
		return showMoreOption;
	}

	/**
	 * Gets all tags of the page in the front which can be used to 
	 * filter the list
	 * @return
	 */
	public List<MagazineTag> getAllTags() {
		return allTags;
	}
	


}
