/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation;

import java.util.Set;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.applovin.sdk.AppLovinSdk;
import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.mediation.interstitial.AppLovinIntersitialMediationAdapter;


public class AppLovinMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "AppLovinAdapter";
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "AppLovin";
	
	private static final String SDK_KEY = "applovin.sdk.key";
	private AppLovinIntersitialMediationAdapter mInterstitialAdapter;
	
	
	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting AppLovin adapter - SDK version " + AppLovinSdk.VERSION);
//		String sdkKey = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, SDK_KEY, String.class);
//		if (StringUtils.notNullNorEmpty(sdkKey)) {
//			storeMetadata(activity, sdkKey);
//		} else {
//			sdkKey = getValueFromAppMetadata(activity);
//		}
		String sdkKey = getValueFromAppMetadata(activity);
		if (StringUtils.notNullNorEmpty(sdkKey)) {
			AppLovinSdk.initializeSdk(activity);
			mInterstitialAdapter = new AppLovinIntersitialMediationAdapter(this, activity);
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
	public SPBrandEngageMediationAdapter<SPMediationAdapter> getVideoMediationAdapter() {
		return null;
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
	
//	private void storeMetadata(Activity activity, String value) {
//		getMetadata(activity).putString(SDK_KEY, value);
//	}
	
	private String getValueFromAppMetadata(Activity activity) {
        Object retrievedValue;

        retrievedValue = getMetadata(activity).get(SDK_KEY);

        return retrievedValue == null ? null : retrievedValue.toString();
	}

}
