package com.statoil.reinvent.servlets;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.services.GoogleReCaptchaValidationService;
import com.statoil.reinvent.services.ServicenowEndPointConfiguration;
import com.statoil.reinvent.utils.GenericUtil;

@SlingServlet(paths = { "/services/ServiceNowContactUs" }, methods = { "POST" })
public class ServiceNowContactUsServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 2160179683948175618L;

	private static final String catalog_identifier = "66f0ff89db2e2644ff6272dabf961945";

	private static final Logger logger = LoggerFactory.getLogger(ServiceNowContactUsServlet.class);

	@Reference
	private GoogleReCaptchaValidationService googleReCaptchaValidationService;
	
	@Reference
	private ServicenowEndPointConfiguration servicenowConfiguration;

	@Override
	protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		try {
			String email = URLEncoder.encode(request.getParameter("email"), "UTF-8");
			String category = URLEncoder.encode(request.getParameter("category"), "UTF-8");
			String howCanWeHelp = URLEncoder.encode(request.getParameter("message"), "UTF-8");
			String tryingToReach = URLEncoder.encode(request.getParameter("receiver"), "UTF-8");
			String name = URLEncoder.encode(request.getParameter("yourName"), "UTF-8");
			String userToken = URLEncoder.encode(request.getParameter("g-recaptcha-response"), "UTF-8");
			if (GenericUtil.validateRecaptcha(userToken, googleReCaptchaValidationService)) {
				String urlString = servicenowConfiguration.getEnvironmentUrl()
						+ "/api/stasa/statoildotcomproject/ContactUs/" + servicenowConfiguration.getCatalogItem() + "/"
						+ catalog_identifier + "?Email=" + email + "&Category=" + category + "&HowCanWeHelp="
						+ howCanWeHelp + "&TryingToReach=" + tryingToReach + "&Name=" + name;

				URL url = new URL(urlString);
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

				String basicAuth = "Basic " + servicenowConfiguration.getCredentials();
				httpURLConnection.setRequestProperty("Authorization", basicAuth);
				httpURLConnection.setRequestMethod("PUT");

				int responseCode = httpURLConnection.getResponseCode();
				response.setStatus(responseCode);
				logger.error("Response Code " + responseCode + " for " + urlString);
			} else {
				response.sendError(SlingHttpServletResponse.SC_FORBIDDEN, "reCaptcha is not validated");
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
