/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mbe.mediation;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationAdaptor;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class FlurryMediationAdaptor extends SPMediationAdaptor implements FlurryAdListener{
			
	private static final String TAG = "FlurryAdaptor";

	private static final String ADAPTOR_VERSION = "1.0.0";

	private static final String ADAPTOR_NAME = "flurryappcircleclips";
	
	private static final String API_KEY = "api.key";
	private static final String AD_NAME_SPACE = "ad.name.space";
	private static final String AD_NAME_TYPE = "ad.name.type";

	private FrameLayout mLayout;

	@Override
	public boolean startAdaptor(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Flurry adaptor - SDK version " + FlurryAgent.getReleaseVersion());
		String apiKey = SPMediationConfigurator.getConfiguration(ADAPTOR_NAME, API_KEY, String.class);
		if (StringUtils.notNullNorEmpty(apiKey)) {
			SponsorPayLogger.i(TAG, "Using API key = " + apiKey);
			FlurryAgent.onStartSession(activity, apiKey);
			FlurryAds.setAdListener(this);
			return true;
		}
		SponsorPayLogger.d(TAG, "API key must have a valid value!");
		return false;
	}

	@Override
	public String getName() {
		return ADAPTOR_NAME;
	}

	@Override
	public String getVersion() {
		return ADAPTOR_VERSION;
	}

	@Override
	public void videosAvailable(Context context) {
		mLayout = new FrameLayout(context);
		FlurryAds.fetchAd(context, getAdSpaceFromConfig(), mLayout,
				FlurryAdSize.valueOf(SPMediationConfigurator.getConfiguration(
						ADAPTOR_NAME, AD_NAME_TYPE, String.class)));
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		if (FlurryAds.isAdReady(getAdSpaceFromConfig())) {
			
			mLayout = new FrameLayout(parentActivity);
	
			if (Build.VERSION.SDK_INT > 10) {
				// REALLY BAD WORKAROUND
				// from Honeycomb onwards, the start activity must take place 
				// on the same context in which the fetchad method occured 
				FlurryAds.fetchAd(parentActivity, getAdSpaceFromConfig(), 
						mLayout, FlurryAdSize.FULLSCREEN);
			}
			parentActivity.addContentView(mLayout, new LayoutParams(
					LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			
			FlurryAds.displayAd(parentActivity, getAdSpaceFromConfig(),
					mLayout);
		} else {
			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventNoVideo);
			clearVideoEvent();
		}
	}

	// FlurryAdListener
	@Override
	public void onAdClicked(String adSpaceName) {
	}

	@Override
	public void onAdClosed(String adSpaceName) {
		// send video event finished or aborted
		notifyCloseEngagement();
		mLayout = null;
	}

	@Override
	public void onAdOpened(String adSpaceName) {
		// send video event started
		if (adSpaceName.equals(getAdSpaceFromConfig())) {
			notifyVideoStarted();
		}
	}

	@Override
	public void onApplicationExit(String adSpaceName) {
	}

	@Override
	public void onRenderFailed(String adSpaceName) {
		// send video event error
		notifyVideoError();
	}

	@Override
	public void onVideoCompleted(String adSpaceName) {
		// store video played = true
		if (adSpaceName.equals(getAdSpaceFromConfig())) {
			setVideoPlayed();
		}
	}

	@Override
	public boolean shouldDisplayAd(String adSpaceName, FlurryAdType type) {
		// always return true. if some non video offer are shown, then we must ask 
		// flurry to filter them on their side
		return true;
	}

	@Override
	public void spaceDidFailToReceiveAd(String adSpaceName) {
		// send validate event no video
		sendValidationEvent(SPTPNValidationResult.SPTPNValidationNoVideoAvailable);
	}

	@Override
	public void spaceDidReceiveAd(String adSpaceName) {
		// send validate event success
		if (adSpaceName.equals(getAdSpaceFromConfig())) {
			sendValidationEvent(SPTPNValidationResult.SPTPNValidationSuccess);
		} else {
			sendValidationEvent(SPTPNValidationResult.SPTPNValidationNoVideoAvailable);
		}
	}
	
	//HELPER method
	private String getAdSpaceFromConfig() {
		return SPMediationConfigurator.getConfiguration(ADAPTOR_NAME, AD_NAME_SPACE, String.class);
	}
	
}
