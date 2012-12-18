/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.InterstitialLoader.InterstitialLoadingStatusListener;
import com.sponsorpay.sdk.android.publisher.OfferBanner.AdShape;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.currency.VirtualCurrencyConnector;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClient;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageRequest;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageRequestListener;
import com.sponsorpay.sdk.android.publisher.unlock.SPUnlockResponseListener;
import com.sponsorpay.sdk.android.publisher.unlock.SponsorPayUnlockConnector;
import com.sponsorpay.sdk.android.utils.SPIdException;
import com.sponsorpay.sdk.android.utils.SPIdValidator;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * Provides convenience calls to load and show the mobile Offer Wall and the mobile Interstitial.
 */
public class SponsorPayPublisher {
	public static final String PREFERENCES_FILENAME = "SponsorPayPublisherState";

	/**
	 * Enumeration identifying the different messages which can be displayed in the user interface.
	 */
	public enum UIStringIdentifier {
		ERROR_DIALOG_TITLE, DISMISS_ERROR_DIALOG, GENERIC_ERROR, 
		ERROR_LOADING_OFFERWALL, ERROR_LOADING_OFFERWALL_NO_INTERNET_CONNECTION, 
		LOADING_INTERSTITIAL, LOADING_OFFERWALL, ERROR_PLAY_STORE_UNAVAILABLE,
		MBE_REWARD_NOTIFICATION, VCS_COINS_NOTIFICATION, VCS_DEFAULT_CURRENCY, 
		MBE_ERROR_DIALOG_TITLE, MBE_ERROR_DIALOG_MESSAGE_DEFAULT, MBE_ERROR_DIALOG_MESSAGE_OFFLINE,
		MBE_ERROR_DIALOG_BUTTON_TITLE_DISMISS, MBE_FORFEIT_DIALOG_TITLE
	};


	
	/**
	 * Messages which can be displayed in the user interface.
	 */
	private static EnumMap<UIStringIdentifier, String> sUIStrings;

	/**
	 * Map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	private static Map<String, String> sCustomKeysValues;

	/**
	 * Default {@link AdShape} used to request Offer Banners to the backend.
	 */
	private static OfferBanner.AdShape sDefaultOfferBannerAdShape = OfferBanner.SP_AD_SHAPE_320X50;

