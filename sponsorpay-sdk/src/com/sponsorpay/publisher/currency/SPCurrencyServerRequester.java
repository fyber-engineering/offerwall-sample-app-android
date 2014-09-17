/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.currency;

import java.util.Map;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.publisher.currency.SPCurrencyServerRequester.SPCurrencyServerReponse;
import com.sponsorpay.utils.SignedResponseRequester;
import com.sponsorpay.utils.SignedServerResponse;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;

/**
 * <p>
 * Requests and loads a resource using the HTTP GET method in the background by using
 * the AsyncTaskRequester doInBackground method (Uses the Android {@link AsyncTask} 
 * mechanism.). Will call the {@link SPVCSResultListener} registered in the 
 * constructor in the same thread which triggered the request / loading process.
 * </p>
 */
public class SPCurrencyServerRequester extends SignedResponseRequester<SPCurrencyServerReponse> {

	/*
	 * VCS API Resource URLs.
	 */
	private static final String VCS_URL_KEY = "vcs";
	
	private static final String URL_PARAM_KEY_LAST_TRANSACTION_ID = "ltid";
	
	public static String TAG = "SPCurrencyServerRequester";
	
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
	private static final String CURRENCY_ID_KEY = "currency_id";
	private static final String CURRENCY_NAME_KEY = "currency_name";
	private static final String IS_DEFAULT_KEY = "is_default";

	public interface SPVCSResultListener {
		public void onSPCurrencyServerResponseReceived(SPCurrencyServerReponse response);
	}
	
	public interface SPCurrencyServerReponse {

	}
	
	public static void requestCurrency(SPVCSResultListener listener,
			SPCredentials credentials, String transactionId, String currencyId,
			Map<String, String> customParameters) {

		String baseUrl = SponsorPayBaseUrlProvider.getBaseUrl(VCS_URL_KEY);
		UrlBuilder urlBuilder = UrlBuilder.newBuilder(baseUrl, credentials)
				.addKeyValue(URL_PARAM_KEY_LAST_TRANSACTION_ID, transactionId)
				//.addKeyValue(CURRENCY_ID_KEY, currencyId)
				.addExtraKeysValues(customParameters)
				.addScreenMetrics()
				.addSignature();
		if(StringUtils.notNullNorEmpty(transactionId)){
			urlBuilder.addKeyValue(CURRENCY_ID_KEY, currencyId);
		}
		new SPCurrencyServerRequester(listener, credentials.getSecurityToken()).execute(urlBuilder);
	}


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
		SignedServerResponse signedServerResponse = new SignedServerResponse(statusCode, responseBody, responseSignature);
		
		if (hasErrorStatusCode(statusCode)) {
			return parseErrorResponse(responseBody);
		} else if (!verifySignature(signedServerResponse, mSecurityToken)) {
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
			SponsorPayLogger.w(TAG,
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
			String currencyId = jsonResponse.optString(CURRENCY_ID_KEY, "");
			String currencyName = jsonResponse.optString(CURRENCY_NAME_KEY, "");
			boolean idDefault= jsonResponse.optBoolean(IS_DEFAULT_KEY, false);
			
			return new SPCurrencyServerSuccessfulResponse(deltaOfCoins, latestTransactionId, currencyId, currencyName, idDefault);
		} catch (Exception e) {
			SPCurrencyServerRequestErrorType errorType = SPCurrencyServerRequestErrorType.ERROR_INVALID_RESPONSE;
			String errorMessage = e.getMessage();
			return new SPCurrencyServerErrorResponse(errorType, null, errorMessage);
		}
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

	@Override
	protected String getTag() {
		return TAG;
	}

	@Override
	protected SPCurrencyServerReponse parsedSignedResponse(
			SignedServerResponse signedServerResponse) {
		SPCurrencyServerReponse response = null; 
		
		if (signedServerResponse != null) {
			response = parseResponse(signedServerResponse.getStatusCode(),
					signedServerResponse.getResponseBody(), signedServerResponse.getResponseSignature());
		}

		if (response == null) {
			response = new SPCurrencyServerErrorResponse(
					SPCurrencyServerRequestErrorType.ERROR_OTHER,
					StringUtils.EMPTY_STRING, "Unknow error");
		}

		return response;
	}

	@Override
	protected SPCurrencyServerReponse noConnectionResponse(Throwable t) {
		return new SPCurrencyServerErrorResponse(
				SPCurrencyServerRequestErrorType.ERROR_NO_INTERNET_CONNECTION,
				null, t.getMessage());
	}

}
