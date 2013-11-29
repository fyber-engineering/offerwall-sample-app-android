/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.currency;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.publisher.currency.AsyncRequest.AsyncRequestResultListener;
import com.sponsorpay.sdk.android.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.android.utils.UrlBuilder;

/**
 * <p>
 * Provides services to access SponsorPay's Virtual Currency Server.
 * </p>
 */
public class VirtualCurrencyConnector implements AsyncRequestResultListener {

	/*
	 * VCS API Resource URLs.
	 */
	private static final String VCS_URL_KEY = "vcs";

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
	
	private static boolean showToastNotification = true;
	
	/**
	 * Boolean indicating if the toast notification 
	 * should be shown on a successful query .
	 */
	private boolean mShouldShowNotification;

	/**
	 * Custom currency name
	 */
	private String mCurrency;
	
	/**
	 * Credentials holding AppID, UserId and Security Token 
	 */
	protected SPCredentials mCredentials;
	
	/**
	 * Android application context.
	 */
	protected Context mContext;

	/**
	 * Map of custom key/values to add to the parameters on the requests.
	 */
	protected Map<String, String> mCustomParameters;
	
	protected SPCurrencyServerListener mUserListener;
	
	/**
	 * Initializes a new instance with the provided context and application data.
	 * 
	 * @param context
	 *            Android application context.
	 * @param credentialsToken
	 *            The token identifying the {@link SPCredentials} to be used.
	 * @param userListener
	 *            {@link SPCurrencyServerListener} registered by the developer code to be notified
	 *            of the result of requests to the Virtual Currency Server.
	 */
	public VirtualCurrencyConnector(Context context, String credentialsToken,
			SPCurrencyServerListener userListener) {
		mContext = context;
		mCredentials = SponsorPay.getCredentials(credentialsToken);
		mUserListener = userListener;
		
		if (StringUtils.nullOrEmpty(mCredentials.getSecurityToken())) {
			throw new IllegalArgumentException("Security token has not been set on the credentials");
		}
	}

	/**
	 * Sets a map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	public void setCustomParameters(Map<String, String> customParams) {
		mCustomParameters = customParams;
	}

	/**
	 * Sets the custom currency name
	 * @param currency
	 * 			the custom currency name
	 * @return
	 */
	public VirtualCurrencyConnector setCurrency(String currency) {
		mCurrency = currency;
		return this;
	}
	
	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for a given user since the last time this method was called. The response
	 * will be delivered to one of the registered listener's callback methods.
	 */
	public void fetchDeltaOfCoins() {
		fetchDeltaOfCoinsForCurrentUserSinceTransactionId(null);
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
		if (StringUtils.nullOrEmpty(transactionId)) {
			transactionId = fetchLatestTransactionIdForCurrentAppAndUser();
		}
		
		Map<String, String> extraKeysValues = new HashMap<String, String>();
		extraKeysValues.put(URL_PARAM_KEY_LAST_TRANSACTION_ID, transactionId);
		
		if (mCustomParameters != null) {
			extraKeysValues.putAll(mCustomParameters);
		}

		String baseUrl = SponsorPayBaseUrlProvider.getBaseUrl(VCS_URL_KEY);

		String requestUrl = UrlBuilder.newBuilder(baseUrl, mCredentials)
				.addExtraKeysValues(extraKeysValues).addScreenMetrics().addTimestamp()
				.buildUrl();

		SponsorPayLogger.d(getClass().getSimpleName(), "Delta of coins request will be sent to URL + params: "
				+ requestUrl);

		AsyncRequest requestTask = new AsyncRequest(requestUrl, this);
		
		mShouldShowNotification = showToastNotification;
		
		requestTask.execute();
	}

	/**
	 * Called by {@link AsyncRequest} when a response from the currency server is
	 * received. Performs the first stage of error handling and initializes the right kind of
	 * {@link CurrencyServerAbstractResponse}.
	 * 
	 * @param requestTask
	 *            The calling {@link AsyncRequest} with the response data.
	 */
	@Override
	public void onAsyncRequestComplete(AsyncRequest requestTask) {
		SponsorPayLogger.d(getClass().getSimpleName(), String.format(
				"Currency Server Response, status code: %d, response body: %s, signature: %s",
				requestTask.getHttpStatusCode(), requestTask.getResponseBody(), requestTask
						.getResponseSignature()));

		CurrencyServerAbstractResponse response;

		if (requestTask.didRequestThrowError()) {
			response = new RequestErrorResponse();
		} else {
			response = new CurrencyServerDeltaOfCoinsResponse(this);
			response.setResponseData(requestTask.getHttpStatusCode(),
					requestTask.getResponseBody(), requestTask.getResponseSignature());
		}

		response.setResponseListener(mUserListener);
		response.parseAndCallListener(mCredentials.getSecurityToken());
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
						generatePreferencesLatestTransactionIdKey(mCredentials.getAppId(), mCredentials.getUserId()), transactionId).commit();
	}

	/**
	 * Retrieves the saved latest known transaction ID for the current user from the publisher state
	 * preferences file.
	 * 
	 * @return The retrieved transaction ID or null.
	 */
	private String fetchLatestTransactionIdForCurrentAppAndUser() {
		return fetchLatestTransactionId(mContext, mCredentials.getCredentialsToken());
	}

	/**
	 * Retrieves the saved latest transaction ID for a given user from the publisher state
	 * preferences file.
	 * 
	 * @param context
	 *          Android application context.
	 * @param credentialsToken
	 * 			credentials token id 
	 * 
	 * @return The retrieved transaction ID or null.
	 */
	public static String fetchLatestTransactionId(Context context, String credentialsToken) {
		SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);
		SharedPreferences prefs = context.getSharedPreferences(
				SponsorPayPublisher.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		String retval = prefs.getString(generatePreferencesLatestTransactionIdKey(credentials.getAppId(), credentials.getUserId()),
				URL_PARAM_VALUE_NO_TRANSACTION);
		return retval;
	}

	/**
	 * Saves the returned latest transaction id and shows the notification
	 * if required
	 */
	public void onDeltaOfCoinsResponse(CurrencyServerDeltaOfCoinsResponse response) {
		saveLatestTransactionIdForCurrentUser(response.getLatestTransactionId());
		if (response.getDeltaOfCoins() > 0 && mShouldShowNotification) {
			String text = String
					.format(SponsorPayPublisher.getUIString(UIStringIdentifier.VCS_COINS_NOTIFICATION),
							response.getDeltaOfCoins(),
							StringUtils.notNullNorEmpty(mCurrency) ? mCurrency : 
								SponsorPayPublisher.getUIString(UIStringIdentifier.VCS_DEFAULT_CURRENCY));
			Toast.makeText(mContext, text,
					Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Indicates whether the toast notification should be shown after a successful query
	 * @param showNotification
	 */
	public static void shouldShowToastNotification(boolean showNotification) {
		showToastNotification = showNotification;
	}
	
	private static String generatePreferencesLatestTransactionIdKey(String appId, String userId) {
		return STATE_LATEST_TRANSACTION_ID_KEY_PREFIX + appId
				+ STATE_LATEST_TRANSACTION_ID_KEY_SEPARATOR + userId;
	}
	
}
