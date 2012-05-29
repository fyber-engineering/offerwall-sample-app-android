/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.currency;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.AbstractConnector;
import com.sponsorpay.sdk.android.publisher.AsyncRequest;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;

/**
 * <p>
 * Provides services to access SponsorPay's Virtual Currency Server.
 * </p>
 */
public class VirtualCurrencyConnector extends AbstractConnector implements SPCurrencyServerListener {
	/*
	 * VCS API Resource URLs.
	 */
	private static final String VIRTUAL_CURRENCY_SERVER_STAGING_BASE_URL = "http://staging.iframe.sponsorpay.com/vcs/v1/";
	private static final String VIRTUAL_CURRENCY_SERVER_PRODUCTION_BASE_URL = "http://api.sponsorpay.com/vcs/v1/";
	private static final String CURRENCY_DELTA_REQUEST_RESOURCE = "new_credit.json";

	/*
	 * Parameter key and default values.
	 */
	private static final String URL_PARAM_KEY_LAST_TRANSACTION_ID = "ltid";
	private static final String URL_PARAM_VALUE_NO_TRANSACTION = "NO_TRANSACTION";

	/**
	 * Key for the String containing the latest known transaction ID, which is saved as state in the
	 * Publisher SDK preferences file (whose name is defined in
	 * {@link SponsorPayPublisher#PREFERENCES_FILENAME}).
	 */
	private static final String STATE_LATEST_TRANSACTION_ID_KEY_PREFIX = "STATE_LATEST_CURRENCY_TRANSACTION_ID_";
	private static final String STATE_LATEST_TRANSACTION_ID_KEY_SEPARATOR = "_";

	/**
	 * {@link SPCurrencyServerListener} registered by the developer's code to be notified of the
	 * result of requests to the Virtual Currency Server.
	 */
	private SPCurrencyServerListener mUserListener;

	/**
	 * Types of requests to be sent to the Virtual Currency Server.
	 * 
	 */
	public enum RequestType {
		DELTA_COINS
	}

	/**
	 * {@link AsyncTask} used to perform the HTTP requests on a background thread and be notified of
	 * its results on the calling thread.
	 */
	private class CurrencyServerRequestAsyncTask extends AsyncRequest {

		/**
		 * Type of the request which will be performed in the background.
		 */
		public RequestType requestType;

		/**
		 * Initializes a new instance whose {@link #execute()} still needs to be invoked to trigger
		 * the request.
		 * 
		 * @param requestType
		 *            Type of the request to be performed. See {@link RequestType}.
		 * @param requestUrl
		 *            Url of the request to be performed.
		 * @param listener
		 *            Listener which will be notified of the results of the request / response on
		 *            the thread which called {@link #execute()}.
		 */
		public CurrencyServerRequestAsyncTask(RequestType requestType, String requestUrl,
				AsyncRequestResultListener listener) {
			super(requestUrl, listener);
			CurrencyServerRequestAsyncTask.this.requestType = requestType;
		}
	}

	/**
	 * Initializes a new instance with the provided context and application data.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            ID of the user for whom the requests will be made.
	 * @param userListener
	 *            {@link SPCurrencyServerListener} registered by the developer code to be notified
	 *            of the result of requests to the Virtual Currency Server.
	 * @param hostInfo
	 *            {@link HostInfo} containing data about the host device and application, including
	 *            its application ID.
	 * @param securityToken
	 *            Security token used to sign requests to the server and verify its responses.
	 */
	public VirtualCurrencyConnector(Context context, String userId,
			SPCurrencyServerListener userListener, HostInfo hostInfo, String securityToken) {
		super(context, userId, hostInfo, securityToken);
		mUserListener = userListener;
	}

	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for a given user since the last time this method was called. The response
	 * will be delivered to one of the registered listener's callback methods.
	 */
	public void fetchDeltaOfCoins() {
		fetchDeltaOfCoinsForCurrentUserSinceTransactionId(fetchLatestTransactionIdForCurrentAppAndUser());
	}

	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for the current user's transactions newer than the one whose ID is passed.
	 * The response will be delivered to one of the registered listener's callback methods.
	 * 
	 * @param transactionId
	 *            The transaction ID used as excluded lower limit to calculate the delta of coins.
	 */
	public void fetchDeltaOfCoinsForCurrentUserSinceTransactionId(String transactionId) {

		String[] requestUrlExtraKeys = new String[] { URL_PARAM_KEY_LAST_TRANSACTION_ID,
				URL_PARAM_KEY_TIMESTAMP };
		String[] requestUrlExtraValues = new String[] { transactionId,
				getCurrentUnixTimestampAsString() };

		Map<String, String> extraKeysValues = UrlBuilder.mapKeysToValues(requestUrlExtraKeys,
				requestUrlExtraValues);

		if (mCustomParameters != null) {
			extraKeysValues.putAll(mCustomParameters);
		}

		String baseUrl = SponsorPayPublisher.shouldUseStagingUrls() ? VIRTUAL_CURRENCY_SERVER_STAGING_BASE_URL
				: VIRTUAL_CURRENCY_SERVER_PRODUCTION_BASE_URL;

		String requestUrl = UrlBuilder.newBuilder(baseUrl + CURRENCY_DELTA_REQUEST_RESOURCE,
				mHostInfo).setUserId(mUserId.toString()).addExtraKeysValues(extraKeysValues)
				.setSecretKey(mSecurityToken).buildUrl();

		Log.d(getClass().getSimpleName(), "Delta of coins request will be sent to URL + params: "
				+ requestUrl);

		CurrencyServerRequestAsyncTask requestTask = new CurrencyServerRequestAsyncTask(
				RequestType.DELTA_COINS, requestUrl, this);

		requestTask.execute();
	}

