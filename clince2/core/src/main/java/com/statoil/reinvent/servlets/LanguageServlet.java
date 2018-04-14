package com.statoil.reinvent.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.statoil.reinvent.constants.StatoilConstants;
import com.statoil.reinvent.models.LanguageSwitchSitesModel;
import com.statoil.reinvent.services.impl.LanguageSwitchPredefinedSites;

@SlingServlet(paths = { "/services/language" }, methods = { "GET" })
public class LanguageServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -8012271679008901132L;

	@Reference
	private LanguageSwitchPredefinedSites languageSwitchPredefinedSites;
	
	private ResourceResolver resourceResolver;
	private PageManager pageManager;
	private Map<String, String> contentPathToDomain;

	private static final Logger logger = LoggerFactory.getLogger(LanguageServlet.class);

	private JsonArray buildLanguages(String serverName, String sitePath, String pagePath) {
		logger.info("Site path = " + sitePath + ", page path = " + pagePath);

		JsonArray jsonArray = new JsonArray();
		Page sitePage = pageManager.getPage(sitePath);

		// Sites are hard-coded, but might not have been published yet.
		if (sitePage == null) {
			return jsonArray;
		}

		Iterator<Page> frontPages = sitePage.listChildren(null, false);

		if (frontPages == null) {
			return jsonArray;
		}

		while (frontPages.hasNext()) {
			Page frontPage = frontPages.next();
			String frontPageTemplate = frontPage.getProperties().get("cq:template", String.class);

			if (frontPageTemplate.equals("/apps/statoil/templates/front")) {
				Locale locale = frontPage.getLanguage(false);
				String displayLanguage = locale.getDisplayLanguage();

				JsonObject language = new JsonObject();
				language.addProperty("name", displayLanguage);

				// + "/" to prevent the global site, /content/statoil, to match
				// all other sites.
				if (pagePath.startsWith(sitePath + "/")) {
					String frontPagePath = frontPage.getPath();
					logger.info("Front Page Path=" + frontPagePath);
					String otherLanguagePath = frontPagePath + pagePath.substring(frontPagePath.length());
					Page otherLanguagePage = pageManager.getPage(otherLanguagePath);

					if (otherLanguagePage == null) {
						language.addProperty("isAvailable", false);
						language.addProperty("url", constructLanguageSwitchURL(frontPage, frontPage.getPath()));
					} else {
						language.addProperty("isAvailable", true);
						language.addProperty("url", constructLanguageSwitchURL(frontPage, otherLanguagePath));
					}
				} else {
					language.addProperty("isAvailable", false);
					language.addProperty("url", constructLanguageSwitchURL(frontPage, frontPage.getPath()));
				}

				jsonArray.add(language);
			}
		}

		return jsonArray;
	}

	private JsonObject buildSite(String serverName, String name, String url, String sitePath, String pagePath) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", name);
		jsonObject.addProperty("url", url);
		jsonObject.add("languages", buildLanguages(serverName, sitePath, pagePath));

		return jsonObject;
	}

	private JsonArray buildSites(String serverName, List<LanguageSwitchSitesModel> sites, String pagePath) {
		JsonArray jsonArray = new JsonArray();

		for (LanguageSwitchSitesModel site : sites) {
			// Sites are pre-defined, but might not be published.
			Page sitePage = pageManager.getPage(site.getPath());
			if (sitePage != null) {
				JsonObject jsonObject = buildSite(serverName, site.getName(), site.getUrl(), site.getPath(), pagePath);
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	private String getSite(List<LanguageSwitchSitesModel> predefinedSites, String pagePath) {
		for (LanguageSwitchSitesModel site : predefinedSites) {
			if (pagePath.startsWith(site.getPath() + "/")) {
				return site.getName();
			}
		}

		throw new IllegalArgumentException("Unknown site for path: " + pagePath);
	}

	private JsonObject buildPage(List<LanguageSwitchSitesModel> predefinedSites, String url, String pagePath) {
		logger.info("buildPage. url=" + url + ", pagePath=" + pagePath);

		Page page = pageManager.getPage(pagePath);

		if (page == null) {
			logger.error("The resource at that path does not exist or is not adaptable to Page: " + pagePath);
			return new JsonObject();
		}

		JsonObject jsonObject = new JsonObject();

		Locale locale = page.getLanguage(false);
		String displayLanguage = locale.getDisplayLanguage();

		jsonObject.addProperty("site", getSite(predefinedSites, pagePath));
		jsonObject.addProperty("language", displayLanguage);
		jsonObject.addProperty("title", page.getTitle());
		jsonObject.addProperty("url", url);

		return jsonObject;
	}

	private static void buildResponse(SlingHttpServletResponse response, JsonObject jsonObject) throws IOException {
		String jsonObjectString = jsonObject.toString();
		InputStream stream = new ByteArrayInputStream(jsonObjectString.getBytes());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		OutputStream responseOutputStream = response.getOutputStream();

		int bytes;

		while ((bytes = stream.read()) != -1) {
			responseOutputStream.write(bytes);
		}
	}

	private String constructLanguageSwitchURL(Page page, String constructedPagePath){
		String domainName = contentPathToDomain.get(page.getAbsoluteParent(1).getPath());
		String URL = domainName + sanitizeURL(constructedPagePath) + StatoilConstants.Extensions.HTML_EXTENSION;
		return URL;
		
	}
	
	private String sanitizeURL(String pagePath){
		return pagePath.substring(StringUtils.ordinalIndexOf(pagePath, "/", 3), pagePath.length());
	}

	@Override
	protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
		try {
			resourceResolver = request.getResourceResolver();
			pageManager = resourceResolver.adaptTo(PageManager.class);
			List<LanguageSwitchSitesModel> predefinedSites = languageSwitchPredefinedSites.getPreDefinedSites();
			contentPathToDomain = predefinedSites.stream().collect(Collectors.toMap(LanguageSwitchSitesModel::getPath, LanguageSwitchSitesModel::getUrl));
			String urlParameter = request.getParameter("url");

			if (StringUtils.isBlank(urlParameter)) {
				logger.error("url is blank.");
				response.sendError(SlingHttpServletResponse.SC_BAD_REQUEST, "url is blank");
			}

			JsonObject jsonObject = new JsonObject();

			// String pagePath = LanguageResourceResolver.resolve(urlParameter);
			String mappedPath = resourceResolver.resolve(request, new URI(urlParameter).getPath()).getPath();
			JsonObject page = buildPage(predefinedSites, urlParameter, mappedPath);
			jsonObject.add("page", page);

			String serverName = request.getServerName();
			JsonArray sites = buildSites(serverName, predefinedSites, mappedPath);
			jsonObject.add("sites", sites);

			buildResponse(response, jsonObject);
		} catch (Exception e) {
			logger.error("Exception: ", e);
		}
	}

}
