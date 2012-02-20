/**
 * SponsorPay Android Advertiser SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.advertiser.AdvertiserCallbackSender.APIResultListener;

/**
 * <p>
 * Provides convenience calls to run the Advertiser callback request. Manages the state of the SDK
 * determining whether a successful response to the callback request has been already received since
 * the application was installed in the host device.
 * </p>
 * 
 * <p>
 * It's implemented as a singleton, and its public methods are static.
 * </p>
 */
public class SponsorPayAdvertiser implements APIResultListener {

	/**
	 * Map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	private static Map<String, String> sCustomParameters;

	private static boolean sShouldUseStagingUrls = false;

	public static void setShouldUseStagingUrls(boolean value) {
		sShouldUseStagingUrls = value;
	}

	public static boolean shouldUseStagingUrls() {
		return sShouldUseStagingUrls;
	}

	/**
	 * Sets a map of custom key/values to add to the parameters on the requests to the REST API.
	 * 
	 * @param params
	 */
	public static void setCustomParameters(Map<String, String> params) {
		sCustomParameters = params;
	}

	/**
	 * Sets a map of custom key/values to add to the parameters on the requests to the REST API.
	 * 
	 * @param keys
	 * @param values
	 */
	public static void setCustomParameters(String[] keys, String[] values) {
		sCustomParameters = UrlBuilder.mapKeysToValues(keys, values);
	}

	/**
	 * Clears the map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	public static void clearCustomParameters() {
		sCustomParameters = null;
	}

	/**
	 * {@link AdvertiserHostInfo} used to collect data related to the host device and application.
	 */
	private HostInfo mHostInfo;

	/**
	 * {@link AdvertiserCallbackSender} used to call the Advertiser API asynchronously.
	 */
	private AdvertiserCallbackSender mAPICaller;

	/**
	 * Host app's Android application context.
	 */
	private Context mContext;

	private SponsorPayAdvertiserState mPersistedState;

	/**
	 * Singleton instance.
	 */
	private static SponsorPayAdvertiser mInstance;

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
		else if (sCustomParameters != null)
			retval = new HashMap<String, String>(sCustomParameters);
		else {
			retval = null;
		}

		return retval;
	}

	/**
	 * Constructor. Stores the received application context and loads up the shared preferences.
	 * 
	 * @param context
	 *            The host application context.
	 */
	private SponsorPayAdvertiser(Context context) {
		mContext = context;
		mPersistedState = new SponsorPayAdvertiserState(mContext);
	}

	/**
	 * Triggers the Advertiser callback. Will try to retrieve the Application ID from the value
	 * defined in the host application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 */
	public static void register(Context context) {
		register(context, null, null);
	}

	/**
	 * Triggers the Advertiser callback after the specified delay has passed. Will retrieve the App
	 * ID from the value defined in the host application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 * @param delayMin
	 *            The delay in minutes for triggering the Advertiser callback.
	 */
	public static void registerWithDelay(Context context, int delayMin) {
		registerWithDelay(context, delayMin, null, null);
	}

	/**
	 * Triggers the Advertiser callback after the specified delay has passed. Will use the provided
	 * App ID instead of trying to retrieve the one defined in the host application's manifest.
	 * 
	 * @param context
	 *            Host application context.
	 * @param delayMin
	 *            The delay in minutes for triggering the Advertiser callback.
	 * @param overrideAppId
	 *            The App ID to use.
	 */
	public static void registerWithDelay(Context context, int delayMin, String overrideAppId) {
		registerWithDelay(context, delayMin, overrideAppId, null);
	}

	/**
	 * Triggers the Advertiser callback after the specified delay has passed. Will use the provided
	 * App ID instead of trying to retrieve the one defined in the host application's manifest.
	 * 
	 * @param context
	 *            Host application context.
	 * @param delayMin
	 *            The delay in minutes for triggering the Advertiser callback.
	 * @param overrideAppId
	 *            The App ID to use.
	 * @param customParams
	 *            Map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	public static void registerWithDelay(Context context, int delayMin, String overrideAppId,
			Map<String, String> customParams) {

		SponsorPayCallbackDelayer.callWithDelay(context, overrideAppId, delayMin,
				getCustomParameters(customParams));
	}

	/**
	 * Triggers the Advertiser callback. If passed a non-null and non-empty Application ID, it will
	 * be used. Otherwise the Application ID will be retrieved from the value defined in the host
	 * application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 * @param overrideAppId
	 *            The App ID to use.
	 */
	public static void register(Context context, String overrideAppId) {
		register(context, overrideAppId, null);
	}

	/**
	 * Triggers the Advertiser callback. If passed a non-null and non-empty Application ID, it will
	 * be used. Otherwise the Application ID will be retrieved from the value defined in the host
	 * application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 * @param overrideAppId
	 *            The App ID to use.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void register(Context context, String overrideAppId,
			Map<String, String> customParams) {

		if (mInstance == null) {
			mInstance = new SponsorPayAdvertiser(context);
		}

		// The actual work is performed by the register() instance method.
		mInstance.register(overrideAppId, getCustomParameters(customParams));
	}

	/**
	 * This method does the actual registration at the SponsorPay backend, performing the advertiser
	 * callback, and including in it a parameter to signal if a successful response has been
	 * received yet.
	 * 
	 * @param overrideAppId
	 *            If left empty (""), will use the App ID value from the application manifest xml
	 *            file. Otherwise, will use the specified App ID.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	private void register(String overrideAppId, Map<String, String> customParams) {
		/* Collect data about the device */
		mHostInfo = new HostInfo(mContext);

		if (overrideAppId != null && !overrideAppId.equals("")) {
			// Override App ID
			mHostInfo.setOverriddenAppId(overrideAppId);
		}

		/*
		 * Check if we have called SponsorPay's API before and gotten a successful response.
		 */
		boolean gotSuccessfulResponseYet = mPersistedState
				.getHasAdvertiserCallbackReceivedSuccessfulResponse();

		/* Send asynchronous call to SponsorPay's API */
		mAPICaller = new AdvertiserCallbackSender(mHostInfo, this);

		mAPICaller.setWasAlreadySuccessful(gotSuccessfulResponseYet);
		mAPICaller.setInstallSubId(mPersistedState.getInstallSubId());
		mAPICaller.setCustomParams(customParams);
		
		mAPICaller.trigger();
	}

	/**
	 * This method is invoked when a response for the advertiser callback is received.
	 * 
	 * @param wasSuccessful
	 *            Status flag to indicate if Advertiser API has been contacted successfully.
	 */
	public void onAPIResponse(boolean wasSuccessful) {
		if (wasSuccessful) {
			mPersistedState.setHasAdvertiserCallbackReceivedSuccessfulResponse(true);
		}
	}
}
