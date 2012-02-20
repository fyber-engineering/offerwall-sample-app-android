package com.sponsorpay.sdk.android.advertiser;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SponsorPayAdvertiserState {
	/**
	 * Shared preferences file name. We store a flag into the shared preferences which is checked on
	 * each consecutive invocation of {@link #register()}, to keep track of whether we have already
	 * successfully contacted the Advertiser API.
	 */
	private static final String PREFERENCES_FILE_NAME = "SponsorPayAdvertiserState";

	/**
	 * The key to store in the preferences file the flag which determines if we have already
	 * successfully contacted the Advertiser API.
	 */
	private static final String STATE_GOT_SUCCESSFUL_RESPONSE_KEY = "SponsorPayAdvertiserState"; // TODO

	private static final String STATE_INSTALL_SUBID_KEY = "InstallSubId";

	private static final String STATE_INSTALL_REFERRER_KEY = "InstallReferrer";

	/**
	 * The shared preferences encoded in the {@link #PREFERENCES_FILE_NAME} file.
	 */
	private SharedPreferences mPrefs;

	public SponsorPayAdvertiserState(Context context) {
		mPrefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
	}

	public void setHasAdvertiserCallbackReceivedSuccessfulResponse(boolean value) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putBoolean(STATE_GOT_SUCCESSFUL_RESPONSE_KEY, value);
		prefsEditor.commit();
	}

	public boolean getHasAdvertiserCallbackReceivedSuccessfulResponse() {
		return mPrefs.getBoolean(STATE_GOT_SUCCESSFUL_RESPONSE_KEY, false);
	}
	
	public void setInstallSubId(String subIdValue) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString(STATE_INSTALL_SUBID_KEY, subIdValue);
		prefsEditor.commit();
	}
	
	public String getInstallSubId() {
		return  mPrefs.getString(STATE_INSTALL_SUBID_KEY, "");
	}
	
	public void setInstallReferrer(String value) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString(STATE_INSTALL_REFERRER_KEY, value);
		prefsEditor.commit();
	}

	public String getInstallReferrer() {
		return  mPrefs.getString(STATE_INSTALL_REFERRER_KEY, "");
	}
}
