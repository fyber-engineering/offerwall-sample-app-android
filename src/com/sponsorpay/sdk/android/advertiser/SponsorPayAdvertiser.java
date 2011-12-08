/**
 * SponsorPay Android Advertiser SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.advertiser.AdvertiserCallbackSender.APIResultListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * <p>
 * Provides convenience calls to run the Advertiser callback request. Manages the state of the SDK determining whether a
 * successful response to the callback request has been already received since the application was installed in the host
 * device.
 * </p>
 * 
 * <p>
 * It's implemented as a singleton, and its public methods are static.
 * </p>
 */
public class SponsorPayAdvertiser implements APIResultListener {

	/**
	 * Shared preferences file name. We store a flag into the shared preferences which is checked on each consecutive
	 * invocation of {@link #register()}, to keep track of whether we have already successfully contacted the Advertiser
	 * API.
	 */
	private static final String PREFERENCES_FILE_NAME = "SponsorPayAdvertiserState";

	/**
	 * The key to store in the preferences file the flag which determines if we have already successfully contacted the
	 * Advertiser API.
	 */
	private static final String STATE_GOT_SUCCESSFUL_RESPONSE_KEY = "SponsorPayAdvertiserState"; // TODO

	/**
	 * The shared preferences encoded in the {@link #PREFERENCES_FILE_NAME} file.
	 */
	private SharedPreferences mPrefs;

	private static boolean sShouldUseStagingUrls = false;

	public static void setShouldUseStagingUrls(boolean value) {
		sShouldUseStagingUrls = value;
	}

	public static boolean shouldUseStagingUrls() {
		return sShouldUseStagingUrls;
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

	/**
	 * Singleton instance.
	 */
	private static SponsorPayAdvertiser mInstance;

	/**
	 * Constructor. Stores the received application context and loads up the shared preferences.
	 * 
	 * @param context
	 *            The host application context.
	 */
	private SponsorPayAdvertiser(Context context) {
		mContext = context;
		mPrefs = mContext.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
	}

	/**
	 * Trigger the Advertiser callback. Will try to retrieve the Application ID from the value defined in the host
	 * application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 */
	public static void register(Context context) {

		// Instantiate the singleton instance if yet uninitialized.
		if (mInstance == null) {
			mInstance = new SponsorPayAdvertiser(context);
		}

		// The actual work is performed by the register(String overrideAppId) instance method, which will be called
		// by its parameterless overload with the App ID retrieved from the application manifest.
		mInstance.register();
	}

	/**
	 * Trigger the Advertiser callback after the specified delay has passed. Will retrieve the App ID from the value
	 * defined in the host application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 * @param delayMin
	 *            The delay in minutes for triggering the Advertiser callback.
	 */
	public static void registerWithDelay(Context context, int delayMin) {
		SponsorPayCallbackDelayer.callWithDelay(context, "", delayMin);
	}

	/**
	 * Trigger the Advertiser callback after the specified delay has passed. Will use the provided App ID instead of
	 * trying to retrieve the one defined in the host application's manifest.
	 * 
	 * @param context
	 *            Host application context.
	 * @param delayMin
	 *            The delay in minutes for triggering the Advertiser callback.
	 * @param overrideAppId
	 *            The App ID to use.
	 */
	public static void registerWithDelay(Context context, int delayMin, String overrideAppId) {
		SponsorPayCallbackDelayer.callWithDelay(context, overrideAppId, delayMin);
	}

	/**
	 * Trigger the Advertiser callback. If passed a non-null and non-empty Application ID, it will be used. Otherwise
	 * the Application ID will be retrieved from the value defined in the host application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 * @param overrideAppId
	 *            The App ID to use.
	 */
	public static void register(Context context, String overrideAppId) {
		if (mInstance == null) {
			mInstance = new SponsorPayAdvertiser(context);
		}

		// The actual work is performed by the register(String overrideAppId) instance method.
		mInstance.register(overrideAppId);
	}

	/**
	 * This method does the actual registration at Sponsorpay's Ad API, performing the advertiser callback, and
	 * including in it a parameter to signal if a successful response has been received yet.
	 * 
	 * @param overrideAppId
	 *            If left empty (""), will use the App ID value from the application manifest xml file. Otherwise, will
	 *            use the specified App ID.
	 */
	private void register(String overrideAppId) {
		/* Collect data about the device */
		mHostInfo = new HostInfo(mContext);

		if (overrideAppId != null && !overrideAppId.equals("")) {
			// Override App ID
			mHostInfo.setOverriddenAppId(overrideAppId);
		}

		/*
		 * Check if we have called SponsorPay's API before and gotten a successful response.
		 */
		boolean gotSuccessfulResponseYet = mPrefs.getBoolean(STATE_GOT_SUCCESSFUL_RESPONSE_KEY, false);

		/* Send asynchronous call to SponsorPay's API */
		mAPICaller = new AdvertiserCallbackSender(mHostInfo, this);
		mAPICaller.setWasAlreadySuccessful(gotSuccessfulResponseYet);
		mAPICaller.trigger();
	}

	/**
	 * Calls its overload {@link #register()} with an empty overrideAppId parameter.
	 */
	private void register() {
		SponsorPayAdvertiser.this.register("");
	}

	/**
	 * This method is invoked when a response for the advertiser callback is received.
	 * 
	 * @param wasSuccessful
	 *            status flag if the Advertiser API has been contacted successfully
	 */
	public void onAPIResponse(boolean wasSuccessful) {
		/*
		 * If we have been successful store the STATE_GOT_SUCCESSFUL_RESPONSE_KEY flag inside the preferences and flush
		 * them to permanent storage.
		 */
		if (wasSuccessful) {
			Editor prefsEditor = mPrefs.edit();
			prefsEditor.putBoolean(STATE_GOT_SUCCESSFUL_RESPONSE_KEY, true);
			prefsEditor.commit();
		}
	}
}
