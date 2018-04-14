package com.statoil.reinvent.services;

import com.statoil.reinvent.models.ReCaptchaValidationResponse;

public interface GoogleReCaptchaValidationService {
	
	public ReCaptchaValidationResponse validateUserInput(String userResponseToken, String remoteUserIp);

	public String getApiClientSecret();
	
}
