/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.currency;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.publisher.currency.SPCurrencyServerRequester.SPCurrencyServerReponse;
import com.sponsorpay.publisher.currency.SPCurrencyServerRequester.SPVCSResultListener;
import com.sponsorpay.utils.HostInfo;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

/**
 * <p>
 * Provides services to access SponsorPay's Virtual Currency Server.
 * </p>
 */
public class SPVirtualCurrencyConnector implements SPVCSResultListener {

	private static final String TAG = "SPVirtualCurrencyConnector";

	public static final String CURRENT_API_LEVEL_NOT_SUPPORTED_ERROR = "Only devices running Android API level 10 and above are supported";

	private static final int VCS_TIMER = 15;
	
	private static final String URL_PARAM_VALUE_NO_TRANSACTION = "NO_TRANSACTION";

	private static HashMap<String, CacheInfo> cacheInfo = new HashMap<String, SPVirtualCurrencyConnector.CacheInfo>();

	/**
	 * Key for the String containing the latest known transaction ID, which is saved as state in the
	 * Publisher SDK preferences file (whose name is defined in
	 * {@link SponsorPayPublisher#PREFERENCES_FILENAME}).
	 */
	private static final String STATE_TRANSACTION_ID_KEY_PREFIX = "STATE_LATEST_CURRENCY_TRANSACTION_ID_";

	private static final String DEFAULT_CURRENCY_ID_KEY_PREFIX = "DEFAULT_CURRENCY_ID_KEY";
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
	
	protected SPCurrencyServerListener mCurrencyServerListener;
			
	/**
	 * Initializes a new instance with the provided context and application data.
	 * 
	 * @param context
	 *            Android application context.
	 * @param credentialsToken
	 *            The token identifying the {@link SPCredentials} to be used.
	 * @param currencyServerListener
	 *            {@link SPCurrencyServerListener} registered by the developer code to be notified
	 *            of the result of requests to the Virtual Currency Server.
	 */
	public SPVirtualCurrencyConnector(Context context, String credentialsToken, SPCurrencyServerListener currencyServerListener) {
		mCredentials = SponsorPay.getCredentials(credentialsToken);
		if (StringUtils.nullOrEmpty(mCredentials.getSecurityToken())) {
			throw new IllegalArgumentException("Security token has not been set on the credentials");
		}

		mContext = context;
		mCurrencyServerListener = currencyServerListener;
	}

	/**
	 * Sets a map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	public SPVirtualCurrencyConnector setCustomParameters(Map<String, String> customParams) {
		mCustomParameters = customParams;
		return this;
	}

	/**
	 * Sets the custom currency name
	 * @param currency
	 * 			the custom currency name
	 * @return
	 */
	public SPVirtualCurrencyConnector setCurrency(String currency) {
		mCurrency = currency;
		return this;
	}
	
	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for a given user since the last time this method was called. The response
	 * will be delivered to one of the registered listener's callback methods.
	 */
	public void fetchDeltaOfCoins() {
		fetchDeltaOfCoinsForCurrentUserSinceTransactionId(null, null);
	}

	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for the current user's transactions newer than the one whose ID is passed.
	 * The response will be delivered to one of the registered listener's callback methods.
	 * 
	 * @param storedResponse
	 *            The stored VCS response. Will be used as excluded lower limit to calculate the delta of coins.
	 */
	public void fetchDeltaOfCoinsForCurrentUserSinceTransactionId(String transactionId, String currencyId) {
		if (!HostInfo.isSupportedDevice()) {
			SPCurrencyServerErrorResponse errorResponse = new SPCurrencyServerErrorResponse(
					SPCurrencyServerRequestErrorType.ERROR_OTHER, "",
					CURRENT_API_LEVEL_NOT_SUPPORTED_ERROR);
			mCurrencyServerListener.onSPCurrencyServerError(errorResponse);
			return;
		}
		Calendar calendar = Calendar.getInstance();
		if (calendar.before(getCachedCalendar(calendar))) {
			SponsorPayLogger
					.d(TAG,	"The VCS was queried less than "+ VCS_TIMER +"s ago.Replying with cached response");
			SPCurrencyServerReponse response = getCachedResponse();
			if (response != null) {
				onSPCurrencyServerResponseReceived(response);
			} else {
				//this shouldn't occur, but still, we'll leave it there
				mCurrencyServerListener
				.onSPCurrencyServerError(new SPCurrencyServerErrorResponse(
						SPCurrencyServerRequestErrorType.ERROR_OTHER,
						StringUtils.EMPTY_STRING, "Unknown error"));
			}
			return;
		}

		calendar.add(Calendar.SECOND, VCS_TIMER);
        setTimerCalendar(calendar); 
		if (StringUtils.notNullNorEmpty(currencyId)) {
			transactionId = fetchLatestTransactionIdForCurrentAppAndUser(currencyId);
		}

		mShouldShowNotification = showToastNotification;

		SPCurrencyServerRequester.requestCurrency(this, mCredentials, transactionId, currencyId, mCustomParameters);
	}
	
