/**
 * SponsorPay Android Advertiser SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import com.sponsorpay.sdk.android.advertiser.AsyncAPICaller.APIResultListener;

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
	 * Version information.
	 */
	public static final int MAJOR_RELEASE_NUMBER = 1;
	public static final int MINOR_RELEASE_NUMBER = 2;
	public static final int BUGFIX_RELEASE_NUMBER = 0;
	public static final String RELEASE_VERSION_STRING = String.format("%d.%d.%d", MAJOR_RELEASE_NUMBER, MINOR_RELEASE_NUMBER,
			BUGFIX_RELEASE_NUMBER);

	/**
	 * Shared preferences file name. We store a flag into the shared preferences which is checked on each consecutive
	 * invocation of {@link #register()}, to ensure that once we have once successfully contacted the Advertiser API we
	 * shall never send the advertiser callback request again (provided the user doesn't uninstall and reinstall the
	 * application, or delete all its preferences).
	 */
	private static final String PREFERENCES_FILE_NAME = "SponsorPayAdvertiserState";

	/**
	 * The key to store in the preferences file the flag which determines if we have already successfully contacted the
	 * Advertiser API.
	 */
	private static final String STATE_GOT_SUCCESSFUL_RESPONSE_KEY = "SponsorPayAdvertiserState";

	private static final boolean SHOULD_USE_OFFER_ID_DEFAULT = false;

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
	 * Whether the advertiser callback should encode the program ID in the request to SponsorPay's backend with the key
	 * 'offer_id' instead of 'program_id'. Default is {@value #SHOULD_USE_OFFER_ID_DEFAULT}.
	 */
	private static boolean sShouldUseOfferId = SHOULD_USE_OFFER_ID_DEFAULT;

	/**
	 * Whether the advertiser callback should encode the program ID in the request to SponsorPay's backend with the key
	 * 'offer_id' instead of 'program_id'. Default is {@value #SHOULD_USE_OFFER_ID_DEFAULT}.
	 * 
	 * @param value
	 */
	public static void setShouldUseOfferId(boolean value) {
		sShouldUseOfferId = value;
	}

	/**
	 * Gets Whether the advertiser callback should encode the program ID in the request to SponsorPay's backend with the
	 * key 'offer_id' instead of 'program_id'.
	 * 
	 * @return
	 */
	public static boolean shouldUseOfferId() {
		return sShouldUseOfferId;
	}

	/**
	 * {@link AdvertiserHostInfo} used to collect data related to the host device and application.
	 */
	private AdvertiserHostInfo mHostInfo;

	/**
	 * {@link AsyncAPICaller} used to call the Advertiser API asynchronously.
	 */
	private AsyncAPICaller mAPICaller;

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
	 * Trigger the Advertiser callback. Will retrieve the program ID from the value defined in the host application's
	 * Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 */
	public static void register(Context context) {

		// Instantiate the singleton instance if yet uninitialized.
		if (mInstance == null) {
			mInstance = new SponsorPayAdvertiser(context);
		}

		// The actual work is performed by the register(String overrideProgramId) instance method, which will be called
		// by
		// its parameterless overload.
		mInstance.register();
	}

	/**
	 * Trigger the Advertiser callback after the specified delay has passed. Will retrieve the program ID from the value
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
	 * Trigger the Advertiser callback after the specified delay has passed. Will use the provided program ID instead of
	 * trying to retrieve the one defined in the host application's manifest.
	 * 
	 * @param context
	 *            Host application context.
	 * @param delayMin
	 *            The delay in minutes for triggering the Advertiser callback.
	 * @param overrideProgramId
	 *            The program id to use.
	 */
	public static void registerWithDelay(Context context, int delayMin, String overrideProgramId) {
		SponsorPayCallbackDelayer.callWithDelay(context, overrideProgramId, delayMin);
	}

	/**
	 * Trigger the Advertiser callback. Will use the provided program ID instead of retrieving it from the value defined
	 * in the host application's Android Manifest XML file.
	 * 
	 * @param context
	 *            Host application context.
	 * @param overrideProgramId
	 *            The program id to use.
	 */
	public static void register(Context context, String overrideProgramId) {
		if (mInstance == null) {
			mInstance = new SponsorPayAdvertiser(context);
		}

		// The actual work is performed by the register(String overrideProgramId) instance method.
		mInstance.register(overrideProgramId);
	}

	/**
	 * This method does the actual registration at Sponsorpay's Ad API, performing the advertiser callback request only
	 * if a successful response hasn't been received yet.
	 * 
	 * @param overrideProgramId
	 *            If left empty (""), will use the program id value from the application manifest xml file. Otherwise,
	 *            will use the specified program id.
	 */
	private void register(String overrideProgramId) {
		/*
		 * Check if we have called SponsorPay's API before and gotten a successful response.
		 */
		boolean gotSuccessfulResponseYet = mPrefs.getBoolean(STATE_GOT_SUCCESSFUL_RESPONSE_KEY, false);

		if (!gotSuccessfulResponseYet) {
			/* Collect data about the device */
			mHostInfo = new AdvertiserHostInfo(mContext);

			if (!overrideProgramId.equals("")) {
				// Override program ID
				mHostInfo.setOverriddenProgramId(overrideProgramId);
			}

			/* Send asynchronous call to SponsorPay's API */
			mAPICaller = new AsyncAPICaller(mHostInfo, this);
			mAPICaller.trigger();
		} else {
			// Nothing to do
		}
	}

	/**
	 * Calls its overload {@link #register()} with an empty overrideProgramId parameter.
	 */
	private void register() {
		SponsorPayAdvertiser.this.register("");
	}

	/**
	 * This method is invoked when a response for the advertiser callback is received.
	 * 
	 * @param wasSuccessful
	 *            status flag if the Ad API has been contacted successfully
	 */
	public void onAPIResponse(boolean wasSuccessful) {
		/*
		 * If we have been successful store the STATE_GOT_SUCCESSFUL_RESPONSE flag inside the preferences and flush them
		 * to permanent storage.
		 */
		if (wasSuccessful) {
			Editor prefsEditor = mPrefs.edit();
			prefsEditor.putBoolean(STATE_GOT_SUCCESSFUL_RESPONSE_KEY, true);
			prefsEditor.commit();
		}
	}
}
