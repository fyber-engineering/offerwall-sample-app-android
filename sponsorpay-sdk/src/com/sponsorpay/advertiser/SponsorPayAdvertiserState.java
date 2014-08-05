/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.advertiser;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sponsorpay.utils.StringUtils;

/**
 * Persists and retrieves the state of the Advertiser part of the SDK.
 */
public class SponsorPayAdvertiserState {
	/**
	 * Shared preferences file name. We store a flag into the shared preferences which is checked on
	 * each consecutive invocation of {@link SponsorPayAdvertiser#register(Context)}, to keep track of whether we have already
	 * successfully contacted the Advertiser API.
	 */
	public static final String PREFERENCES_FILE_NAME = "SponsorPayAdvertiserState";

	/**
	 * The key to store in the preferences file the flag which determines if we have already
	 * successfully contacted the Advertiser API.
	 */
	private static final String STATE_GOT_SUCCESSFUL_RESPONSE_KEY = "SponsorPayAdvertiserState";

	/**
	 * The key to store the install subID in the preferences file
	 */
	private static final String STATE_INSTALL_SUBID_KEY = "InstallSubId";

	/**
	 * The key to store the install referrer in the preferences file
	 */
	private static final String STATE_INSTALL_REFERRER_KEY = "InstallReferrer";

	/**
	 * The shared preferences encoded in the {@link #PREFERENCES_FILE_NAME} file.
	 */
	private SharedPreferences mPrefs;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Android application context, used to gain access to the preferences file.
	 */
	public SponsorPayAdvertiserState(Context context) {
		mPrefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
	}

	/**
	 * Persists the flag which determines if we have already successfully contacted the Advertiser
	 * API.
	 */
	public void setCallbackReceivedSuccessfulResponse(String actionId, boolean value) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString(STATE_GOT_SUCCESSFUL_RESPONSE_KEY + actionId, value ? "1" : "0");
		prefsEditor.commit();
	}

	/**
	 * Retrieves the flag which determines if we have already successfully contacted the Advertiser
	 * API.
	 */
	public String getCallbackReceivedSuccessfulResponse(String actionId) {
		return mPrefs.getString(STATE_GOT_SUCCESSFUL_RESPONSE_KEY + actionId, "0");
	}

	/**
	 * Persists the value of the install subID.
	 */
	public void setInstallSubId(String subIdValue) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString(STATE_INSTALL_SUBID_KEY, subIdValue);
		prefsEditor.commit();
	}

	/**
	 * Retrieves the value of the install subID.
	 */
	public String getInstallSubId() {
		return mPrefs.getString(STATE_INSTALL_SUBID_KEY, StringUtils.EMPTY_STRING);
	}

	/**
	 * Persists the value of the whole install referrer.
	 */
	public void setInstallReferrer(String value) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString(STATE_INSTALL_REFERRER_KEY, value);
		prefsEditor.commit();
	}

	/**
	 * Retrieves the values of the install referrer, typically set when the host app was installed.
	 * 
	 * @return
	 */
	public String getInstallReferrer() {
		return mPrefs.getString(STATE_INSTALL_REFERRER_KEY, StringUtils.EMPTY_STRING);
	}
}
