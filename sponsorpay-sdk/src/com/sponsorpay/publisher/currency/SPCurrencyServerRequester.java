/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.currency;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.publisher.currency.SPCurrencyServerRequester.SPCurrencyServerReponse;
import com.sponsorpay.utils.HttpResponseParser;
import com.sponsorpay.utils.SPHttpClient;
import com.sponsorpay.utils.SignatureTools;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;

/**
 * <p>
 * Requests and loads a resource using the HTTP GET method in the background. Will call the
 * {@link SPVCSResultListener} registered in the constructor in the same thread
 * which triggered the request / loading process. Uses the Android {@link AsyncTask} mechanism.
 * </p>
 */
public class SPCurrencyServerRequester extends AsyncTask<UrlBuilder, Void, SPCurrencyServerReponse> {

	/*
	 * VCS API Resource URLs.
	 */
	private static final String VCS_URL_KEY = "vcs";
	
	private static final String URL_PARAM_KEY_LAST_TRANSACTION_ID = "ltid";
	/*
	 * JSON keys used to enclose error information.
	 */
	private static final String ERROR_CODE_KEY = "code";
	private static final String ERROR_MESSAGE_KEY = "message";
	/*
	 * JSON keys used to enclose data from a successful response.
	 */
	private static final String DELTA_OF_COINS_KEY = "delta_of_coins";
	private static final String LATEST_TRANSACTION_ID_KEY = "latest_transaction_id";	

	public interface SPVCSResultListener {
		public void onSPCurrencyServerResponseReceived(SPCurrencyServerReponse response);
	}
	
	public interface SPCurrencyServerReponse {

	}
	
	public static void requestCurrency(SPVCSResultListener listener,
			SPCredentials credentials, String transactionId,
			Map<String, String> customParameters) {

		String baseUrl = SponsorPayBaseUrlProvider.getBaseUrl(VCS_URL_KEY);
		UrlBuilder urlBuilder = UrlBuilder.newBuilder(baseUrl, credentials)
				.addKeyValue(URL_PARAM_KEY_LAST_TRANSACTION_ID, transactionId)
				.addExtraKeysValues(customParameters)
				.addScreenMetrics()
				.addSignature();
		new SPCurrencyServerRequester(listener, credentials.getSecurityToken()).execute(urlBuilder);
	}


	public static String TAG = "SPCurrencyServerRequester";

	/**
	 * Key of the User-Agent header sent on background requests.
	 */
	private static String USER_AGENT_HEADER_NAME = "User-Agent";

	/**
	 * Key of the Accept-Language header sent on background requests.
	 */
	private static String ACCEPT_LANGUAGE_HEADER_NAME = "Accept-Language";

	/**
	 * Value of the User-Agent header sent on background requests.
	 */
	private static String USER_AGENT_HEADER_VALUE = "Android";

	/**
	 * Custom SponsorPay HTTP header containing the signature of the response.
	 */
	private static final String SIGNATURE_HEADER = "X-Sponsorpay-Response-Signature";


	/**
	 * Registered {@link AsyncRequestResultListener} to be notified of the request's results when
	 * they become available.
	 */
	private SPVCSResultListener mResultListener;

	private String mSecurityToken;



	/**
	 * 
	 * @param urlBuilder
	 *            the {@link UrlBuilder} that will be used for this request.
	 * @param listener
	 *            {@link AsyncRequestResultListener} to be notified of the request's results when
	 *            they become available.
	 * @param securityToken 
	 */
	private SPCurrencyServerRequester(SPVCSResultListener listener, String securityToken) {
		mResultListener = listener;
		mSecurityToken = securityToken;
	}

	/**
	 * Performs the request in the background. Called by the parent {@link AsyncTask} when
	 * {@link #requestCurrency(SPVCSResultListener, SPCredentials, String, Map)} is invoked.
	 * 
	 */
	@Override
	protected SPCurrencyServerReponse doInBackground(UrlBuilder... params) {
		Thread.currentThread().setName(TAG);
		
		String requestUrl = params[0].buildUrl();
		
		SponsorPayLogger.d(getClass().getSimpleName(), "Delta of coins request will be sent to URL + params: "
				+ requestUrl);
		
		HttpUriRequest request = new HttpGet(requestUrl);
		request.addHeader(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_VALUE);

		String acceptLanguageHeaderValue = makeAcceptLanguageHeaderValue();
		
		request.addHeader(ACCEPT_LANGUAGE_HEADER_NAME, acceptLanguageHeaderValue);

		HttpClient client = SPHttpClient.getHttpClient();
		
		try {
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			String responseBody = HttpResponseParser.extractResponseString(response);
			Header[] responseSignatureHeaders = response.getHeaders(SIGNATURE_HEADER);
			String responseSignature = responseSignatureHeaders.length > 0 ? responseSignatureHeaders[0]
					.getValue() : StringUtils.EMPTY_STRING;
					
			SponsorPayLogger.d(getClass().getSimpleName(), String.format(
					"Currency Server Response, status code: %d, response body: %s, signature: %s",
					statusCode, responseBody, responseSignature));
			
			return parseResponse(statusCode, responseBody, responseSignature);
		} catch (Throwable t) {
			SponsorPayLogger.e(TAG, "Exception triggered when executing request: " + t);

			SPCurrencyServerErrorResponse errorResponse = new SPCurrencyServerErrorResponse(
					SPCurrencyServerRequestErrorType.ERROR_NO_INTERNET_CONNECTION,
					null, t.getMessage());
			
			return errorResponse;
		}
	}
	
