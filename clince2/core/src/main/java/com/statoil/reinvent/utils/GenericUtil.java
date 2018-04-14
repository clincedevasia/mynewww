package com.statoil.reinvent.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang3.StringUtils;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.constants.StatoilConstants;
import com.statoil.reinvent.models.ImageModel;
import com.statoil.reinvent.models.LinkModel;
import com.statoil.reinvent.models.MagazineListViewModel;
import com.statoil.reinvent.models.ReCaptchaValidationResponse;
import com.statoil.reinvent.services.GoogleReCaptchaValidationService;

public class GenericUtil {
	
	/**
	 * This method is used to convert iterator to java8 Stream object
	 * @param iterator {@link Iterator}
	 * @return {@link Stream}
	 */
	public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
		return asStream(sourceIterator, false);
	}
	
	/**
	 * This method is used to convert iterator to java8 Stream object with
	 * parallel execution
	 * @param iterator {@link Iterator}
	 * @param parallel
	 * @return {@link Stream}
	 */
	public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
		Iterable<T> iterable = () -> sourceIterator;
		return StreamSupport.stream(iterable.spliterator(), parallel);
	}
	
	/**
	 * This method validate user token and response with true or false
	 * @param userToken
	 * @param googleReCaptchaValidationService
	 * @return
	 */
	public static boolean validateRecaptcha(String userToken,
			GoogleReCaptchaValidationService googleReCaptchaValidationService) {
		ReCaptchaValidationResponse response = googleReCaptchaValidationService.validateUserInput(userToken, null);
		return response != null ? response.getSuccess() : false;
	}
	
	/**
	 * This method returns the list of tags that are associated to the child pages
	 * @param page
	 * @return
	 */
	public static List<Tag> getAllTagsOfChildPages(Page page){
		List<Tag> tagList = new ArrayList<>();
		Iterator<Page> children = page.listChildren();
		while (children.hasNext()) {
			Tag[] pageTags = children.next().getTags();
			if (pageTags != null) {
				Arrays.asList(pageTags).forEach(e -> tagList.add(e));
			}
		}
		return tagList;
	}
	
	/**
	 * This method is used to get property values of a node in List object.This
	 * comes handy if a property has multiple values.
	 * @param page {@link Page}
	 * @return valuesList {@link List}
	 */
	public static List<String> getPropertyValuesAsList(Property property) throws RepositoryException {
		List<String> valuesList = new ArrayList<>();
		if (property.isMultiple()) {
			Value[] values = property.getValues();
			for (Value value : values) {
				valuesList.add(value.getString());
			}
		} else {
			valuesList.add(property.getValue().getString());
		}
		return valuesList;
	}
	
	/**
	 * Use this to get Level 1 absolute parent
	 * @param currentPage {@link Page}
	 * @return Page {@link Page}
	 */
	public static Page getFirstLevelAbsoluteParent(Page currentPage) {
		return currentPage.getAbsoluteParent(1);
	}

	/**
	 * Use this to get Level 2 absolute parent
	 * @param currentPage {@link Page}
	 * @return Page {@link Page}
	 */
	public static Page getSecondLevelAbsoluteParent(Page currentPage) {
		return currentPage.getAbsoluteParent(2);
	}
	
	/**
	 * This method extracts the tags of page and creates a list with tag id's
	 * @param page
	 * @return
	 */
	public static List<String> extractPageTagsAsList(Page page){
		Tag[] tags = page.getTags();
		List<String> pageTags = Arrays.asList(tags)
									  .stream()
									  .map(e-> e.getTagID())
									  .collect(Collectors.toList());
		return pageTags;
		
	}
	
	/**
	 * This method extracts the tags of page and creates a list with Tags
	 * @param page
	 * @return
	 */
	public static List<Tag> extractPageTagsAsTagsList(Page page){
		if(page != null){
			Tag[] tags = page.getTags();
			List<Tag> pageTags = Arrays.asList(tags);
			return pageTags;
		}
		return Collections.emptyList();
	}
	
	/**
	 * This method is used to create LinkModel object from Page object which
	 * contains link and title of the page. Navigation title will take
	 * precedence if present 
	 * @param page {@link Page}
	 * @return LinkModel {@link LinkModel}
	 */
	public static LinkModel constructPageLinkModel(Page page) {
		LinkModel linkModel = new LinkModel();
		linkModel.setTitle(StringUtils.isEmpty(page.getTitle()) ? page.getNavigationTitle() : page.getTitle());
		linkModel.setLink(page.getPath() + StatoilConstants.Extensions.HTML_EXTENSION);
		linkModel.setPath(page.getPath());
		return linkModel;
	}
	
	public static MagazineListViewModel constructMagazineListModel(Page page){
		MagazineListViewModel magazineListViewModel =  new MagazineListViewModel();
		LinkModel linkModel = constructPageLinkModel(page);
		magazineListViewModel.setLinkModel(linkModel);
		ImageModel imageModel = constructMagazineImageModel(page);
		magazineListViewModel.setImageModel(imageModel);
		magazineListViewModel.setLastModified(page.getProperties().get(NameConstants.PN_PAGE_LAST_MOD, Date.class));
		return magazineListViewModel;
	}

	public static ImageModel constructMagazineImageModel(Page page) {
		ImageModel imageModel = new ImageModel();
		String topBanner = page.getContentResource()
							   .getChild(StatoilConstants.TOP_BANNER_PATH)
							   .getValueMap()
							   .get(StatoilConstants.FILE_REFERENCE, String.class);
		
		if(StringUtils.isNotEmpty(topBanner))
			imageModel.setImageSrc(topBanner);
		else
			imageModel.setImageSrc(page.getContentResource()
									   .getValueMap()
									   .get(StatoilConstants.BANNER_IMAGE, String.class));
		
		imageModel.setAltText(page.getName());
		return imageModel;
	}

}
