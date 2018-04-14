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

@SlingServlet(paths = { "/services/ServiceNowStorageRequest" }, methods = { "POST" })
public class ServiceNowStorageRequestFormServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -447863407123742445L;
	private static final String catalog_identifier = "e2bf84fd4f93f200993042818110c746";

	private static final Logger logger = LoggerFactory.getLogger(ServiceNowStorageRequestFormServlet.class);

	@Reference
	private GoogleReCaptchaValidationService googleReCaptchaValidationService;

	@Reference
	private ServicenowEndPointConfiguration servicenowConfiguration;
	
	@Override
	protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		try {
			String contactPerson = URLEncoder.encode(request.getParameter("contactPerson"), "UTF-8");
			String company = URLEncoder.encode(request.getParameter("company"), "UTF-8");
			String email = URLEncoder.encode(request.getParameter("email"), "UTF-8");
			String phone = URLEncoder.encode(request.getParameter("phone"), "UTF-8");
			String street = URLEncoder.encode(request.getParameter("street"), "UTF-8");
			String postCode = URLEncoder.encode(request.getParameter("postcode"), "UTF-8");
			String town = URLEncoder.encode(request.getParameter("town"), "UTF-8");
			String transporter = URLEncoder.encode(request.getParameter("transporter"), "UTF-8");
			String volumeOfGas = URLEncoder.encode(request.getParameter("volume"), "UTF-8");
			String entryCapacity = URLEncoder.encode(request.getParameter("entryCapacity"), "UTF-8");
			String exitCapacity = URLEncoder.encode(request.getParameter("exitCapacity"), "UTF-8");
			String dateFrom = URLEncoder.encode(request.getParameter("dateFrom"), "UTF-8");
			String dateTo = URLEncoder.encode(request.getParameter("dateTo"), "UTF-8");
			String userToken = URLEncoder.encode(request.getParameter("g-recaptcha-response"), "UTF-8");

			if (GenericUtil.validateRecaptcha(userToken, googleReCaptchaValidationService)) {
				String urlString = servicenowConfiguration.getEnvironmentUrl() + "/api/stasa/statoildotcomproject/Storage/"
						+ servicenowConfiguration.getCatalogItem() + "/" + catalog_identifier + "?ContactPerson=" + contactPerson
						+ "&Company=" + company + "&Email=" + email + "&PhoneNumber=" + phone + "&Address=" + street
						+ "&Postcode=" + postCode + "&Town=" + town + "&Transporter=" + transporter + "&VolumeOfGas="
						+ volumeOfGas + "&EntryCapacity=" + entryCapacity + "&ExitCapacity=" + exitCapacity
						+ "&DateFrom=" + dateFrom + "&DateTo=" + dateTo;

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