	/**
	 * Saves the provided transaction ID for the current user into the publisher state preferences
	 * file. Used to save the latest transaction id as returned by the server.
	 * 
	 * @param transactionId
	 *            The transaction ID to save.
	 */
	private void saveLatestTransactionForCurrentUser(SPCurrencyServerSuccessfulResponse successfulResponse) {
		SharedPreferences prefs = mContext.getSharedPreferences(SponsorPayPublisher.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(STATE_TRANSACTION_ID_KEY_PREFIX + successfulResponse.getCurrencyId(), successfulResponse.getLatestTransactionId());
		
		if(successfulResponse.isDefault()){
			editor.putString(DEFAULT_CURRENCY_ID_KEY_PREFIX, STATE_TRANSACTION_ID_KEY_PREFIX + successfulResponse.getCurrencyId());
		}
	
		editor.commit();
	}

	/**
	 * Retrieves the saved latest known transaction ID for the current user from the publisher state
	 * preferences file.
	 * 
	 * @return The retrieved transaction ID or null.
	 */
	private String fetchLatestTransactionIdForCurrentAppAndUser(String vcsId) {
		return fetchLatestTransactionId(mContext, vcsId);
	}

	/**
	 * Retrieves the saved latest transaction ID for a given user from the publisher state
	 * preferences file.
	 * 
	 * @param context
	 *          Android application context.
	 * @param vcsId
	 * 			The ID of the requested  VCS.
	 * 
	 * @return The retrieved transaction ID or null.
	 */
	public static String fetchLatestTransactionId(Context context, String vcsId) {

		SharedPreferences prefs = context.getSharedPreferences(SponsorPayPublisher.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		String retval = prefs.getString(STATE_TRANSACTION_ID_KEY_PREFIX + vcsId, URL_PARAM_VALUE_NO_TRANSACTION);

		return retval;
	}

	/**
	 * Saves the returned latest transaction id and shows the notification
	 * if required
	 */
	private void onDeltaOfCoinsResponse(SPCurrencyServerSuccessfulResponse response) {
		saveLatestTransactionForCurrentUser(response);
		if (response.getDeltaOfCoins() > 0 && mShouldShowNotification) {
			String text = String
					.format(SponsorPayPublisher.getUIString(UIStringIdentifier.VCS_COINS_NOTIFICATION),
							response.getDeltaOfCoins(),
							StringUtils.notNullNorEmpty(mCurrency) ? mCurrency : 
								SponsorPayPublisher.getUIString(UIStringIdentifier.VCS_DEFAULT_CURRENCY));
			Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Indicates whether the toast notification should be shown after a successful query
	 * @param showNotification
	 */
	public static void shouldShowToastNotification(boolean showNotification) {
		showToastNotification = showNotification;
	}

	@Override
	public void onSPCurrencyServerResponseReceived(SPCurrencyServerReponse response) {
		if (response instanceof SPCurrencyServerSuccessfulResponse) {
			SPCurrencyServerSuccessfulResponse lastResponse;
			SPCurrencyServerSuccessfulResponse successfulResponse = (SPCurrencyServerSuccessfulResponse) response;
			lastResponse = new SPCurrencyServerSuccessfulResponse(0, successfulResponse.getLatestTransactionId(), successfulResponse.getCurrencyId(),
					successfulResponse.getCurrencyName(), successfulResponse.isDefault());
			setCachedResponse(lastResponse);
			onDeltaOfCoinsResponse(successfulResponse);
			mCurrencyServerListener.onSPCurrencyDeltaReceived(successfulResponse);
		} else {
			setCachedResponse(response);
			mCurrencyServerListener.onSPCurrencyServerError((SPCurrencyServerErrorResponse) response);
		}
	}
	
	//Helper methods

	private class CacheInfo {
		private Calendar calendar;
		private SPCurrencyServerReponse response;
	}
	
	private void setTimerCalendar(Calendar calendar) {
		CacheInfo pair = cacheInfo.get(mCredentials.getCredentialsToken());
		if (pair == null) {
			pair = new CacheInfo();
			cacheInfo.put(mCredentials.getCredentialsToken(), pair);
		}
		pair.calendar = calendar;
	}
	
	private void setCachedResponse(SPCurrencyServerReponse reponse) {
		CacheInfo pair = cacheInfo.get(mCredentials.getCredentialsToken());
		if (pair == null) {
			pair = new CacheInfo();
			cacheInfo.put(mCredentials.getCredentialsToken(), pair);
		}
		pair.response = reponse;
	}

	private Calendar getCachedCalendar(Calendar defaultIfNull) {
		CacheInfo pair= cacheInfo.get(mCredentials.getCredentialsToken());
		if (pair == null) {
			pair = new CacheInfo();
			pair.calendar = defaultIfNull;
			cacheInfo.put(mCredentials.getCredentialsToken(), pair);
		}
		return pair.calendar;
	}
	
	private SPCurrencyServerReponse getCachedResponse() {
		CacheInfo pair = cacheInfo.get(mCredentials.getCredentialsToken());
		if (pair == null) {
			pair = new CacheInfo();
			pair.calendar = Calendar.getInstance();
			cacheInfo.put(mCredentials.getCredentialsToken(), pair);
		}
		return pair.response;
	}

}
