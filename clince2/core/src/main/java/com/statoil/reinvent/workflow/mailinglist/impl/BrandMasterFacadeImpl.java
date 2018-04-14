package com.statoil.reinvent.workflow.mailinglist.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.models.BrandMasterConfigurationModel;
import com.statoil.reinvent.workflow.mailinglist.BrandMasterConfiguration;
import com.statoil.reinvent.workflow.mailinglist.BrandMasterFacade;

@Component
@Service
public class BrandMasterFacadeImpl implements BrandMasterFacade {

	@Reference
	private BrandMasterConfiguration brandMasterConfiguration;

	private static final Logger logger = LoggerFactory.getLogger(BrandMasterFacadeImpl.class);

	private BrandMasterConfigurationModel brandMasterConfigurationModel;

	@Override
	public void distribute(Map<String, String> emailParameters) throws Exception {
		brandMasterConfigurationModel = brandMasterConfiguration.getBrandMasterConfigurations();
		disableCertificateValidation();

		SOAPConnectionFactory soapConnectionFactory;
		SOAPConnection soapConnection = null;

		try {
			LoginResult loginResult = authenticate();

			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();

			SOAPMessage request = createDistributeRequest(brandMasterConfigurationModel.getClientSecret(), loginResult,
					emailParameters);
			logger.error("Request: " + toString(request));

			SOAPMessage response = soapConnection.call(request, brandMasterConfigurationModel.getSubscriptionUrl());

			if (response.getSOAPBody().hasFault()) {
				throw new Exception(response.getSOAPBody().getFault().getFaultCode() + ": "
						+ response.getSOAPBody().getFault().getFaultString());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if (soapConnection != null) {
				try {
					soapConnection.close();
				} catch (SOAPException e) {
					logger.error(e.getMessage());
					throw e;
				}
			}
		}
	}

	@Override
	public void signup(Map<String, String> formParameters) throws Exception {
		brandMasterConfigurationModel = brandMasterConfiguration.getBrandMasterConfigurations();
		disableCertificateValidation();

		SOAPConnectionFactory soapConnectionFactory;
		SOAPConnection soapConnection = null;

		try {
			LoginResult loginResult = authenticate();

			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();

			SOAPMessage request = createSignupRequest(brandMasterConfigurationModel.getClientSecret(), loginResult,
					formParameters);
			logger.error("Request: " + toString(request));

			SOAPMessage response = soapConnection.call(request, brandMasterConfigurationModel.getSubscriptionUrl());

			if (response.getSOAPBody().hasFault()) {
				throw new Exception(response.getSOAPBody().getFault().getFaultCode() + ": "
						+ response.getSOAPBody().getFault().getFaultString());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if (soapConnection != null) {
				try {
					soapConnection.close();
				} catch (SOAPException e) {
					logger.error(e.getMessage());
					throw e;
				}
			}
		}
	}

	private void disableCertificateValidation() throws KeyManagementException, NoSuchAlgorithmException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	private String toString(SOAPMessage message) throws IOException, SOAPException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		message.writeTo(output);
		String messageString = new String(output.toByteArray());
		return messageString;
	}

	private LoginResult authenticate() throws Exception {
		SOAPConnectionFactory soapConnectionFactory;
		SOAPConnection soapConnection = null;

		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();

			SOAPMessage request = createAuthenticationRequest(brandMasterConfigurationModel);
			logger.error("Request: " + toString(request));

			SOAPMessage response = soapConnection.call(request, brandMasterConfigurationModel.getAuthenticationUrl());

			if (response.getSOAPBody().hasFault()) {
				throw new Exception(response.getSOAPBody().getFault().getFaultCode() + ": "
						+ response.getSOAPBody().getFault().getFaultString());
			}

			SOAPBody responseBody = response.getSOAPBody();
			String apiSecret = responseBody.getElementsByTagName("v1:apiSecret").item(0).getFirstChild().getNodeValue();
			String instId = responseBody.getElementsByTagName("v1:instId").item(0).getFirstChild().getNodeValue();

			return new LoginResult(apiSecret, instId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if (soapConnection != null) {
				try {
					soapConnection.close();
				} catch (SOAPException e) {
					logger.error(e.getMessage());
					throw e;
				}
			}
		}
	}

	private static SOAPMessage createAuthenticationRequest(BrandMasterConfigurationModel configuration)
			throws SOAPException, IOException {
		String string = String.format(
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><Authentication___Login xmlns=\"http://tempuri.org/\"><clientSecret>%s</clientSecret><userName>SUBSCRIPTIONAPI</userName><password>%s</password><comId>34</comId><ptlId>%s</ptlId><otyId>%s</otyId><laeId>1</laeId><apnId>%s</apnId></Authentication___Login></s:Body></s:Envelope>",
				configuration.getClientSecret(), configuration.getPassword(), configuration.getPtlId(),
				configuration.getOtyId(), configuration.getApnId());
		InputStream inputStream = new ByteArrayInputStream(string.getBytes());
		SOAPMessage message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)
				.createMessage((new MimeHeaders()), inputStream);
		return message;
	}

