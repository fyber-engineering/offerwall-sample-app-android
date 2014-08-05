/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.currency;

import java.util.Calendar;
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
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

/**
 * <p>
 * Provides services to access SponsorPay's Virtual Currency Server.
 * </p>
 */
public class SPVirtualCurrencyConnector implements SPVCSResultListener {

	private static final int VCS_TIMEOUT = 15;

	private static final String TAG = "SPVirtualCurrencyConnector";
	
	private static final String URL_PARAM_VALUE_NO_TRANSACTION = "NO_TRANSACTION";

	private static Calendar nextRequestTimestamp;
	private static SPCurrencyServerReponse lastResponse;

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
	public SPVirtualCurrencyConnector(Context context, String credentialsToken,
			SPCurrencyServerListener currencyServerListener) {
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
		Calendar calendar = Calendar.getInstance();
		if (calendar.before(nextRequestTimestamp)) {
			SponsorPayLogger
					.d(TAG,	"The VCS was queried less than "+ VCS_TIMEOUT +"s ago.Replying with cached response");
			if (lastResponse != null) {
				onSPCurrencyServerResponseReceived(lastResponse);
			} else {
				mCurrencyServerListener
				.onSPCurrencyServerError(new SPCurrencyServerErrorResponse(
						SPCurrencyServerRequestErrorType.ERROR_OTHER,
						"blas", "blaaa"));
			}
			return;
		}
		calendar.add(Calendar.SECOND, VCS_TIMEOUT);
		nextRequestTimestamp = calendar; 
		if (StringUtils.nullOrEmpty(transactionId)) {
			transactionId = fetchLatestTransactionIdForCurrentAppAndUser();
		}
		
		mShouldShowNotification = showToastNotification;
		
		SPCurrencyServerRequester.requestCurrency(this, mCredentials,
				transactionId, mCustomParameters);
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
		Editor editor = prefs.edit();
		editor.putString(generatePreferencesLatestTransactionIdKey(mCredentials), 
						transactionId);
		editor.commit();
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
		String retval = prefs.getString(generatePreferencesLatestTransactionIdKey(credentials),
				URL_PARAM_VALUE_NO_TRANSACTION);
		return retval;
	}

	/**
	 * Saves the returned latest transaction id and shows the notification
	 * if required
	 */
	private void onDeltaOfCoinsResponse(SPCurrencyServerSuccesfulResponse response) {
		saveLatestTransactionIdForCurrentUser(response.getLatestTransactionId());
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
	
	private static String generatePreferencesLatestTransactionIdKey(SPCredentials credentials) {
		return STATE_LATEST_TRANSACTION_ID_KEY_PREFIX + credentials.getAppId()
				+ STATE_LATEST_TRANSACTION_ID_KEY_SEPARATOR + credentials.getUserId();
	}

	@Override
	public void onSPCurrencyServerResponseReceived(
			SPCurrencyServerReponse response) {
		if (response instanceof SPCurrencyServerSuccesfulResponse) {
			lastResponse = new SPCurrencyServerSuccesfulResponse(0, ((SPCurrencyServerSuccesfulResponse) response).getLatestTransactionId());
			onDeltaOfCoinsResponse((SPCurrencyServerSuccesfulResponse) response);
			mCurrencyServerListener.onSPCurrencyDeltaReceived((SPCurrencyServerSuccesfulResponse) response);
		} else {
			lastResponse = response;
			mCurrencyServerListener.onSPCurrencyServerError((SPCurrencyServerErrorResponse) response);
		}
	}
	
}
