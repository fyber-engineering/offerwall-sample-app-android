/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.currency;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.HttpResponseParser;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

/**
 * <p>
 * Provides services to access SponsorPay's Virtual Currency Server.
 * </p>
 */
public class VirtualCurrencyConnector implements SPCurrencyServerListener {

	/*
	 * VCS API Resource URLs.
	 */
	private static final String VIRTUAL_CURRENCY_SERVER_BASE_URL = "http://api.sponsorpay.com/vcs/v1/";
	private static final String CURRENCY_DELTA_REQUEST_RESOURCE = "new_credit.json";

	/*
	 * Parameter key and default values.
	 */
	private static final String URL_PARAM_KEY_LAST_TRANSACTION_ID = "ltid";
	private static final String URL_PARAM_VALUE_NO_TRANSACTION = "NO_TRANSACTION";
	private static final String URL_PARAM_KEY_TIMESTAMP = "timestamp";

	/**
	 * Key for the String containing the latest known transaction ID, which is saved as state in the Publisher SDK
	 * preferences file (whose name is defined in {@link SponsorPayPublisher#PREFERENCES_FILENAME}).
	 */
	private static final String STATE_LATEST_TRANSACTION_ID_KEY_PREFIX = "STATE_LATEST_CURRENCY_TRANSACTION_ID_";
	private static final String STATE_LATEST_TRANSACTION_ID_KEY_SEPARATOR = "_";
	
	/**
	 * Android application context.
	 */
	private Context mContext;

	/**
	 * ID of the user for whom the requests will be made.
	 */
	private String mUserId;

	/**
	 * {@link HostInfo} containing data about the host device and application, including its application ID.
	 */
	private HostInfo mHostInfo;

	/**
	 * Security token used to sign requests to the server and verify its responses.
	 */
	private String mSecurityToken;

	/**
	 * {@link SPCurrencyServerListener} registered by the developer code to be notified of the result of requests to the
	 * Virtual Currency Server.
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
	 * {@link AsyncTask} used to perform the HTTP requests on a background thread and be notified of its results on the
	 * calling thread.
	 */
	private class CurrencyServerRequestAsyncTask extends AsyncTask<Void, Void, Void> {
		/**
		 * Custom SponsorPay HTTP header containing the signature of the response.
		 */
		private static final String SIGNATURE_HEADER = "X-Sponsorpay-Response-Signature";

		/**
		 * Tightly coupled host {@link VirtualCurrencyConnector}.
		 */
		private VirtualCurrencyConnector mVcc;

		/**
		 * URL for the request which will be performed in the background.
		 */
		private String mRequestUrl;

		/**
		 * Type of the request which will be performed in the background.
		 */
		public RequestType requestType;

		/**
		 * Status code of the server's response.
		 */
		public int statusCode;

		/**
		 * Server's response body.
		 */
		public String responseBody;

		/**
		 * Server's response signature, extracted of the {@value #SIGNATURE_HEADER} header.
		 */
		public String signature;

		/**
		 * Whether the request triggered a local exception, usually denoting a network connectivity problem.
		 */
		public boolean didTriggerException;

		/**
		 * Initializes a new instance whose {@link #execute()} still needs to be invoked to trigger the request.
		 * 
		 * @param vcc
		 *            Host {@link VirtualCurrencyConnector}
		 * @param requestType
		 *            Type of the request to be performed. See {@link RequestType}.
		 * @param requestUrl
		 *            Url of the request to be performed.
		 */
		public CurrencyServerRequestAsyncTask(VirtualCurrencyConnector vcc, RequestType requestType, String requestUrl) {
			mVcc = vcc;
			mRequestUrl = requestUrl;
			CurrencyServerRequestAsyncTask.this.requestType = requestType;
		}