	private static SOAPMessage createDistributeRequest(String clientSecret, LoginResult loginResult,
			Map<String, String> emailParameters) throws SOAPException, IOException {
		String string = String.format(
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><Subscription___Distribute xmlns=\"http://tempuri.org/\"><clientSecret>%s</clientSecret><apiSecret>%s</apiSecret><instId>%s</instId><timeStamp>%s</timeStamp><Title>%s</Title><Ingress><![CDATA[%s]]></Ingress><newsURL>%s</newsURL><newsType>%s</newsType><language>%s</language><additionalParams/></Subscription___Distribute></s:Body></s:Envelope>",
				clientSecret, loginResult.getApiSecret(), loginResult.getInstId(), emailParameters.get("date"),
				emailParameters.get("title"), emailParameters.get("ingress"), emailParameters.get("link"),
				emailParameters.get("newsType"), emailParameters.get("languageCode"));
		InputStream inputStream = new ByteArrayInputStream(string.getBytes());
		SOAPMessage message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)
				.createMessage((new MimeHeaders()), inputStream);
		return message;
	}

	private static SOAPMessage createSignupRequest(String clientSecret, LoginResult loginResult,
			Map<String, String> formParameters) throws Exception {

		if (!formParameters.get("languageCode").equals("en") && !formParameters.get("languageCode").equals("no")) {
			throw new Exception(String.format("Invalid language code '%s'.", formParameters.get("languageCode")));
		}

		String additionalParameters = String.format(
				"{ \"stock_market\":\"%s\", \"company_news\":\"%s\", \"crude_oil_assays\":\"%s\", \"magazine\":\"%s\", \"lang\": \"%s\", \"type\": \"Investor\" }",
				formParameters.get("subscribeToStockMarketAnnouncements"), formParameters.get("subscribeToCompanyNews"),
				formParameters.get("subscribeToCrudeOilAssays"), formParameters.get("subscribeToMagazineArticles"),
				formParameters.get("languageCode"));
		String string = String.format(
				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><Subscription___SignUp xmlns=\"http://tempuri.org/\"><clientSecret>%s</clientSecret><apiSecret>%s</apiSecret><instId>%s</instId><firstName>%s</firstName><lastName>%s</lastName><email>%s</email><additionalParams>%s</additionalParams></Subscription___SignUp></s:Body></s:Envelope>",
				clientSecret, loginResult.getApiSecret(), loginResult.getInstId(), formParameters.get("firstName"),
				formParameters.get("lastName"), formParameters.get("email"), additionalParameters);
		InputStream inputStream = new ByteArrayInputStream(string.getBytes());
		SOAPMessage message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)
				.createMessage((new MimeHeaders()), inputStream);
		return message;
	}

}