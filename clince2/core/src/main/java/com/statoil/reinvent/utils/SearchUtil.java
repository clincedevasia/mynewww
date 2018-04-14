package com.statoil.reinvent.utils;

import com.day.cq.search.result.Hit;
import com.day.cq.wcm.api.Page;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper methods for getting properties in search.
 */
public class SearchUtil {

	private SearchUtil() {

	}

	/**
	 * Gets properties from the hit based on the input. It then returns the
	 * first property that is unempty.
	 *
	 * @param hit
	 *            Search hit returned by QueryBuilder API
	 * @param properties
	 *            Name of properties to prioritize
	 * @return the first unempty property
	 * @throws RepositoryException
	 */
	public static String getFirstUnemptyProperty(Resource resource, String... properties) throws RepositoryException {
		String[] propertiesValues = new String[properties.length];
		for (int i = 0; i < properties.length; i++) {
			propertiesValues[i] = resource.adaptTo(Page.class).getContentResource().getValueMap().get(properties[i],
					String.class);
		}
		return PropertiesUtil.getFirstUnemptyString(propertiesValues);
	}

	/**
	 * Returns the title of the page based on a QueryBuilder Hit
	 *
	 * @param hit
	 *            Search hit returned by QueryBuilder API
	 * @return either pageTitle, navTitle or jcr:title. In that order.
	 * @throws RepositoryException
	 */
	public static String getTitle(Resource resource) throws RepositoryException {
		return getFirstUnemptyProperty(resource, "pageTitle", "navTitle", "jcr:title");
	}

	/**
	 * Returns the description from the hit.
	 *
	 * @param hit
	 *            Search hit returned by QueryBuilder API
	 * @return jcr:description
	 * @throws RepositoryException
	 */
	public static String getDescription(Resource resource) throws RepositoryException {
		return resource.adaptTo(Page.class).getContentResource().getValueMap().get("jcr:description", String.class);
	}

	/**
	 * Returns the creation date
	 *
	 * @param hit
	 *            Search hit returned by QueryBuilder API
	 * @return jcr:created
	 * @throws RepositoryException
	 */
	public static Date getCreatedDate(Hit hit) throws RepositoryException {
		return hit.getProperties().get("jcr:created", Date.class);
	}

	/**
	 * Formats date of artikle to search
	 *
	 * @param date
	 *            The date to format
	 * @param language
	 *            The locale as a string
	 * @return Formatted date string
	 */
	public static String getFormattedDate(Date date, String language) {
		if (date == null) {
			return null;
		}

		Locale locale = new Locale(language);
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
		String formattedDate = dateFormat.format(date);
		return formattedDate;
	}

	private static final String[] chars = new String[] { "\\", "\"", "+", "-", "&&", "||", "!", "(", ")", "{", "}", "[",
			"]", "^", "~", "*", "?", ":", "<", ">", "/" };
	private static final String[] quotedChars = quotedChars();
	private static final String[] replacementChars = replacementChars();

	public static String escapeQuery(String query) {
		String newValue = query;
		for (int i = 0; i < chars.length; i++) {
			newValue = newValue.replaceAll(quotedChars[i], replacementChars[i]);
		}
		return newValue;
	}

	private static String[] quotedChars() {
		String[] quoted = new String[chars.length];
		for (int i = 0; i < chars.length; i++) {
			quoted[i] = Pattern.quote(chars[i]);
		}
		return quoted;
	}

	private static String[] replacementChars() {
		String[] replacement = new String[chars.length];
		for (int i = 0; i < chars.length; i++) {
			replacement[i] = Matcher.quoteReplacement("\\" + chars[i]);
		}
		return replacement;
	}

	public static List<String> getStaticSiteResults(String language) {
		List<String> sitePages = new ArrayList<>();
		sitePages.add("/content/statoil/" + language + "/what-we-do/johan-sverdrup");
		sitePages.add("/content/statoil/" + language + "/what-we-do/hywind-where-the-wind-takes-us");
		sitePages.add("/content/statoil/" + language + "/how-and-why/sustainability");
		return sitePages;
	}
}