		/**
		 * Performs the request in the background. Called by the parent {@link AsyncTask} when {@link #execute(Void...)}
		 * is invoked.
		 * 
		 * @param
		 * @return
		 */
		@Override
		protected Void doInBackground(Void... params) {
			HttpUriRequest request = new HttpGet(mRequestUrl);
			HttpClient client = new DefaultHttpClient();

			didTriggerException = false;

			try {
				HttpResponse response = client.execute(request);
				statusCode = response.getStatusLine().getStatusCode();
				responseBody = HttpResponseParser.extractResponseString(response);
				Header[] responseSignatureHeaders = response.getHeaders(SIGNATURE_HEADER);
				signature = responseSignatureHeaders.length > 0 ? responseSignatureHeaders[0].getValue() : "";

			} catch (Exception e) {
				Log.e(CurrencyServerRequestAsyncTask.class.getSimpleName(),
						"Exception triggered when executing request: " + e);
				didTriggerException = true;
			}
			return null;
		}

		/**
		 * Called in the original thread when a response from the server is available. Notifies the host
		 * {@link VirtualCurrencyConnector}.
		 * 
		 * @param result
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mVcc.onCurrencyServerResponse(CurrencyServerRequestAsyncTask.this);
		}
	}

	/**
	 * Initializes a new instance with the provided context and application data.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            ID of the user for whom the requests will be made.
	 * @param listener
	 *            {@link SPCurrencyServerListener} registered by the developer code to be notified of the result of
	 *            requests to the Virtual Currency Server.
	 * @param hostInfo
	 *            {@link HostInfo} containing data about the host device and application, including its
	 *            application ID.
	 * @param securityToken
	 *            Security token used to sign requests to the server and verify its responses.
	 */
	public VirtualCurrencyConnector(Context context, String userId, SPCurrencyServerListener listener,
			HostInfo hostInfo, String securityToken) {
		mContext = context;
		mUserId = userId;
		mHostInfo = hostInfo;
		mSecurityToken = securityToken;
		mUserListener = listener;
	}

	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of virtual currency for a
	 * given user since the last time this method was called. The response will be delivered to one of the registered
	 * listener's callback methods.
	 */
	public void fetchDeltaOfCoins() {
		fetchDeltaOfCoinsForCurrentUserSinceTransactionId(fetchLatestTransactionIdForCurrentAppAndUser());
	}

	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of virtual currency for the
	 * current user's transactions newer than the one whose ID is passed. The response will be delivered to one of the
	 * registered listener's callback methods.
	 * 
	 * @param transactionId
	 *            The transaction ID used as excluded lower limit to calculate the delta of coins.
	 */
	public void fetchDeltaOfCoinsForCurrentUserSinceTransactionId(String transactionId) {

		String[] interstitialUrlExtraKeys = new String[] { URL_PARAM_KEY_LAST_TRANSACTION_ID, URL_PARAM_KEY_TIMESTAMP };
		String[] interstitialUrlExtraValues = new String[] { transactionId, getCurrentUnixTimestampAsString() };

		String requestUrl = UrlBuilder.buildUrl(VIRTUAL_CURRENCY_SERVER_BASE_URL + CURRENCY_DELTA_REQUEST_RESOURCE,
				mUserId, mHostInfo, interstitialUrlExtraKeys, interstitialUrlExtraValues, mSecurityToken);

		Log.d(VirtualCurrencyConnector.class.getSimpleName(), "Delta of coins request will be sent to URL + params: "
				+ requestUrl);

		CurrencyServerRequestAsyncTask requestTask = new CurrencyServerRequestAsyncTask(VirtualCurrencyConnector.this,
				RequestType.DELTA_COINS, requestUrl);

		requestTask.execute();
	}

