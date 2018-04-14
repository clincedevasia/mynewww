package com.statoil.reinvent.servlets;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
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

@SlingServlet(paths = { "/services/ServiceNowCareerFair" }, methods = { "POST" })
public class ServiceNowCareerFairServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 4501340649538538630L;

	private static final String catalog_identifier = "b64f8cf9db692600ff6272dabf961904";

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
			String event = URLEncoder.encode(request.getParameter("event"), "UTF-8");
			String location = URLEncoder.encode(request.getParameter("location"), "UTF-8");
			String eventDescription = URLEncoder.encode(request.getParameter("eventDescription"), "UTF-8");
			String website = URLEncoder.encode(request.getParameter("website"), "UTF-8");
			String sendSupportingDocuments = StringUtils.isBlank(request.getParameter("supportingDocuments")) ? "No"
					: "Yes";
			String userToken = URLEncoder.encode(request.getParameter("g-recaptcha-response"), "UTF-8");
			if (GenericUtil.validateRecaptcha(userToken, googleReCaptchaValidationService)) {
				String urlString = servicenowConfiguration.getEnvironmentUrl()
						+ "/api/stasa/statoildotcomproject/CareerFairs/" + servicenowConfiguration.getCatalogItem() + "/"
						+ catalog_identifier
						+ String.format(
								"?SchoolOrganisation=%s&ContactPerson=%s&PhoneNumber=%s&Email=%s&Event=%s&Location=%s&DescriptionOfEvent=%s&LinkToWebsite=%s&SupportingDocuments=%s",
								groupOrganisation, contactPerson, phoneNumber, email, event, location, eventDescription,
								website, sendSupportingDocuments);

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