	/**
	 * Fills {@link #sUIStrings} with the default messages.
	 */
	private static void initUIStrings() {
		sUIStrings = new EnumMap<UIStringIdentifier, String>(UIStringIdentifier.class);
		sUIStrings.put(UIStringIdentifier.ERROR_DIALOG_TITLE, "Error");
		sUIStrings.put(UIStringIdentifier.DISMISS_ERROR_DIALOG, "Dismiss");
		sUIStrings.put(UIStringIdentifier.GENERIC_ERROR,
				"An error happened when performing this operation");
		sUIStrings.put(UIStringIdentifier.ERROR_LOADING_OFFERWALL,
				"An error happened when loading the offer wall");
		sUIStrings.put(UIStringIdentifier.ERROR_LOADING_OFFERWALL_NO_INTERNET_CONNECTION,
				"An error happened when loading the offer wall (no internet connection)");
		sUIStrings.put(UIStringIdentifier.LOADING_INTERSTITIAL, "Loading...");
		sUIStrings.put(UIStringIdentifier.LOADING_OFFERWALL, "Loading...");
		sUIStrings.put(UIStringIdentifier.ERROR_PLAY_STORE_UNAVAILABLE, "You don't have the Google Play Store application on your device to complete App Install offers.");
		sUIStrings.put(UIStringIdentifier.MBE_REWARD_NOTIFICATION, "Thanks! Your reward will be payed out shortly");
		sUIStrings.put(UIStringIdentifier.VCS_COINS_NOTIFICATION,"Congratulations! You've earned %.0f %s!");
		sUIStrings.put(UIStringIdentifier.VCS_DEFAULT_CURRENCY, "coins");
		
		sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_TITLE, "Error");
		sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT, "We're sorry, something went wrong. Please try again.");
		sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_OFFLINE, "Your Internet connection has been lost. Please try again later.");
		sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_BUTTON_TITLE_DISMISS, "Dismiss");
		sUIStrings.put(UIStringIdentifier.MBE_FORFEIT_DIALOG_TITLE, "");
	}

	/**
	 * Gets a particular UI message identified by a {@link UIStringIdentifier}.
	 * 
	 * @param identifier
	 *            The identifier of the message to get.
	 * @return The message string.
	 */
	public static String getUIString(UIStringIdentifier identifier) {
		if (sUIStrings == null) {
			initUIStrings();
		}

		return sUIStrings.get(identifier);
	}

	/**
	 * Replaces one of the UI messages with a custom text.
	 * 
	 * @param identifier
	 *            The identifier of the message to set.
	 * @param message
	 *            Custom text for the message.
	 */
	public static void setCustomUIString(UIStringIdentifier identifier, String message) {
		if (sUIStrings == null) {
			initUIStrings();
		}

		sUIStrings.put(identifier, message);
	}

	/**
	 * Replaces one or several of the UI messages at once.
	 * 
	 * @param messages
	 *            An EnumMap mapping {@link UIStringIdentifier}s to the respective desired texts.
	 */
	public static void setCustomUIStrings(EnumMap<UIStringIdentifier, String> messages) {
		for (UIStringIdentifier condition : UIStringIdentifier.values()) {
			if (messages.containsKey(condition)) {
				setCustomUIString(condition, messages.get(condition));
			}
		}
	}

	/**
	 * Replaces one of the UI messages with the text identified by an Android String resource id.
	 * 
	 * @param identifier
	 *            The {@link UIStringIdentifier} of the message to replace.
	 * @param message
	 *            An Android String resource identifier.
	 * @param context
	 *            An Android context used to fetch the resource
	 */
	public static void setCustomUIString(UIStringIdentifier identifier, int message, Context context) {
		setCustomUIString(identifier, context.getString(message));
	}

	public static void setCustomUIStrings(EnumMap<UIStringIdentifier, Integer> messages,
			Context context) {
		for (UIStringIdentifier condition : UIStringIdentifier.values()) {
			if (messages.containsKey(condition)) {
				setCustomUIString(condition, messages.get(condition), context);
			}
		}
	}

	/**
	 * Sets a map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	public static void setCustomParameters(Map<String, String> params) {
		sCustomKeysValues = params;
	}

	/**
	 * Sets a map of custom key/values to add to the parameters on the requests to the REST API.
	 * 
	 * @param keys
	 * @param values
	 */
	public static void setCustomParameters(String[] keys, String[] values) {
		sCustomKeysValues = UrlBuilder.mapKeysToValues(keys, values);
	}

	/**
	 * Clears the map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	public static void clearCustomParameters() {
		sCustomKeysValues = null;
	}

	/**
	 * Returns the map of custom key/values to add to the parameters on the requests to the REST
	 * API.
	 * 
	 * @param
	 * @return If passedParameters is not null, a copy of it is returned. Otherwise if the
	 *         parameters set with {@link #setCustomParameters(Map)} or
	 *         {@link #setCustomParameters(String[], String[])} are not null, a copy of that map is
	 *         returned. Otherwise null is returned.
	 */
	private static HashMap<String, String> getCustomParameters(Map<String, String> passedParameters) {
		HashMap<String, String> retval;

		if (passedParameters != null)
			retval = new HashMap<String, String>(passedParameters);
		else if (sCustomKeysValues != null)
			retval = new HashMap<String, String>(sCustomKeysValues);
		else {
			retval = null;
		}

		return retval;
	}

	private static boolean sShouldUseStagingUrls = false;

	public static void setShouldUseStagingUrls(boolean value) {
		sShouldUseStagingUrls = value;
	}

	public static boolean shouldUseStagingUrls() {
		return sShouldUseStagingUrls;
	}

	private static String sOverridingWebViewUrl;
	
	public static void setOverridingWebViewUrl(String url) {
		sOverridingWebViewUrl = url;
	}
	
	/**
	 * The default request code needed for starting the Offer Wall activity.
	 */
	public static final int DEFAULT_OFFERWALL_REQUEST_CODE = 0xFF;

	/**
	 * The default request code needed for starting the Unlock Offer Wall activity.
	 */
	public static final int DEFAULT_UNLOCK_OFFERWALL_REQUEST_CODE = 0xFE;

	
	/**
	 * Sets the provided cookie strings into the application's cookie manager for the given base
	 * URL.
	 * 
	 * @param cookies
	 *            An array of cookie strings.
	 * @param baseUrl
	 *            The base URL to set the cookies for.
	 * @param context
	 *            Android application context.
	 */
	public static void setCookiesIntoCookieManagerInstance(String[] cookies, String baseUrl,
			Context context) {
		if (cookies == null || cookies.length == 0) {
			return;
		}

		CookieManager instance;

		// CookieSyncManager.getInstance() has to be called before we get CookieManager's
		try {
			CookieSyncManager.getInstance();
		} catch (IllegalStateException e) {
			CookieSyncManager.createInstance(context);
		}

		instance = CookieManager.getInstance();

		SponsorPayLogger.v(AsyncRequest.LOG_TAG, "Setting the following cookies into CookieManager instance "
				+ instance + " for base URL " + baseUrl + ": ");

		for (String cookieString : cookies) {
			instance.setCookie(baseUrl, cookieString);
			SponsorPayLogger.v(AsyncRequest.LOG_TAG, cookieString);
		}
	}

	/**
	 * Converts device pixels into screen pixels.
	 */
	public static int convertDevicePixelsIntoPixelsMeasurement(float dps, Context context) {
		final float scale = context.getResources().getDisplayMetrics().density;
		int pixels = (int) (dps * scale + 0.5f);
		return pixels;
	}
	
	//================================================================================
	// OfferWall
	//================================================================================
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the publisher application id and user id stored in the current credentials.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 */
	public static Intent getIntentForOfferWallActivity(Context context,	Boolean shouldStayOpen) {
		String credentialsToken =  SponsorPay.getCurrentCredentials().getCredentialsToken();
		return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, null, null);
	}

	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the publisher application id and user id stored in the current credentials.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	 
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 */
	public static Intent getIntentForOfferWallActivity(Context context, Boolean shouldStayOpen,
			String currencyName, HashMap<String, String> customParams) {
		String credentialsToken =  SponsorPay.getCurrentCredentials().getCredentialsToken();
		return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, currencyName, customParams);
	}
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the provided publisher application id and user id stored in the credentials identified 
	 * by the token id.
	 * </p>
	 * 
	 * @param credentialsToken
	 *            The token id of the credentials to be used.
	 * @param context
	 *            The publisher application context.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	 
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 */
	public static Intent getIntentForOfferWallActivity(String credentialsToken, Context context,
			Boolean shouldStayOpen, String currencyName, HashMap<String, String> customParams) {

		SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);


		Intent intent = new Intent(context, OfferWallActivity.class);
		intent.putExtra(OfferWallActivity.EXTRA_CREDENTIALS_TOKEN_KEY, credentials.getCredentialsToken());

		if (shouldStayOpen != null) {
			intent.putExtra(OfferWallActivity.EXTRA_SHOULD_STAY_OPEN_KEY, shouldStayOpen);
		}

		if (StringUtils.notNullNorEmpty(currencyName)) {
			intent.putExtra(OfferWallActivity.EXTRA_CURRENCY_NAME_KEY, currencyName);
		}

		if (sOverridingWebViewUrl != null) {
			intent.putExtra(OfferWallActivity.EXTRA_OVERRIDING_URL_KEY, sOverridingWebViewUrl);
		}

		intent.putExtra(OfferWallActivity.EXTRA_KEYS_VALUES_MAP_KEY,
				getCustomParameters(customParams));

		return intent;
	}

	//================================================================================
	// Unlock OfferWall
	//================================================================================
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the Unlock {@link OfferWallActivity}. Let the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the current credentials or throw an exception if none exists yet.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param unlockItemId
	 * 			  The Id of the item to be used to show offer for unlocking.
	 * @param unlockItemName
	 * 			  An item name to override the default one set on the server
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 */
	public static Intent getIntentForUnlockOfferWallActivity(Context context,
			String unlockItemId, String unlockItemName) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		return getIntentForUnlockOfferWallActivity(credentialsToken, context,
				unlockItemId, unlockItemName, null);
	}
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the Unlock {@link OfferWallActivity}. Let the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * @param credentialsToken
	 * 			  the id of the credentials hat will be used
	 * @param context
	 *            The publisher application context.
	 * @param unlockItemId
	 * 			  The Id of the item to be used to show offer for unlocking.
	 * @param unlockItemName
	 * 			  An item name to override the default one set on the server
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 */
	public static Intent getIntentForUnlockOfferWallActivity(String credentialsToken, Context context,
			String unlockItemId, String unlockItemName,	HashMap<String, String> customParams) {
		
		try {
			SPIdValidator.validate(unlockItemId);
		} catch (SPIdException e) {
			throw new RuntimeException("The provided Unlock Item ID is not valid. "
					+ e.getLocalizedMessage());
		}
		SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);

		Intent intent = new Intent(context, OfferWallActivity.class);
		intent.putExtra(OfferWallActivity.EXTRA_CREDENTIALS_TOKEN_KEY, credentials.getCredentialsToken());

		intent.putExtra(OfferWallActivity.EXTRA_OFFERWALL_TYPE,
				OfferWallActivity.OFFERWALL_TYPE_UNLOCK);
		intent.putExtra(OfferWallActivity.UnlockOfferWallTemplate.EXTRA_UNLOCK_ITEM_ID_KEY,
				unlockItemId);
		intent.putExtra(OfferWallActivity.UnlockOfferWallTemplate.EXTRA_UNLOCK_ITEM_NAME_KEY,
				unlockItemName);

		if (sOverridingWebViewUrl != null) {
			intent.putExtra(OfferWallActivity.EXTRA_OVERRIDING_URL_KEY, sOverridingWebViewUrl);
		}
		
		intent.putExtra(OfferWallActivity.EXTRA_KEYS_VALUES_MAP_KEY, getCustomParameters(customParams));

		return intent;
	}

	//================================================================================
	// Interstitial
	//================================================================================
	
	/**
	 * Starts the mobile interstitial request / loading / showing process using the current
	 * credentials.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 */
	public static void loadShowInterstitial(Activity callingActivity,  
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		loadShowInterstitial(credentialsToken , callingActivity, loadingStatusListener, shouldStayOpen,
				null, null,0, null, null);
	}
	
	/**
	 * Starts the mobile interstitial request / loading / showing process using the current
	 * credentials.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param loadingTimeoutSecs
	 *            Sets the maximum amount of time the interstitial should take to load. If you set
	 *            it to 0 or a negative number, it will fall back to the default value of 5 seconds.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void loadShowInterstitial(Activity callingActivity,  
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName, int loadingTimeoutSecs, String currencyName,
			Map<String, String> customParams) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		loadShowInterstitial(credentialsToken, callingActivity,
				loadingStatusListener, shouldStayOpen, backgroundUrl, skinName,
				loadingTimeoutSecs, currencyName, customParams);
	}
	
	/**
	 * Starts the mobile interstitial request / loading / showing process.
	 * 
	 * @param credentialsToken
	 *            The token id of the credentials to be used.
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param loadingTimeoutSecs
	 *            Sets the maximum amount of time the interstitial should take to load. If you set
	 *            it to 0 or a negative number, it will fall back to the default value of 5 seconds.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void loadShowInterstitial(String credentialsToken, Activity callingActivity,  
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName, int loadingTimeoutSecs, String currencyName,
			Map<String, String> customParams) {

		InterstitialLoader il = new InterstitialLoader(callingActivity, credentialsToken,
				loadingStatusListener);

		if (shouldStayOpen != null) {
			il.setShouldStayOpen(shouldStayOpen);
		}
		if (StringUtils.notNullNorEmpty(backgroundUrl)) {
			il.setBackgroundUrl(backgroundUrl);
		}
		if (StringUtils.notNullNorEmpty(skinName)) {
			il.setSkinName(skinName);
		}
		if (loadingTimeoutSecs > 0) {
			il.setLoadingTimeoutSecs(loadingTimeoutSecs);
		}
		if (StringUtils.notNullNorEmpty(currencyName)) {
			il.setCurrencyName(currencyName);
		}
		Map<String, String> extraParams = getCustomParameters(customParams);

		if (extraParams != null) {
			il.setCustomParameters(extraParams);
		}

		if (sOverridingWebViewUrl != null) {
			il.setOverridingUrl(sOverridingWebViewUrl);
		}
		
		il.startLoading();
	}

	//================================================================================
	// VCS
	//================================================================================
	
	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for a given user. Returns immediately, and the answer is delivered to one of
	 * the provided listener's callback methods. See {@link SPCurrencyServerListener}. The current 
	 * credentials will be used to get the application id and user id.
	 * 
	 * @param context
	 *            Android application context.
	 * @param listener
	 *            {@link SPCurrencyServerListener} which will be notified of the result of the
	 *            request.
	 */
	public static void requestNewCoins(Context context, SPCurrencyServerListener listener) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		requestNewCoins(credentialsToken, context, listener, null, null);
	}
	
	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for a given user. Returns immediately, and the answer is delivered to one of
	 * the provided listener's callback methods. See {@link SPCurrencyServerListener}. It will use the 
	 * credentials identified by the provided token id.
	 * 
	 * @param credentialsToken
	 *            The token id of the credentials to be used.
	 * @param context
	 *            Android application context.
	 * @param listener
	 *            {@link SPCurrencyServerListener} which will be notified of the result of the
	 *            request.
	 * @param transactionId
	 *            Optionally, provide the ID of the latest known transaction. The delta of coins
	 *            will be calculated from this transaction (not included) up to the present. Leave
	 *            it to null to let the SDK use the latest transaction ID it kept track of.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void requestNewCoins(String credentialsToken, Context context, 
			SPCurrencyServerListener listener, String transactionId, Map<String, String> customParams) {
		
		VirtualCurrencyConnector vcc = new VirtualCurrencyConnector(context, credentialsToken, listener);
		vcc.setCustomParameters(getCustomParameters(customParams));
		vcc.fetchDeltaOfCoinsForCurrentUserSinceTransactionId(transactionId);
	}
	
	//FIXME add documentation
	public static void displayNotificationForSuccessfullCoinRequest(boolean shouldShowNotification) {
//		displayNotificationForSuccessfullCoinRequest(shouldShowNotification);
		VirtualCurrencyConnector.shouldShowToastNotification(shouldShowNotification);
	}
	
