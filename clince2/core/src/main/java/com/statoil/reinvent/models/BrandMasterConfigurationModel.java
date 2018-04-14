package com.statoil.reinvent.models;

public class BrandMasterConfigurationModel {
	
	private  String clientSecret;
	private  String password;
	private  String apnId;
	private  String otyId;
	private  String ptlId;
	private  String authenticationUrl;
	private  String subscriptionUrl;
	
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getApnId() {
		return apnId;
	}
	public void setApnId(String apnId) {
		this.apnId = apnId;
	}
	public String getOtyId() {
		return otyId;
	}
	public void setOtyId(String otyId) {
		this.otyId = otyId;
	}
	public String getPtlId() {
		return ptlId;
	}
	public void setPtlId(String ptlId) {
		this.ptlId = ptlId;
	}
	public String getAuthenticationUrl() {
		return authenticationUrl;
	}
	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
	}
	public String getSubscriptionUrl() {
		return subscriptionUrl;
	}
	public void setSubscriptionUrl(String subscriptionUrl) {
		this.subscriptionUrl = subscriptionUrl;
	}

}