	/**
	 * Performs a second-stage error checking, parses the response and invokes the relevant method
	 * of the registered listener.
	 * @param statusCode 
	 * @param responseSignature 
	 * @param responseBody 
	 * 
	 * @param securityToken
	 *            Security token used to verify the authenticity of the response.
	 */
	private SPCurrencyServerReponse parseResponse(int statusCode, String responseBody, String responseSignature) {
		if (hasErrorStatusCode(statusCode)) {
			return parseErrorResponse(responseBody);
		} else if (!verifySignature(responseBody, responseSignature)) {
			return new SPCurrencyServerErrorResponse(
				SPCurrencyServerRequestErrorType.ERROR_INVALID_RESPONSE_SIGNATURE,
				null,
				"The signature received in the request did not match the expected one");
		} else {
			return parseSuccessfulResponse(responseBody);
		}
	}
	
	
	/**
	 * Parses a response containing a non-successful HTTP status code. Tries to extract the error
	 * code and error message from the response body.
	 * @param responseBody 
	 * @return 
	 */
	private SPCurrencyServerReponse parseErrorResponse(String responseBody) {
		String errorMessage;
		SPCurrencyServerRequestErrorType errorType;
		String errorCode = null;
		try {
			JSONObject jsonResponse = new JSONObject(responseBody);
			errorCode = jsonResponse.getString(ERROR_CODE_KEY);
			errorMessage = jsonResponse.getString(ERROR_MESSAGE_KEY);
			errorType = SPCurrencyServerRequestErrorType.SERVER_RETURNED_ERROR;
		} catch (Exception e) {
			SponsorPayLogger.w(getClass().getSimpleName(),
					"An exception was triggered while parsing error response", e);
			errorType = SPCurrencyServerRequestErrorType.ERROR_OTHER;
			errorMessage = e.getMessage();
		}
		return new SPCurrencyServerErrorResponse(errorType, errorCode, errorMessage);
	}
	
	private SPCurrencyServerReponse parseSuccessfulResponse(String responseBody) {
		try {
			JSONObject jsonResponse = new JSONObject(responseBody);
			double deltaOfCoins = jsonResponse.getDouble(DELTA_OF_COINS_KEY);
			String latestTransactionId = jsonResponse.getString(LATEST_TRANSACTION_ID_KEY);
			return new SPCurrencyServerSuccesfulResponse(deltaOfCoins, latestTransactionId);
		} catch (Exception e) {
			SPCurrencyServerRequestErrorType errorType = SPCurrencyServerRequestErrorType.ERROR_INVALID_RESPONSE;
			String errorMessage = e.getMessage();
			return new SPCurrencyServerErrorResponse(errorType, null, errorMessage);
		}
	}
	
	

	/**
	 * Verify calculate the signature of the response with the provided security token and compare
	 * it against the server-provided response signature.
	 * @param responseBody 
	 * @param responseSignature 
	 * 
	 * @param securityToken
	 *            Security token which will be used to calculate the signature.
	 * @return true if the calculated signature matches the server-provided signature. false
	 *         otherwise.
	 */
	private boolean verifySignature(String responseBody, String responseSignature) {
		String generatedSignature = SignatureTools.generateSignatureForString(responseBody,
				mSecurityToken);
		return generatedSignature.equals(responseSignature);
	}

	/**
	 * Returns true if the response contains an HTTP status code out of the 200s.
	 * @param responseStatusCode 
	 * 
	 * @return false if HTTP status code is between 200 and 299. True otherwise.
	 */
	private boolean hasErrorStatusCode(int responseStatusCode) {
		return responseStatusCode < 200 || responseStatusCode > 299;
	}
	
	/**
	 * Returns a value for the HTTP Accept-Language header based on the current locale set up for
	 * the device.
	 */
	private String makeAcceptLanguageHeaderValue() {
		String preferredLanguage = Locale.getDefault().getLanguage();

		String acceptLanguageLocaleValue = preferredLanguage;
		final String englishLanguageCode = Locale.ENGLISH.getLanguage();

		if (StringUtils.nullOrEmpty(preferredLanguage)) {
			acceptLanguageLocaleValue = englishLanguageCode;
		} else if (!englishLanguageCode.equals(preferredLanguage)) {
			acceptLanguageLocaleValue += String.format(", %s;q=0.8", englishLanguageCode);
		}
		return acceptLanguageLocaleValue;
	}

	/**
	 * Called in the original thread when a response from the server is available. Notifies the
	 * request result listener.
	 * 
	 * @param result
	 */
	@Override
	protected void onPostExecute(SPCurrencyServerReponse result) {
		mResultListener.onSPCurrencyServerResponseReceived(result);
	}

}
