package com.statoil.reinvent.services.impl;

import java.io.IOException;
import java.util.Dictionary;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.statoil.reinvent.models.ReCaptchaValidationResponse;
import com.statoil.reinvent.services.GoogleReCaptchaValidationService;

@Component(label = "Google reCaptcha Configuration", immediate = true, enabled = true, metatype = true, description = "This configuration is responsible for server side validation of reCaptcha")
@Service
@Properties({
		@Property(name = "google.recaptcha.api.request.url", label = "Recaptcha API request URL", description = "Provide reCaptcha api request URL which is used to validate from server side. For Example: 'https://www.google.com/recaptcha/api/siteverify' "),
		@Property(name = "google.recaptcha.client.secret", label = "Recaptcha Client Secret", description = "Provide reCaptcha client secret given by google service"),
		@Property(name = "google.recaptcha.server.secret", label = "Recaptcha Server Secret", description = "Provide reCaptcha server secret given by google service"), })
public class GoogleReCaptchaValidationServiceImpl implements GoogleReCaptchaValidationService {

	private static final Logger LOG = LoggerFactory.getLogger(GoogleReCaptchaValidationServiceImpl.class);
	private String apiRequestUrl;
	private String apiServerSecret;
	private String apiClientSecret;

	@Override
	public ReCaptchaValidationResponse validateUserInput(String userResponseToken, String remoteUserIp) {

		try {
			HttpClient client = new HttpClient();
			PostMethod method = new PostMethod(apiRequestUrl);
			NameValuePair[] nameValuePair = { new NameValuePair("secret", apiServerSecret),
					new NameValuePair("response", userResponseToken) };
			method.setRequestBody(nameValuePair);
			if (client.executeMethod(method) == HttpStatus.SC_OK) {
				String responseBody = method.getResponseBodyAsString();
				Gson gson = new Gson();
				return gson.fromJson(responseBody, ReCaptchaValidationResponse.class);
			} else {
				throw new IOException("Bad response from the server");
			}
		} catch (IOException e) {
			LOG.error("exception due to ", e);
		}
		return null;
	}

	@Activate
	protected void activate(ComponentContext ctx) {
		final Dictionary<?, ?> config = ctx.getProperties();
		apiServerSecret = PropertiesUtil.toString(config.get("google.recaptcha.server.secret"), null);
		apiClientSecret = PropertiesUtil.toString(config.get("google.recaptcha.client.secret"), null);
		apiRequestUrl = PropertiesUtil.toString(config.get("google.recaptcha.api.request.url"), null);
	}

	@Override
	public String getApiClientSecret() {
		return apiClientSecret;
	}
}
