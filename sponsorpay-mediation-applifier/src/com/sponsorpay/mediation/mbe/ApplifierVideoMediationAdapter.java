/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.mbe;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.mediation.ApplifierMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.unity3d.ads.android.IUnityAdsListener;
import com.unity3d.ads.android.UnityAds;

public class ApplifierVideoMediationAdapter extends SPBrandEngageMediationAdapter<ApplifierMediationAdapter>
		implements IUnityAdsListener {

	private boolean campaignAvailable;

	public ApplifierVideoMediationAdapter(ApplifierMediationAdapter adapter) {
		super(adapter);
	}

	@Override
	public void videosAvailable(Context context) {
		sendValidationEvent(campaignAvailable ? SPTPNVideoValidationResult.SPTPNValidationSuccess
				: SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
	}

	@Override
	public void startVideo(Activity parentActivity) {
		if (UnityAds.canShowAds()) {
			UnityAds.changeActivity(parentActivity);
			Map<String, Object> optionsMap = getOptionsMaps();
			if (!UnityAds.show(optionsMap)) {
				notifyVideoError();
			}
		} else {
			// assume that no more videos are available
			campaignAvailable = false;
			notifyVideoError();
		}
	}

	// IUnityAdsListener
	@Override
	public void onFetchCompleted() {
		campaignAvailable = true;
	}

	@Override
	public void onFetchFailed() {
		campaignAvailable = false;
	}

	@Override
	public void onHide() {
		notifyCloseEngagement();
	}

	@Override
	public void onShow() {
		notifyVideoStarted();
	}

	@Override
	public void onVideoCompleted(String rewardItemKey, boolean skipped) {
		if (!skipped) {
			setVideoPlayed();
		}
	}

	@Override
	public void onVideoStarted() {
		// if we send the event here, then we can have a
		// START event timeout because of the carousel
		// sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventStarted);
	}

	// HELPER method

	private Map<String, Object> getOptionsMaps() {
		String[] keys = { UnityAds.UNITY_ADS_OPTION_NOOFFERSCREEN_KEY,
				UnityAds.UNITY_ADS_OPTION_OPENANIMATED_KEY,
				UnityAds.UNITY_ADS_OPTION_GAMERSID_KEY,
				UnityAds.UNITY_ADS_OPTION_MUTE_VIDEO_SOUNDS,
				UnityAds.UNITY_ADS_OPTION_VIDEO_USES_DEVICE_ORIENTATION};
		HashMap<String, Object> optionsMap = new HashMap<String, Object>(keys.length);
		for (String key : keys) {
			Object value = getValueFromConfig(key);
			if (value != null) {
				if (!key.equals(UnityAds.UNITY_ADS_OPTION_GAMERSID_KEY)) {
					value = Boolean.parseBoolean(value.toString());
				}
				optionsMap.put(key, value);
			}
		}
		return optionsMap;
	}

	private Object getValueFromConfig(String key) {
		Map<String, Object> config = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(getName());
		return config != null ? config.get(key) : null;
	}

}
