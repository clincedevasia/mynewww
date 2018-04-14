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

@SlingServlet(paths = { "/services/ServiceNowOrderReports" }, methods = { "POST" })
public class ServiceNowOrderReportsServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -1942092454219106095L;

	private static final String catalog_identifier = "d1872741db26ea40977079e9bf961949";

	private static final Logger logger = LoggerFactory.getLogger(ServiceNowOrderReportsServlet.class);

	@Reference
	private GoogleReCaptchaValidationService googleReCaptchaValidationService;
	
	@Reference
	private ServicenowEndPointConfiguration servicenowConfiguration;

	@Override
	protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		try {
			String annualReport = StringUtils.isBlank(request.getParameter("annualReport")) ? "No"
					: request.getParameter("annualReport");
			String statutoryReport = StringUtils.isBlank(request.getParameter("statutoryReport")) ? "No"
					: request.getParameter("statutoryReport");
			String prospectusReport = StringUtils.isBlank(request.getParameter("prospectusReport")) ? "No"
					: request.getParameter("prospectusReport");
			String name = URLEncoder.encode(request.getParameter("yourName"), "UTF-8");
			String email = URLEncoder.encode(request.getParameter("email"), "UTF-8");
			String company = URLEncoder.encode(request.getParameter("company"), "UTF-8");
			String address = URLEncoder.encode(request.getParameter("address"), "UTF-8");
			String zip = URLEncoder.encode(request.getParameter("zip"), "UTF-8");
			String city = URLEncoder.encode(request.getParameter("city"), "UTF-8");
			String country = URLEncoder.encode(request.getParameter("country"), "UTF-8");
			String userToken = URLEncoder.encode(request.getParameter("g-recaptcha-response"), "UTF-8");
			if (GenericUtil.validateRecaptcha(userToken, googleReCaptchaValidationService)) {
				String urlString = servicenowConfiguration.getEnvironmentUrl()
						+ "/api/stasa/statoildotcomproject/OrderReports/" + servicenowConfiguration.getCatalogItem() + "/"
						+ catalog_identifier + "?Company=" + company + "&City=" + city + "&Address=" + address
						+ "&Name=" + name + "&ZIP=" + zip + "&StatutoryReport=" + statutoryReport + "&AnnualReport="
						+ annualReport + "&Country=" + country + "&Email=" + email + "&ProspectusReport="
						+ prospectusReport;

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
