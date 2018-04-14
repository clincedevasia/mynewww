package com.statoil.reinvent.jobs.newsimport;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.services.UserMapperService;

@Component(enabled = false, metatype = true, label = "News Import Modification Batch")
@Service(value = Runnable.class)
@Property(name = "scheduler.expression", value = "0 * * * * ?")
public class NewsImportModificationBatch implements Runnable {

	private final Logger LOGGER = LoggerFactory.getLogger(NewsImportModificationBatch.class);

	public static final String STATOIL_TAG_WORKSPACE = "statoil-tags:";
	public static final String NOTIFIABLE_TRADING = "notifiable trading";
	public static final String NOTIFIABLE_TRADING_NO = "meldepliktig handel";
	public static final String NOTIFIABLE_TRADING_TAG = "notifiable-trading";
	public static final String STOCK_MARKET_ANNOUNCEMENT = "stock market announcement";
	public static final String STOCK_MARKET_ANNOUNCEMENT_NO = "børsmelding";
	public static final String STOCK_MARKET_ANNOUNCEMENT_TAG = "stock-market-announcements";
	public static final String GENERAL_NEWS_TAG = "general-news";

	@Reference
	private UserMapperService userMapperService;

	private ResourceResolver resourceResolver;

	private void iterate(Node node) throws RepositoryException {
		NodeIterator rootNodeIterator = node.getNodes();
		while (rootNodeIterator.hasNext()) {
			Node nextNode = rootNodeIterator.nextNode();
			if (nextNode.hasNodes()) {
				iterate(nextNode);
			}
			if (nextNode.getProperty("jcr:primaryType").getString().equals("cq:Page")) {
				fixPage(resourceResolver.getResource(nextNode.getPath()).adaptTo(Page.class));
			}
		}
	}

	private void fixPage(Page page) throws RepositoryException {
		ValueMap properties = page.getProperties();
		String title = properties.get("jcr:title", String.class);
		String category = page.getProperties().get("category", String.class);
		if (hasBeenModified(category)) {
			return;
		}
		if (modifyBasedOnLanguage(title, NOTIFIABLE_TRADING, NOTIFIABLE_TRADING_NO) != null) {
			fixTitle(page, modifyBasedOnLanguage(title, NOTIFIABLE_TRADING, NOTIFIABLE_TRADING_NO));
			changeCategory(page, NOTIFIABLE_TRADING_TAG);
			addTag(page, NOTIFIABLE_TRADING_TAG);
		} else if (modifyBasedOnLanguage(title, STOCK_MARKET_ANNOUNCEMENT, STOCK_MARKET_ANNOUNCEMENT_NO) != null) {
			fixTitle(page, modifyBasedOnLanguage(title, STOCK_MARKET_ANNOUNCEMENT, STOCK_MARKET_ANNOUNCEMENT_NO));
			changeCategory(page, STOCK_MARKET_ANNOUNCEMENT_TAG);
			addTag(page, STOCK_MARKET_ANNOUNCEMENT_TAG);
		} else {
			addTag(page, GENERAL_NEWS_TAG);
			changeCategory(page, GENERAL_NEWS_TAG);
		}
		replaceTags(page);
	}

	private boolean hasBeenModified(String category) {
		return !("NEWS".equals(category) || "NYHETER".equals(category));
	}

	private String modifyBasedOnLanguage(String title, String... languageStrings) {
		for (String lan : languageStrings) {
			if (title.toLowerCase().contains(lan)) {
				return modifyTitle(title, lan);
			}
		}
		return null;
	}

	private String modifyTitle(String original, String keyword) {
		String lowerCase = original.toLowerCase();
		int stop = lowerCase.lastIndexOf(keyword);
		return original.substring(0, stop).trim();
	}

	private void fixTitle(Page page, String newTitle) throws RepositoryException {
		Node contentNode = page.getContentResource().adaptTo(Node.class);
		contentNode.setProperty("jcr:title", newTitle);
	}

	private void addTag(Page page, String tag) throws RepositoryException {
		Node contentNode = page.getContentResource().adaptTo(Node.class);
		contentNode.setProperty("cq:tags", new String[] { STATOIL_TAG_WORKSPACE + tag });
	}

	private void changeCategory(Page page, String tag) throws RepositoryException {
		Node contentNode = page.getContentResource().adaptTo(Node.class);
		contentNode.setProperty("category", tag);
	}

	private void replaceTags(Page page) throws RepositoryException {
		Node textNode = page.getContentResource().adaptTo(Node.class).getNode("par").getNode("text");
		String body = textNode.getProperty("text").getString();
		String newBody = body.replaceAll("<b>", "<strong>");
		newBody = newBody.replaceAll("</b>", "</strong>");
		newBody = newBody.replaceAll("<i>", "<em>");
		newBody = newBody.replaceAll("</i>", "</em>");
		textNode.setProperty("text", newBody);
	}

	@Override
	public void run() {
		try {
			resourceResolver = userMapperService.getJCRWriterService();
			String pathNo = "/content/statoil/no/news/archive";
			String pathEn = "/content/statoil/en/news/archive";
			Resource rootResourceNo = resourceResolver.getResource(pathNo);
			Resource rootResourceEn = resourceResolver.getResource(pathEn);
			Node rootNodeNo = rootResourceNo.adaptTo(Node.class);
			Node rootNodeEn = rootResourceEn.adaptTo(Node.class);
			iterate(rootNodeNo);
			iterate(rootNodeEn);
			rootNodeEn.getSession().save();
			rootNodeNo.getSession().save();
		} catch (RepositoryException | LoginException e) {
			LOGGER.error("Exception due to ", e);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
	}
}