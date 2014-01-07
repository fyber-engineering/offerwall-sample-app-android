/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import android.app.Activity;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.mediation.mbe.FlurryVideoMediationAdapter;

public class FlurryMediationAdapter extends SPMediationAdapter implements FlurryAdListener{
			
	private static final String TAG = "FlurryAdapter";

	private static final String ADAPTER_VERSION = "2.0.0";
	private static final String ADAPTER_NAME = "FlurryAppCircleClips";
	private static final String API_KEY = "api.key";

	private WeakReference<Activity> actRef;
	
	private FlurryVideoMediationAdapter mVideoMediationAdapter = new FlurryVideoMediationAdapter(this);
	
	private HashSet<FlurryAdListener> mFlurryListeners = new HashSet<FlurryAdListener>();
	
	@Override
	public boolean startAdapter(Activity activity) {
		actRef = new WeakReference<Activity>(activity);
		SponsorPayLogger.d(TAG, "Starting Flurry adapter - SDK version " + FlurryAgent.getReleaseVersion());
		String apiKey = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, API_KEY, String.class);
		if (StringUtils.notNullNorEmpty(apiKey)) {
			SponsorPayLogger.i(TAG, "Using API key = " + apiKey);
			FlurryAgent.onStartSession(actRef.get(), apiKey);
			FlurryAds.setAdListener(this);
			mFlurryListeners.add(mVideoMediationAdapter);
			return true;
		}
		SponsorPayLogger.d(TAG, "API key must have a valid value!");
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
	public FlurryVideoMediationAdapter getVideoMediationAdapter() {
		return mVideoMediationAdapter;
	}
	
	@Override
	public SPInterstitialMediationAdapter getInterstitialMediationAdapter() {
		return null;
	}
	
	@Override
	protected HashSet<FlurryAdListener> getListeners() {
		return  mFlurryListeners;
	}

	// FlurryAdListener
	@Override
	public void onAdClicked(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public void onAdClosed(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public void onAdOpened(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public void onApplicationExit(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public void onRendered(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public void onRenderFailed(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public void onVideoCompleted(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public boolean shouldDisplayAd(String adSpaceName, FlurryAdType type) {
		return true;
	}

	@Override
	public void spaceDidFailToReceiveAd(String adSpaceName) {
		notifyListeners(adSpaceName);
	}

	@Override
	public void spaceDidReceiveAd(String adSpaceName) {
		notifyListeners(adSpaceName);
	}
	

	public WeakReference<Activity> getActRef() {
		return actRef;
	}

}
