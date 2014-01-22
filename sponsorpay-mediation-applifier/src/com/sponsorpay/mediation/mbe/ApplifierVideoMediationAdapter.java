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

import com.applifier.impact.android.ApplifierImpact;
import com.applifier.impact.android.IApplifierImpactListener;
import com.sponsorpay.mediation.ApplifierMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;

public class ApplifierVideoMediationAdapter extends SPBrandEngageMediationAdapter<ApplifierMediationAdapter> 
	implements IApplifierImpactListener{
	
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
		if (ApplifierImpact.instance.canShowCampaigns()) {
			ApplifierImpact.instance.changeActivity(parentActivity);
			Map<String, Object> optionsMap = getOptionsMaps();
			if (!ApplifierImpact.instance.showImpact(optionsMap)) {
				notifyVideoError();
			}
		} else {
			//assume that no more videos are available
			campaignAvailable = false;
			notifyVideoError();
		}
	}

	// IApplifierImpactListener
	@Override
	public void onCampaignsAvailable() {
		campaignAvailable = true;
	}

	@Override
	public void onCampaignsFetchFailed() {
		campaignAvailable = false;
	}

	@Override
	public void onImpactClose() {
		notifyCloseEngagement();
	}

	@Override
	public void onImpactOpen() {
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
//		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventStarted);
	}
	
	//HELPER method
	
	private Map<String, Object> getOptionsMaps() {
		String[] keys = {ApplifierImpact.APPLIFIER_IMPACT_OPTION_NOOFFERSCREEN_KEY,
				ApplifierImpact.APPLIFIER_IMPACT_OPTION_OPENANIMATED_KEY,
				ApplifierImpact.APPLIFIER_IMPACT_OPTION_GAMERSID_KEY,
				ApplifierImpact.APPLIFIER_IMPACT_OPTION_MUTE_VIDEO_SOUNDS,
				ApplifierImpact.APPLIFIER_IMPACT_OPTION_VIDEO_USES_DEVICE_ORIENTATION};	
		HashMap<String, Object> optionsMap = new HashMap<String, Object>(keys.length);
		for (String key : keys) {
			Object value = getValueFromConfig(key);
			if (value != null) {
				if (!key.equals(ApplifierImpact.APPLIFIER_IMPACT_OPTION_GAMERSID_KEY)) {
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