//	//FIXME add documentation
//	public static void displayNotificationForSuccessfullCoinRequest(boolean shouldShowNotification, 
//			SPCurrencyRoundingMode roundingMode) {
//		VirtualCurrencyConnector.setRoundingMode(roundingMode);
//		VirtualCurrencyConnector.shouldShowToastNotification(shouldShowNotification);
//	}
	
	//================================================================================
	// Unlock Items
	//================================================================================
	
	/**
	 * <p>
	 * Requests the status of the "Unlockable" Items to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * </p>
	 * 
	 * <p>
	 * This method will use the current credentials or throw a {@link RuntimeException} if there's none.
	 * </p>
	 * 
	 * @param context
	 *            Android application context.
	 * @param listener
	 *            {@link SPUnlockResponseListener} which will be notified of the results of the
	 *            request.
	 */
	public static void requestUnlockItemsStatus(Context context, SPUnlockResponseListener listener) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		requestUnlockItemsStatus(credentialsToken, context, listener, null);
	}
	
	/**
	 * <p>
	 * Requests the status of the "Unlockable" Items to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * </p>
	 * 
	 * @param credentialsToken
	 *            The token id of the credentials to be used.
	 * @param context
	 *            Android application context.
	 * @param listener
	 *            {@link SPUnlockResponseListener} which will be notified of the results of the
	 *            request.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void requestUnlockItemsStatus(String credentialsToken, Context context,
			SPUnlockResponseListener listener, Map<String, String> customParams) {
		
		SponsorPayUnlockConnector uc = new SponsorPayUnlockConnector(context, credentialsToken, listener);
		
		uc.setCustomParameters(getCustomParameters(customParams));
		
		uc.fetchItemsStatus();
	}

	//================================================================================
	// Offer Banner
	//================================================================================
	
	/**
	 * <p>
	 * Requests an Offer Banner to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * </p>
	 * 
	 * <p>
	 * This method will use the current credentials or throw a {@link RuntimeException} if there's none.
	 * </p>
	 * 
	 * @param context
	 *            Android application context.
	 * @param listener
	 *            {@link SPOfferBannerListener} which will be notified of the results of the
	 *            request.
	 * 
	 * @return An {@link OfferBannerRequest} instance which manages the request to the server on the
	 *         background.
	 */
	public static OfferBannerRequest requestOfferBanner(Context context, SPOfferBannerListener listener) {
		return requestOfferBanner(context, listener, null, null, (Map<String, String>)null);
	}
	
	/**
	 * <p>
	 * Requests an Offer Banner to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * </p>
	 * 
	 * <p>
	 * This method will use the current credentials or throw a {@link RuntimeException} if there's none.
	 * </p>
	 * 
	 * @param context
	 *            Android application context.
	 * @param listener
	 *            {@link SPOfferBannerListener} which will be notified of the results of the
	 *            request.
	 * @param offerBannerAdShape
	 *            Provide null for this parameter to request a banner of the default dimensions (320
	 *            x 50).
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An {@link OfferBannerRequest} instance which manages the request to the server on the
	 *         background.
	 */
	public static OfferBannerRequest requestOfferBanner(Context context,
			SPOfferBannerListener listener,	OfferBanner.AdShape offerBannerAdShape, 
			String currencyName, Map<String, String> customParams) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		return requestOfferBanner(credentialsToken, context, listener, offerBannerAdShape,
				currencyName, (Map<String, String>)null);
	}
	
	/**
	 * Requests an Offer Banner to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * 
	 * @param credentialsToken
	 *            The token id of the credentials to be used.
	 * @param context
	 *            Android application context.
	 * @param listener
	 *            {@link SPOfferBannerListener} which will be notified of the results of the
	 *            request.
	 * @param offerBannerAdShape
	 *            Provide null for this parameter to request a banner of the default dimensions (320
	 *            x 50).
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An {@link OfferBannerRequest} instance which manages the request to the server on the
	 *         background.
	 */
	public static OfferBannerRequest requestOfferBanner(String credentialsToken, Context context, 
			SPOfferBannerListener listener,	OfferBanner.AdShape offerBannerAdShape, 
			String currencyName, Map<String, String> customParams) {
		
		if (offerBannerAdShape == null) {
			offerBannerAdShape = sDefaultOfferBannerAdShape;
		}
		
		OfferBannerRequest bannerRequest = new OfferBannerRequest(context, credentialsToken,
				listener, offerBannerAdShape, currencyName, getCustomParameters(customParams));
		
		if (sOverridingWebViewUrl != null) {
			bannerRequest.setOverridingUrl(sOverridingWebViewUrl);
		}
		
		bannerRequest.requestOfferBanner();
		
		return bannerRequest;
	}

	//================================================================================
	// Mobile BrandEngage
	//================================================================================

	//FIXME add documentation
	
	public static SPBrandEngageRequest getIntentForMBEActivity(Activity activity, 
			SPBrandEngageRequestListener listener) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		return getIntentForMBEActivity(credentialsToken, activity, listener);
	}

	public static SPBrandEngageRequest getIntentForMBEActivity(String credentialsToken,
			Activity activity, SPBrandEngageRequestListener listener) {
		return getIntentForMBEActivity(credentialsToken, activity, listener, null, null, null);
	}
	
	public static SPBrandEngageRequest getIntentForMBEActivity(String credentialsToken, Activity activity, 
			SPBrandEngageRequestListener listener, String currencyName, Map<String, String> parameters, 
			SPCurrencyServerListener vcsListener) {
		if (SPBrandEngageClient.INSTANCE.canRequestOffers()) {

			SPCredentials credentials = SponsorPay
					.getCredentials(credentialsToken);

			SPBrandEngageClient.INSTANCE.setCurrencyName(currencyName);
			SPBrandEngageClient.INSTANCE
					.setCustomParameters(getCustomParameters(parameters));
			SPBrandEngageClient.INSTANCE.setCurrencyListener(vcsListener);

			SPBrandEngageRequest request = new SPBrandEngageRequest(
					credentials, activity, listener);
			request.askForOffers();
			return request;
		} else {
			return null;
		}
	}
	
	//================================================================================
    // Deprecated Methods
	//================================================================================
	
	//================================================================================
	// OfferWall
	//================================================================================
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}.
	 * </p>
	 * <p>
	 * Will retrieve the publisher application id from the application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link SponsorPayPublisher#getIntentForOfferWallActivity(Context, Boolean)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context, String userId) {
		return getIntentForOfferWallActivity(context, userId, null, null, null);
	}
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}.
	 * </p>
	 * <p>
	 * Will retrieve the publisher application id from the application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForOfferWallActivity(Context, Boolean, String, HashMap)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context, String userId, String currencyName) {
		return getIntentForOfferWallActivity(context, userId, null, currencyName, null, null);
	}

	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * <p>
	 * Will retrieve the publisher application id from the application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForOfferWallActivity(Context, Boolean)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context, String userId,
			boolean shouldStayOpen) {

		return getIntentForOfferWallActivity(context, userId, shouldStayOpen, null, null);
	}
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * <p>
	 * Will retrieve the publisher application id from the application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForOfferWallActivity(String, Context, Boolean, String, HashMap)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context, String userId,
			 String currencyName,  boolean shouldStayOpen) {
		
		return getIntentForOfferWallActivity(context, userId, shouldStayOpen, currencyName, null, null);
	}

	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the provided publisher application id instead of trying to retrieve it from the
	 * application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @param overrideAppId
	 *            An app ID which will override the one included in the manifest.
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForOfferWallActivity(Context, Boolean, String, HashMap)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context, String userId,
			boolean shouldStayOpen, String overrideAppId) {

		return getIntentForOfferWallActivity(context, userId, shouldStayOpen, null, overrideAppId, null);
	}
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the provided publisher application id instead of trying to retrieve it from the
	 * application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	
	 * @param overrideAppId
	 *            An app ID which will override the one included in the manifest.
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForOfferWallActivity(Context, Boolean, String, HashMap)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context, String userId,
			boolean shouldStayOpen, String currencyName, String overrideAppId) {
		
		return getIntentForOfferWallActivity(context, userId, shouldStayOpen, currencyName, overrideAppId, null);
	}

	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the provided publisher application id instead of trying to retrieve it from the
	 * application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForOfferWallActivity(String, Context, Boolean, String, HashMap)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context,
			String userId, Boolean shouldStayOpen, String overridingAppId,
			HashMap<String, String> customParams) {
		return getIntentForOfferWallActivity(context, userId, shouldStayOpen,
				null, overridingAppId, customParams);
	}
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the {@link OfferWallActivity}. Lets the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the provided publisher application id instead of trying to retrieve it from the
	 * application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param shouldStayOpen
	 *            True if the Offer Wall should stay open after the user clicks on an offer and gets
	 *            redirected out of the app. False to close the Offer Wall.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	 
	 * @param overridingAppId
	 *            An app ID which will override the one included in the manifest.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForOfferWallActivity(String, Context, Boolean, String, HashMap)} instead.
	 */
	public static Intent getIntentForOfferWallActivity(Context context,
			String userId, Boolean shouldStayOpen, String currencyName,
			String overridingAppId, HashMap<String, String> customParams) {
		String credentialsToken = SponsorPay.getCredentials(overridingAppId, userId, null, context);
		return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, currencyName, customParams);
	}
	
	//================================================================================
	// Unlock OfferWall
	//================================================================================
	
	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the Unlock {@link OfferWallActivity}. Let the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will retrieve the AppID from the application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param unlockItemId
	 * 			  The Id of the item to be used to show offer for unlocking.
	 * @param unlockItemName
	 * 			  An item name to override the default one set on the server
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForUnlockOfferWallActivity(Context, String, String)} instead.
	 */
	public static Intent getIntentForUnlockOfferWallActivity(Context context, String userId,
			String unlockItemId, String unlockItemName) {
		return getIntentForUnlockOfferWallActivity(context, userId, unlockItemId, unlockItemName,
				null, null);
	}

	/**
	 * <p>
	 * Returns an {@link Intent} that can be used to launch the Unlock {@link OfferWallActivity}. Let the
	 * caller specify the behavior of the Offer Wall once the user gets redirected out of the
	 * application by clicking on an offer.
	 * </p>
	 * 
	 * <p>
	 * Will use the provided publisher application id instead of trying to retrieve it from the
	 * application manifest.
	 * </p>
	 * 
	 * @param context
	 *            The publisher application context.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param unlockItemId
	 * 			  The Id of the item to be used to show offer for unlocking.
	 * @param unlockItemName
	 * 			  An item name to override the default one set on the server
	 * @param overrideAppId
	 *            An app ID which will override the one included in the manifest.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An Android {@link Intent} which can be used with the {@link Activity} method
	 *         startActivityForResult() to launch the {@link OfferWallActivity}.
	 *         
	 * @deprecated This method will be removed from a future release of the SDK. You should use 
	 * 				{@link #getIntentForUnlockOfferWallActivity(String, Context, String, String, HashMap)} instead.
	 */
	public static Intent getIntentForUnlockOfferWallActivity(Context context, String userId,
			String unlockItemId, String unlockItemName, String overrideAppId,
			HashMap<String, String> customParams) {
		String credentialsToken = SponsorPay.getCredentials(overrideAppId, userId, null, context);
		return getIntentForUnlockOfferWallActivity(credentialsToken, context,
				unlockItemId, unlockItemName, customParams);
	}
	
	//================================================================================
	// Interstitial
	//================================================================================
	
	/**
	 * Starts the mobile interstitial request / loading / showing process.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param loadingTimeoutSecs
	 *            Sets the maximum amount of time the interstitial should take to load. If you set
	 *            it to 0 or a negative number, it will fall back to the default value of 5 seconds.
	 * @param overriddenAppId
	 *            An app ID which will override the one included in the manifest.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName, int loadingTimeoutSecs, String overriddenAppId) {

		loadShowInterstitial(callingActivity, userId, loadingStatusListener, shouldStayOpen,
				backgroundUrl, skinName, loadingTimeoutSecs, null, overriddenAppId, null);
	}
	
	/**
	 * Starts the mobile interstitial request / loading / showing process.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param loadingTimeoutSecs
	 *            Sets the maximum amount of time the interstitial should take to load. If you set
	 *            it to 0 or a negative number, it will fall back to the default value of 5 seconds.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param overriddenAppId
	 *            An app ID which will override the one included in the manifest.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName, int loadingTimeoutSecs, String currencyName, String overriddenAppId) {
		
		loadShowInterstitial(callingActivity, userId, loadingStatusListener, shouldStayOpen,
				backgroundUrl, skinName, loadingTimeoutSecs, currencyName, overriddenAppId, null);
	}

	/**
	 * Starts the mobile interstitial request / loading / showing process.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param loadingTimeoutSecs
	 *            Sets the maximum amount of time the interstitial should take to load. If you set
	 *            it to 0 or a negative number, it will fall back to the default value of 5 seconds.
	 * @param overriddenAppId
	 *            An app ID which will override the one included in the manifest.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity,
			String userId,
			InterstitialLoadingStatusListener loadingStatusListener,
			Boolean shouldStayOpen, String backgroundUrl, String skinName,
			int loadingTimeoutSecs, String overriddenAppId,
			Map<String, String> customParams) {
		loadShowInterstitial(callingActivity, userId, loadingStatusListener,
				shouldStayOpen, backgroundUrl, skinName, loadingTimeoutSecs,
				null, overriddenAppId, customParams);
	}
	 
	/**
	 * Starts the mobile interstitial request / loading / showing process.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param loadingTimeoutSecs
	 *            Sets the maximum amount of time the interstitial should take to load. If you set
	 *            it to 0 or a negative number, it will fall back to the default value of 5 seconds.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param overriddenAppId
	 *            An app ID which will override the one included in the manifest.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName, int loadingTimeoutSecs, String currencyName, 
			String overriddenAppId, Map<String, String> customParams) {
		String credentialsToken = SponsorPay.getCredentials(
				overriddenAppId, userId, null, callingActivity.getApplication());
		loadShowInterstitial(credentialsToken, callingActivity,
				loadingStatusListener, shouldStayOpen, backgroundUrl, skinName,
				loadingTimeoutSecs, currencyName, customParams);
	}
	
	/**
	 * Starts the mobile interstitial request / loading / showing process retrieving the application
	 * id from the Android Manifest.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param loadingTimeoutSecs
	 *            Sets the maximum amount of time the interstitial should take to load. If you set
	 *            it to 0 or a negative number, it will fall back to the default value of 5 seconds.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName, int loadingTimeoutSecs) {

		loadShowInterstitial(callingActivity, userId, loadingStatusListener, shouldStayOpen,
				backgroundUrl, skinName, loadingTimeoutSecs, null, null, null);
	}

	/**
	 * Starts the mobile interstitial request / loading / showing process using a default value for
	 * loadingTimeoutSecs and retrieving the application id from the Android Manifest.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName) {

		loadShowInterstitial(callingActivity, userId, loadingStatusListener, shouldStayOpen,
				backgroundUrl, skinName, 0, null, null, null);
	}
	
	/**
	 * Starts the mobile interstitial request / loading / showing process using a default value for
	 * loadingTimeoutSecs and retrieving the application id from the Android Manifest.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param backgroundUrl
	 *            Can be set to the absolute URL of an image to use as background graphic for the
	 *            interstitial. Must include the protocol scheme (http:// or https://) at the
	 *            beginning of the URL. Leave it null for no custom background.
	 * @param skinName
	 *            Used to specify the name of a custom skin or template for the requested
	 *            interstitial. Leaving it null will make the interstitial fall back to the DEFAULT
	 *            template.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen,
			String backgroundUrl, String skinName, String currencyName) {
		
		loadShowInterstitial(callingActivity, userId, loadingStatusListener, shouldStayOpen,
				backgroundUrl, skinName, 0, currencyName, null, null);
	}

	/**
	 * Starts the mobile interstitial request / loading / showing process using default values for
	 * backgroundUrl, skinName, loadingTimeoutSecs and retrieving the application id from the
	 * Android Manifest.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen) {

		loadShowInterstitial(callingActivity, userId, loadingStatusListener, shouldStayOpen, null,
				null, 0, null, null, null);
	}
	/**
	 * Starts the mobile interstitial request / loading / showing process using default values for
	 * backgroundUrl, skinName, loadingTimeoutSecs and retrieving the application id from the
	 * Android Manifest.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param shouldStayOpen
	 *            Used to specify the behavior of the interstitial once the user clicks on the
	 *            presented ad and is redirected outside the host publisher app. The default
	 *            behavior is to close the interstitial and let the user go back to the activity
	 *            that called the interstitial when they come back to the app. If you want the
	 *            interstitial not to close until the user does it explicitly, set this parameter to
	 *            true.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, Boolean shouldStayOpen, String currencyName) {
		
		loadShowInterstitial(callingActivity, userId, loadingStatusListener, shouldStayOpen, null,
				null, 0, currencyName,  null, null);
	}

	/**
	 * Starts the mobile interstitial request / loading / showing process using default values for
	 * shouldStayOpen, backgroundUrl, skinName, loadingTimeoutSecs and retrieving the application id
	 * from the Android Manifest.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 *           
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener) {

		loadShowInterstitial(callingActivity, userId, loadingStatusListener, null, null, null, 0,
				null, null, null);
	}
	/**
	 * Starts the mobile interstitial request / loading / showing process using default values for
	 * shouldStayOpen, backgroundUrl, skinName, loadingTimeoutSecs and retrieving the application id
	 * from the Android Manifest.
	 * 
	 * @param callingActivity
	 *            The activity which requests the interstitial. A progress dialog will be shown on
	 *            top of it and if an ad is returned, the calling activity will be used to launch
	 *            the {@link InterstitialActivity} in order to show the ad.
	 * @param userId
	 *            The current user ID of the host application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.	
	 *           
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void loadShowInterstitial(Activity callingActivity, String userId,
			InterstitialLoadingStatusListener loadingStatusListener, String currencyName) {
		
		loadShowInterstitial(callingActivity, userId, loadingStatusListener, null, null, null, 0,
				currencyName, null, null);
	}

	//================================================================================
	//VCS
	//================================================================================
	
	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for a given user. Returns immediately, and the answer is delivered to one of
	 * the provided listener's callback methods. See {@link SPCurrencyServerListener}.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            The ID of the user for which the delta of coins will be requested.
	 * @param listener
	 *            {@link SPCurrencyServerListener} which will be notified of the result of the
	 *            request.
	 * @param transactionId
	 *            Optionally, provide the ID of the latest known transaction. The delta of coins
	 *            will be calculated from this transaction (not included) up to the present. Leave
	 *            it to null to let the SDK use the latest transaction ID it kept track of.
	 * @param securityToken
	 *            Security Token associated with the provided Application ID. It's used to sign the
	 *            requests and verify the server responses.
	 * @param applicationId
	 *            Application ID assigned by SponsorPay. Provide null to read the Application ID
	 *            from the Application Manifest.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void requestNewCoins(Context context, String userId,
			SPCurrencyServerListener listener, String transactionId, String securityToken,
			String applicationId) {

		requestNewCoins(context, userId, listener, transactionId, securityToken, applicationId,
				null);
	}

	/**
	 * Sends a request to the SponsorPay currency server to obtain the variation in amount of
	 * virtual currency for a given user. Returns immediately, and the answer is delivered to one of
	 * the provided listener's callback methods. See {@link SPCurrencyServerListener}.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            The ID of the user for which the delta of coins will be requested.
	 * @param listener
	 *            {@link SPCurrencyServerListener} which will be notified of the result of the
	 *            request.
	 * @param transactionId
	 *            Optionally, provide the ID of the latest known transaction. The delta of coins
	 *            will be calculated from this transaction (not included) up to the present. Leave
	 *            it to null to let the SDK use the latest transaction ID it kept track of.
	 * @param securityToken
	 *            Security Token associated with the provided Application ID. It's used to sign the
	 *            requests and verify the server responses.
	 * @param applicationId
	 *            Application ID assigned by SponsorPay. Provide null to read the Application ID
	 *            from the Application Manifest.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void requestNewCoins(Context context, String userId,
			SPCurrencyServerListener listener, String transactionId, String securityToken,
			String applicationId, Map<String, String> customParams) {
		
		String credentialsToken = SponsorPay.getCredentials(applicationId, userId, securityToken, context);
		requestNewCoins(credentialsToken, context, listener, transactionId, customParams);
	}

	//================================================================================
	// Unlock Items
	//================================================================================
	
	/**
	 * <p>
	 * Requests the status of the "Unlockable" Items to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * </p>
	 * 
	 * <p>
	 * Will use the application id set in the application manifest.
	 * </p>
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            The ID of the user for whom the banner will be requested.
	 * @param listener
	 *            {@link SPUnlockResponseListener} which will be notified of the results of the
	 *            request.
	 * @param securityToken
	 *            Security Token associated with the provided Application ID. It's used to sign the
	 *            requests and verify the server responses.
	 * 
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void requestUnlockItemsStatus(Context context, String userId,
			SPUnlockResponseListener listener, String securityToken) {

		requestUnlockItemsStatus(context, userId, listener, securityToken, null, null);
	}
	
	/**
	 * <p>
	 * Requests the status of the "Unlockable" Items to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * </p>
	 * 
	 * <p>
	 * Will use the provided publisher application id instead of trying to retrieve it from the
	 * application manifest.
	 * </p>
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            The ID of the user for whom the banner will be requested.
	 * @param listener
	 *            {@link SPUnlockResponseListener} which will be notified of the results of the
	 *            request.
	 * @param securityToken
	 *            Security Token associated with the provided Application ID. It's used to sign the
	 *            requests and verify the server responses.
	 * @param applicationId
	 *            Your Application ID, or null to retrieve it from your application manifest.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static void requestUnlockItemsStatus(Context context, String userId,
			SPUnlockResponseListener listener, String securityToken, String applicationId,
			Map<String, String> customParams) {
		String credentialsToken = SponsorPay.getCredentials(applicationId, userId, securityToken, context);
		requestUnlockItemsStatus(credentialsToken, context, listener, customParams);
	}
	
	//================================================================================
	// Offer banner
	//================================================================================
	
	/**
	 * Requests an Offer Banner to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            The ID of the user for whom the banner will be requested.
	 * @param listener
	 *            {@link SPOfferBannerListener} which will be notified of the results of the
	 *            request.
	 * @param offerBannerAdShape
	 *            Provide null for this parameter to request a banner of the default dimensions (320
	 *            x 50).
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param applicationId
	 *            Your Application ID, or null to retrieve it from your application manifest.
	 * @return An {@link OfferBannerRequest} instance which manages the request to the server on the
	 *         background.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static OfferBannerRequest requestOfferBanner(Context context, String userId,
			SPOfferBannerListener listener, OfferBanner.AdShape offerBannerAdShape,
			String currencyName, String applicationId) {

		return requestOfferBanner(context, userId, listener, offerBannerAdShape, currencyName,
				applicationId, null);
	}

	/**
	 * Requests an Offer Banner to the SponsorPay servers and registers a listener which will be
	 * notified when a response is received.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            The ID of the user for whom the banner will be requested.
	 * @param listener
	 *            {@link SPOfferBannerListener} which will be notified of the results of the
	 *            request.
	 * @param offerBannerAdShape
	 *            Provide null for this parameter to request a banner of the default dimensions (320
	 *            x 50).
	 * @param currencyName
	 *            The name of the currency employed by your application. Provide null if you don't
	 *            use a custom currency name.
	 * @param applicationId
	 *            Your Application ID, or null to retrieve it from your application manifest.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 * 
	 * @return An {@link OfferBannerRequest} instance which manages the request to the server on the
	 *         background.
	 *            
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static OfferBannerRequest requestOfferBanner(Context context, String userId,
			SPOfferBannerListener listener, OfferBanner.AdShape offerBannerAdShape,
			String currencyName, String applicationId, Map<String, String> customParams) {
		String credentialsToken = SponsorPay.getCredentials(applicationId, userId, null, context);
		return requestOfferBanner(credentialsToken, context, listener, offerBannerAdShape, currencyName, customParams);
	}
	
}