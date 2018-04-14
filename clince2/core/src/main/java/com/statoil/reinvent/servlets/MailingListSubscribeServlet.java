package com.statoil.reinvent.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.services.GoogleReCaptchaValidationService;
import com.statoil.reinvent.utils.GenericUtil;
import com.statoil.reinvent.workflow.mailinglist.BrandMasterFacade;

@SlingServlet(paths = { "/services/MailingListSubscribe" }, methods = { "GET", "POST" })
public class MailingListSubscribeServlet extends SlingAllMethodsServlet implements OptingServlet {

	private static final long serialVersionUID = 7530054779489070755L;
	private static final Logger logger = LoggerFactory.getLogger(MailingListSubscribeServlet.class);

	@Reference
	private GoogleReCaptchaValidationService googleReCaptchaValidationService;
	
	@Reference
	private BrandMasterFacade brandMasterFacade;
	/**
	 * Creates lead and sends verification e-mail.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		try {
			String stockMarketAnnouncements = request.getParameter("stockMarketAnnouncements");
			String generalNews = request.getParameter("generalNews");
			String magazineArticles =  request.getParameter("magazineArticles");
			String crudeOilAssays = request.getParameter("crudeOilAssays");
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String email = request.getParameter("email");
			String language = request.getParameter("language");
			String userToken = URLEncoder.encode(request.getParameter("g-recaptcha-response"), "UTF-8");
			// Validates parameters.
			if (GenericUtil.validateRecaptcha(userToken, googleReCaptchaValidationService)) {
				boolean subscribeToStockMarketAnnouncements = StringUtils.isNotBlank(stockMarketAnnouncements)
						&& stockMarketAnnouncements.equals("on");
				boolean subscribeToCompanyNews = StringUtils.isNotBlank(generalNews) && generalNews.equals("on");
				boolean subscribeToCrudeOilAssays = StringUtils.isNotBlank(crudeOilAssays)
						&& crudeOilAssays.equals("on");
				boolean subscribeToMagazineArticles = StringUtils.isNotBlank(magazineArticles)
						&& magazineArticles.equals("on");

				if (!subscribeToStockMarketAnnouncements && !subscribeToCompanyNews && !subscribeToCrudeOilAssays &&!subscribeToMagazineArticles) {
					logger.error("At least one category required");
					response.sendError(SlingHttpServletResponse.SC_BAD_REQUEST, "At least one category required");
					return;
				}

				if (StringUtils.isBlank(firstName)) {
					logger.error("Given name cannot be blank");
					response.sendError(SlingHttpServletResponse.SC_BAD_REQUEST, "First name cannot be blank");
					return;
				}

				if (StringUtils.isBlank(lastName)) {
					response.sendError(SlingHttpServletResponse.SC_BAD_REQUEST, "Last name cannot be blank");
					return;
				}

				if (!isValidEmail(email)) {
					response.sendError(SlingHttpServletResponse.SC_BAD_REQUEST, "E-mail is not valid");
					return;
				}

				Map<String, String> formParameters = new HashMap<>();
				formParameters.put("languageCode", language);
				formParameters.put("firstName", firstName);
				formParameters.put("lastName", lastName);
				formParameters.put("email", email);

				formParameters.put("subscribeToStockMarketAnnouncements",
						subscribeToStockMarketAnnouncements ? "Y" : "N");
				formParameters.put("subscribeToCompanyNews", subscribeToCompanyNews ? "Y" : "N");
				formParameters.put("subscribeToCrudeOilAssays", subscribeToCrudeOilAssays ? "Y" : "N");
				formParameters.put("subscribeToMagazineArticles", subscribeToMagazineArticles ? "Y" : "N");
				
				brandMasterFacade.signup(formParameters);

				response.setStatus(HttpServletResponse.SC_OK);
				return;
			} else {
				response.sendError(SlingHttpServletResponse.SC_FORBIDDEN, "reCaptcha is not validated");
			}
		} catch (Exception e) {
			logger.error("Exception occurred", e);
			response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred");
		}
	}

	/** OptingServlet Acceptance Method **/

	@Override
	public final boolean accepts(SlingHttpServletRequest request) {
		/*
		 * Add logic which inspects the request which determines if this servlet
		 * should handle the request. This will only be executed if the Service
		 * Configuration's paths/resourcesTypes/selectors accept the request.
		 */
		return true;
	}

	/**
	 * Checks if valid e-mail address.
	 * <p>
	 * Based on recommendation from
	 * http://howtodoinjava.com/2014/11/11/java-regex-validate-email-address.
	 * Tried using EmailValidator found in Apache Commons, but that did not play
	 * well with AEM, so opted for regex.
	 *
	 * @param email
	 *            The e-mail to validate.
	 * @return true if the e-mail is valid. Otherwise false.
	 */
	private boolean isValidEmail(String email) {
		String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
