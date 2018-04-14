package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.i18n.I18n;

public class RssFeed extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(RssFeed.class);

	private static final String ALL_NEWS_RSS_LINK = "/services/wcwfeed?lang=%s&feedtype=news";
	private static final String NOTIFIABLE_TRADING_RSS_LINK = "/services/wcwfeed?lang=%s&feedtype=notif";
	private static final String REMIT_RSS_LINK = "/services/remitrss";
	private static final String STOCK_MARKET_ANNOUNCEMENTS_RSS_LINK = "/services/wcwfeed?lang=%s&feedtype=stock";

	private I18n i18n;
	private Locale locale;

	private String title;
	private String space;
	private List<RssFeedLink> rssFeedLinks = new ArrayList<>();

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside rssfeed component wcmuse class");
		locale = currentPage.getLanguage(true);
		ResourceBundle resourceBundle = request.getResourceBundle(locale);
		i18n = new I18n(resourceBundle);

		this.title = properties.get("title", String.class);
		this.space = properties.get("space", String.class);

		AddRssFeedLink("allNews", ALL_NEWS_RSS_LINK);
		AddRssFeedLink("notifiableTrading", NOTIFIABLE_TRADING_RSS_LINK);
		AddRssFeedLink("remit", REMIT_RSS_LINK);
		AddRssFeedLink("stockMarketAnnouncements", STOCK_MARKET_ANNOUNCEMENTS_RSS_LINK);
	}

	private void AddRssFeedLink(String propertyName, String rssFeedLink) {
		String propertyValue = properties.get(propertyName, String.class);
		if (StringUtils.isNotBlank(propertyValue) && propertyValue.equals("true")) {
			String text = i18n.get("rssFeed." + propertyName);
			String link = String.format(rssFeedLink, locale.getLanguage());
			rssFeedLinks.add(new RssFeedLink(text, link));
		}
	}

	public List<RssFeedLink> getRssFeedLinks() {
		return rssFeedLinks;
	}

	public String getTitle() {
		return title;
	}

	public String getSpace() {
		return space;
	}

	public class RssFeedLink {
		private String link;
		private String text;

		public RssFeedLink(String text, String link) {
			this.text = text;
			this.link = link;
		}

		public String getLink() {
			return link;
		}

		public String getText() {
			return text;
		}
	}

}