package com.statoil.reinvent.servlets;

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.statoil.reinvent.queries.TagQuery;
import com.statoil.reinvent.rss.RSSFeedBuilder;

@SlingServlet(paths = { "/services/wcwfeed" }, methods = { "GET" })
public class RssFeedServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -7885498112986089826L;

	private static final String[] LANGUAGES = new String[] { "en", "no" };

	@Reference
	private Externalizer externalizer;

	private static final Logger logger = LoggerFactory.getLogger(RssFeedServlet.class);

	/**
	 * Adds lead to list.
	 * 
	 * @param slingRequest
	 * @param slingResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected final void doGet(SlingHttpServletRequest slingRequest, SlingHttpServletResponse slingResponse)
			throws IOException {
		// To minimize breaking changes we use the same parameters as in the
		// original service,
		// http://www.statoil.com/_layouts/wcw/wcwfeed.aspx?lang=en&feedtype=news
		String lang = slingRequest.getParameter("lang");
		String feedType = slingRequest.getParameter("feedtype");

		// If lang is not specified, default to Norwegian as in original service
		// to prevent breaking changes.
		if (StringUtils.isBlank(lang)) {
			lang = "no";
		} else if (!ArrayUtils.contains(LANGUAGES, lang)) {
			throw new IllegalArgumentException("Invalid lang parameter: " + lang);
		}

		// Map.
		String path = "/content/statoil/" + lang;
		String tagId = ToTag(feedType);

		logger.info("Mapped lang from " + lang + " to " + path + " and feedtype from " + feedType + " to " + tagId);

		String tagNamespace = "statoil-tags:";
		String[] tagNamespacedIds = null;

		ResourceResolver resourceResolver = slingRequest.getResourceResolver();
		PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
		// http://technotes.iangreenleaf.com/posts/2013-10-17-rss-the-least-wrong-way.html
		slingResponse.setContentType("text/xml");

		try {

			TagQuery tagQuery = new TagQuery(resourceResolver, pageManager);
			List<Page> feedPages = null;
			String[] tagIds = { tagId };
			if ("all".equals(tagId)) {
				tagNamespacedIds = new String[3];
				tagNamespacedIds[0] = tagNamespace + "notifiable-trading";
				tagNamespacedIds[1] = tagNamespace + "stock-market-announcements";
				tagNamespacedIds[2] = tagNamespace + "general-news";
				feedPages = tagQuery.findSortedTaggedPages(path, true, "@jcr:content/cq:lastModified", "desc",
						tagNamespacedIds);
				tagIds = new String[] { tagId };
				String rssFeedTitle = lang.equals("no") ? "Alle nyheter" : "All News";
				RSSFeedBuilder.writeTaggedPages(slingRequest, rssFeedTitle, tagIds, lang, feedType, feedPages,
						resourceResolver, slingResponse.getWriter());
			} else if ("notifiable-trading".equals(tagId)) {
				tagNamespacedIds = new String[1];
				tagNamespacedIds[0] = tagNamespace + tagId;
				feedPages = tagQuery.findSortedTaggedPages(path, false, "@jcr:content/cq:lastModified", "desc",
						tagNamespacedIds);
				String rssFeedTitle = lang.equals("no") ? "Meldepliktig handel" : "Notifiable trading";
				RSSFeedBuilder.writeTaggedPages(slingRequest, rssFeedTitle, tagIds, lang, feedType, feedPages,
						resourceResolver, slingResponse.getWriter());
			} else if ("stock-market-announcements".equals(tagId)) {
				tagNamespacedIds = new String[1];
				tagNamespacedIds[0] = tagNamespace + tagId;
				feedPages = tagQuery.findSortedTaggedPages(path, false, "@jcr:content/cq:lastModified", "desc",
						tagNamespacedIds);
				String rssFeedTitle = lang.equals("no") ? "Børsmeldinger" : "Stock market announcements";
				RSSFeedBuilder.writeTaggedPages(slingRequest, rssFeedTitle, tagIds, lang, feedType, feedPages,
						resourceResolver, slingResponse.getWriter());
			}

		} catch (RepositoryException e) {
			logger.error("RepositoryException occurred", e);
		} catch (IOException e) {
			logger.error("IOException occurred", e);
		}
		slingResponse.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred");
	}

	private String ToTag(String feedType) {
		switch (feedType) {
		case "notif":
			return "notifiable-trading";
		case "stock":
			return "stock-market-announcements";
		default:
			// If feedtype is invalid, default to News as in original service to
			// prevent breaking changes.
			return "all";
		}
	}
}
