
package com.statoil.reinvent.models;

import java.util.List;

public class ReCaptchaValidationResponse {

	private Boolean success;

	private Integer challengeTs;

	private String hostname;

	private List<Object> errorCodes;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Integer getChallengeTs() {
		return challengeTs;
	}

	public void setChallengeTs(Integer challengeTs) {
		this.challengeTs = challengeTs;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public List<Object> getErrorCodes() {
		return errorCodes;
	}

	public void setErrorCodes(List<Object> errorCodes) {
		this.errorCodes = errorCodes;
	}

}