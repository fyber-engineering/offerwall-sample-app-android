/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.mbe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.sdk.mediation.FlurryMediationAdapter;

public class FlurryVideoMediationAdapter extends SPBrandEngageMediationAdapter<FlurryMediationAdapter> 
	implements FlurryAdListener{

	private static final String AD_NAME_SPACE = "ad.name.space";
	private static final String AD_NAME_TYPE = "ad.name.type";

	private FrameLayout mLayout;

	public FlurryVideoMediationAdapter(FlurryMediationAdapter adapter) {
		super(adapter);
	}
	
	@Override
	public void videosAvailable(Context context) {
		mLayout = new FrameLayout(context);
		mLayout.setBackgroundColor(Color.BLACK);
		FlurryAds.fetchAd(getMediationAdapter().getActRef().get(), getAdSpaceFromConfig(), mLayout,
				getAdSizeFromConfig());
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		final String adSpaceFromConfig = getAdSpaceFromConfig();
		if (FlurryAds.isAdReady(adSpaceFromConfig)) {
			
			parentActivity.addContentView(mLayout, new LayoutParams(
					LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			
			FlurryAds.displayAd(getMediationAdapter().getActRef().get(), adSpaceFromConfig,
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
	public void onRendered(String adSpaceName) {
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
		if (adSpaceName.equals(getAdSpaceFromConfig())) {
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
		}
	}

	@Override
	public void spaceDidReceiveAd(String adSpaceName) {
		// send validate event success
		if (adSpaceName.equals(getAdSpaceFromConfig())) {
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationSuccess);
		}
	}
	
	//HELPER method
	private String getAdSpaceFromConfig() {
		return SPMediationConfigurator.getConfiguration(getName(),
				AD_NAME_SPACE, String.class);
	}
	

	private FlurryAdSize getAdSizeFromConfig() {
		return FlurryAdSize.valueOf(SPMediationConfigurator.getConfiguration(
				getName(), AD_NAME_TYPE, "FULLSCREEN", String.class));
	}
	
}
