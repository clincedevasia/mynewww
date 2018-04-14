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

@SlingServlet(paths = { "/services/ServiceNowSponsorships" }, methods = { "POST" })
public class ServiceNowSponsorshipsServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 2335082849566847155L;

	private static final String catalog_identifier = "c9518fa8db52aec0a32950cbbf96199d";

	private static final Logger logger = LoggerFactory.getLogger(ServiceNowSponsorshipsServlet.class);

	@Reference
	private GoogleReCaptchaValidationService googleReCaptchaValidationService;
	
	@Reference
	private ServicenowEndPointConfiguration servicenowConfiguration;

	@Override
	protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		try {
			String groupOrganisation = URLEncoder.encode(request.getParameter("organisation"), "UTF-8");
			String contactPerson = URLEncoder.encode(request.getParameter("contactPerson"), "UTF-8");
			String phoneNumber = URLEncoder.encode(request.getParameter("phoneNumber"), "UTF-8");
			String email = URLEncoder.encode(request.getParameter("email"), "UTF-8");
			String category = URLEncoder.encode(request.getParameter("category"), "UTF-8");
			String location = URLEncoder.encode(request.getParameter("location"), "UTF-8");
			String yourApplication = URLEncoder.encode(request.getParameter("application"), "UTF-8");
			String userToken = URLEncoder.encode(request.getParameter("g-recaptcha-response"), "UTF-8");
			if (GenericUtil.validateRecaptcha(userToken, googleReCaptchaValidationService)) {
				String urlString = servicenowConfiguration.getEnvironmentUrl()
						+ "/api/stasa/statoildotcomproject/Sponsorships/" + servicenowConfiguration.getCatalogItem() + "/"
						+ catalog_identifier
						+ String.format(
								"?YourApplication=%s&SchoolOrganisation=%s&ContactPerson=%s&PhoneNumber=%s&Email=%s&Category=%s&Location=%s",
								yourApplication, groupOrganisation, contactPerson, phoneNumber, email, category,
								location);

				logger.error("urlString=" + urlString);

				URL url = new URL(urlString);
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				String basicAuth = "Basic " + servicenowConfiguration.getCredentials();
				httpURLConnection.setRequestProperty("Authorization", basicAuth);
				httpURLConnection.setRequestMethod("PUT");

				int responseCode = httpURLConnection.getResponseCode();
				response.setStatus(responseCode);
			} else {
				response.sendError(SlingHttpServletResponse.SC_FORBIDDEN, "reCaptcha is not validated");
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