	/**
	 * Called by {@link CurrencyServerRequestAsyncTask} when a response from the currency server is
	 * received. Performs the first stage of error handling and initializes the right kind of
	 * {@link CurrencyServerAbstractResponse}.
	 * 
	 * @param requestTask
	 *            The calling {@link CurrencyServerRequestAsyncTask} with the response data.
	 */
	@Override
	public void onAsyncRequestComplete(AsyncRequest request) {
		CurrencyServerRequestAsyncTask requestTask = (CurrencyServerRequestAsyncTask) request;

		Log.d(getClass().getSimpleName(), String.format(
				"Currency Server Response, status code: %d, response body: %s, signature: %s",
				requestTask.getHttpStatusCode(), requestTask.getResponseBody(), requestTask
						.getResponseSignature()));

		CurrencyServerAbstractResponse response;

		if (requestTask.didRequestThrowError()) {
			response = new RequestErrorResponse();
		} else {
			response = CurrencyServerAbstractResponse.getParsingInstance(requestTask.requestType);
			response.setResponseData(requestTask.getHttpStatusCode(),
					requestTask.getResponseBody(), requestTask.getResponseSignature());
		}

		response.setResponseListener(this);
		response.parseAndCallListener(mSecurityToken);
	}

	/**
	 * Saves the provided transaction ID for the current user into the publisher state preferences
	 * file. Used to save the latest transaction id as returned by the server.
	 * 
	 * @param transactionId
	 *            The transaction ID to save.
	 */
	private void saveLatestTransactionIdForCurrentUser(String transactionId) {
		SharedPreferences prefs = mContext.getSharedPreferences(
				SponsorPayPublisher.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		prefs.edit()
				.putString(
						generatePreferencesLatestTransactionIdKey(mUserId.toString(), mHostInfo
								.getAppId()), transactionId).commit();
	}

	private static String generatePreferencesLatestTransactionIdKey(String appId, String userId) {
		return STATE_LATEST_TRANSACTION_ID_KEY_PREFIX + appId
				+ STATE_LATEST_TRANSACTION_ID_KEY_SEPARATOR + userId;
	}

	/**
	 * Retrieves the saved latest known transaction ID for the current user from the publisher state
	 * preferences file.
	 * 
	 * @return The retrieved transaction ID or null.
	 */
	private String fetchLatestTransactionIdForCurrentAppAndUser() {
		String retval = fetchLatestTransactionId(mContext, mHostInfo.getAppId(), mUserId.toString());
		// Log.i(getClass().getSimpleName(),
		// String.format("fetchLatestTransactionIdForCurrentAppAndUser will return %s", retval));
		return retval;
	}

	/**
	 * Retrieves the saved latest transaction ID for a given user from the publisher state
	 * preferences file.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            ID of the user related to which the fetched transaction ID must belong.
	 * @return The retrieved transaction ID or null.
	 */
	public static String fetchLatestTransactionId(Context context, String appId, String userId) {
		SharedPreferences prefs = context.getSharedPreferences(
				SponsorPayPublisher.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		String retval = prefs.getString(generatePreferencesLatestTransactionIdKey(userId, appId),
				URL_PARAM_VALUE_NO_TRANSACTION);
		// Log.i(VirtualCurrencyConnector.class.getSimpleName(),
		// String.format("fetchLatestTransactionId(context, appId: %s, userId: %s) = %s", appId,
		// userId, retval));
		return retval;
	}

	/**
	 * Implemented from {@link SPCurrencyServerListener}. Forwards the call to the user listener.
	 */
	@Override
	public void onSPCurrencyServerError(CurrencyServerAbstractResponse response) {
		mUserListener.onSPCurrencyServerError(response);
	}

	/**
	 * Implemented from {@link SPCurrencyServerListener}. Saves the returned latest transaction id
	 * and forwards the call to the user listener.
	 */
	@Override
	public void onSPCurrencyDeltaReceived(CurrencyServerDeltaOfCoinsResponse response) {
		saveLatestTransactionIdForCurrentUser(response.getLatestTransactionId());
		mUserListener.onSPCurrencyDeltaReceived(response);
	}
}
