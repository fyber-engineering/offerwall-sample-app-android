/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import android.app.Activity;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;

import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.mediation.mbe.FlurryVideoMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

public class FlurryMediationAdapter extends SPMediationAdapter implements FlurryAdListener{
			
	private static final String TAG = "FlurryAdapter";

	private static final String ADAPTER_VERSION = "2.1.0";
	private static final String ADAPTER_NAME = "FlurryAppCircleClips";
	private static final String API_KEY = "api.key";

	private WeakReference<Activity> actRef;
	
	private FlurryVideoMediationAdapter mVideoMediationAdapter = new FlurryVideoMediationAdapter(this);
	
	private HashSet<FlurryAdListener> mFlurryListeners = new HashSet<FlurryAdListener>();
	
	@Override
	public boolean startAdapter(Activity activity) {
		actRef = new WeakReference<Activity>(activity);
		SponsorPayLogger.d(TAG, "Starting Flurry adapter - SDK version " + FlurryAgent.getReleaseVersion());
		final String apiKey = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, API_KEY, String.class);
		if (StringUtils.notNullNorEmpty(apiKey)) {
			SponsorPayLogger.i(TAG, "Using API key = " + apiKey);
			activity.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					//onStartSession must be called from the UI thread.
					FlurryAgent.onStartSession(actRef.get(), apiKey);
					FlurryAgent.addOrigin("SponsorPayAndroid", "SPONSORPAY_SDK_VERSION");
				}
				
			});
			
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
	public SPInterstitialMediationAdapter<SPMediationAdapter> getInterstitialMediationAdapter() {
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
