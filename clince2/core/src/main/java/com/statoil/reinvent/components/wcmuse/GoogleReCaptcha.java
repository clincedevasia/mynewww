package com.statoil.reinvent.components.wcmuse;

import java.util.Locale;

import com.statoil.reinvent.services.GoogleReCaptchaValidationService;

public class GoogleReCaptcha extends BaseComponent {

	private String reCaptchaApiClientSecret;
	private Locale language;

	@Override
	protected void activated() throws Exception {
		GoogleReCaptchaValidationService googleReCaptchaValidationService = slingScriptHelper
				.getService(GoogleReCaptchaValidationService.class);
		reCaptchaApiClientSecret = googleReCaptchaValidationService.getApiClientSecret();
		language = currentPage.getLanguage(true);		
	}
	
	public String getReCaptchaApiClientSecret() {
		return reCaptchaApiClientSecret;
	}

	public String getLanguage() {
		return language.getLanguage();
	}

}