	/**
	 * Called by {@link CurrencyServerRequestAsyncTask} when a response from the currency server is received. Performs
	 * the first stage of error handling and initializes the right kind of {@link CurrencyServerAbstractResponse}.
	 * 
	 * @param requestTask
	 *            The calling {@link CurrencyServerRequestAsyncTask} with the response data.
	 */
	private void onCurrencyServerResponse(CurrencyServerRequestAsyncTask requestTask) {
		Log.d(VirtualCurrencyConnector.class.getSimpleName(), String.format(
				"Currency Server Response, status code: %d, response body: %s, signature: %s", requestTask.statusCode,
				requestTask.responseBody, requestTask.signature));

		CurrencyServerAbstractResponse response;

		if (requestTask.didTriggerException) {
			response = new RequestErrorResponse();
		} else {
			response = CurrencyServerAbstractResponse.getParsingInstance(requestTask.requestType);
			response.setResponseData(requestTask.statusCode, requestTask.responseBody, requestTask.signature);
		}

		response.setResponseListener(this);
		response.parseAndCallListener(mSecurityToken);
	}

	/**
	 * Saves the provided transaction ID for the current user into the publisher state preferences file. Used to save
	 * the latest transaction id as returned by the server.
	 * 
	 * @param transactionId
	 *            The transaction ID to save.
	 */
	private void saveLatestTransactionIdForCurrentUser(String transactionId) {
		SharedPreferences prefs = mContext.getSharedPreferences(SponsorPayPublisher.PREFERENCES_FILENAME,
				Context.MODE_PRIVATE);
		prefs.edit().putString(generatePreferencesLatestTransactionIdKey(mUserId, mHostInfo.getAppId()), transactionId).commit();
	}

	private static String generatePreferencesLatestTransactionIdKey(String appId, String userId) {
		return STATE_LATEST_TRANSACTION_ID_KEY_PREFIX + appId + STATE_LATEST_TRANSACTION_ID_KEY_SEPARATOR + userId;
	}
	
	/**
	 * Retrieves the saved latest known transaction ID for the current user from the publisher state preferences file.
	 * 
	 * @return The retrieved transaction ID or null.
	 */
	private String fetchLatestTransactionIdForCurrentAppAndUser() {
		String retval = fetchLatestTransactionId(mContext, mHostInfo.getAppId(), mUserId);
		//Log.i(getClass().getSimpleName(), String.format("fetchLatestTransactionIdForCurrentAppAndUser will return %s", retval));
		return retval;
	}

	/**
	 * Retrieves the saved latest transaction ID for a given user from the publisher state preferences file.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            ID of the user related to which the fetched transaction ID must belong.
	 * @return The retrieved transaction ID or null.
	 */
	public static String fetchLatestTransactionId(Context context, String appId, String userId) {
		SharedPreferences prefs = context.getSharedPreferences(SponsorPayPublisher.PREFERENCES_FILENAME,
				Context.MODE_PRIVATE);
		String retval = prefs.getString(generatePreferencesLatestTransactionIdKey(userId, appId), URL_PARAM_VALUE_NO_TRANSACTION);
		//Log.i(VirtualCurrencyConnector.class.getSimpleName(), String.format("fetchLatestTransactionId(context, appId: %s, userId: %s) = %s", appId, userId, retval));
		return retval;
	}

	/**
	 * Gets the current UNIX timestamp (in seconds) for the outbound requests.
	 * 
	 * @return
	 */
	private static String getCurrentUnixTimestampAsString() {
		final int MILLISECONDS_IN_SECOND = 1000;
		return String.valueOf(System.currentTimeMillis() / MILLISECONDS_IN_SECOND);
	}

	/**
	 * Implemented from {@link SPCurrencyServerListener}. Forwards the call to the user listener.
	 */
	@Override
	public void onSPCurrencyServerError(CurrencyServerAbstractResponse response) {
		mUserListener.onSPCurrencyServerError(response);
	}

	/**
	 * Implemented from {@link SPCurrencyServerListener}. Saves the returned latest transaction id and forwards the call
	 * to the user listener.
	 */
	@Override
	public void onSPCurrencyDeltaReceived(CurrencyServerDeltaOfCoinsResponse response) {
		saveLatestTransactionIdForCurrentUser(response.getLatestTransactionId());
		mUserListener.onSPCurrencyDeltaReceived(response);
	}
}
