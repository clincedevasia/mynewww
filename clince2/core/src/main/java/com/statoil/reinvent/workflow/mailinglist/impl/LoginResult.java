package com.statoil.reinvent.workflow.mailinglist.impl;

public class LoginResult {
    private String apiSecret;
    private String instId;

    public String getInstId() {
        return instId;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public LoginResult(String apiSecret, String instId) {
        this.apiSecret = apiSecret;
        this.instId = instId;
    }
}