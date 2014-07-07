/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.applovin.sdk.AppLovinSdk;
import com.sponsorpay.mediation.interstitial.AppLovinIntersitialMediationAdapter;
import com.sponsorpay.mediation.mbe.AppLovinVideoMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;


public class AppLovinMediationAdapter extends SPMediationAdapter {

	public static final String TAG = "AppLovinAdapter";
	private static final String ADAPTER_VERSION = "1.2.0";
	private static final String ADAPTER_NAME = "AppLovin";
	
	private static final String SDK_KEY = "applovin.sdk.key";
	private AppLovinIntersitialMediationAdapter mInterstitialAdapter;
	private AppLovinVideoMediationAdapter mVideoAdapter;
	
	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting AppLovin adapter - SDK version " + AppLovinSdk.VERSION);
		String sdkKey = getValueFromAppMetadata(activity);
		if (StringUtils.notNullNorEmpty(sdkKey)) {
			AppLovinSdk.initializeSdk(activity);
			mInterstitialAdapter = new AppLovinIntersitialMediationAdapter(this, activity);
			mVideoAdapter = new AppLovinVideoMediationAdapter(this, activity);
			return true;
		}
		SponsorPayLogger.i(TAG, "SDK key value is not set in the AndroidManifest file of your application.");
		return false;
	}
	
	@Override
	public String getName() {
		return ADAPTER_NAME;
	}
	
	@Override
	public String getVersion() {
		return ADAPTER_VERSION;
	}
	
	@Override
	public AppLovinVideoMediationAdapter getVideoMediationAdapter() {
		return mVideoAdapter;
	}
	
	@Override
	public AppLovinIntersitialMediationAdapter getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}
	
	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}
	
	private Bundle getMetadata(Activity activity) {
        ApplicationInfo ai = null;

        // Extract the meta data from the package manager
		try {
			ai = activity.getPackageManager().getApplicationInfo(
					activity.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return null;
		}

		if (ai.metaData == null) {
			ai.metaData = new Bundle();
		}
		
        return ai.metaData;
	}
	
	private String getValueFromAppMetadata(Activity activity) {
        Object retrievedValue;
        retrievedValue = getMetadata(activity).get(SDK_KEY);
        return retrievedValue == null ? null : retrievedValue.toString();
	}

}
