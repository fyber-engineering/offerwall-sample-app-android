/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mbe.mediation;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.applifier.impact.android.ApplifierImpact;
import com.applifier.impact.android.IApplifierImpactListener;
import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.mediation.SPMediationEngagementEvent;
import com.sponsorpay.sdk.android.mediation.SPMediationFormat;
import com.sponsorpay.sdk.android.mediation.SPMediationValidationEvent;
import com.sponsorpay.sdk.android.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class ApplifierMediationAdapter extends SPBrandEngageMediationAdapter implements SPMediationAdapter, IApplifierImpactListener{

	public ApplifierMediationAdapter() {
		super(null);
		mAdapter = this;
	}

	private static final String TAG = "ApplifierAdapter";

	private static final String ADAPTER_VERSION = "2.0.0";
	
	private static final String ADAPTER_NAME = "Applifier";

	public static final String GAME_ID_KEY = "game.id.key";

	private boolean campaignAvailable;

	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Applifier adapter - SDK version " + ApplifierImpact.getSDKVersion());
		String gameKey = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, GAME_ID_KEY, String.class);
		if (StringUtils.notNullNorEmpty(gameKey)) {
			new ApplifierImpact(activity, gameKey, this);
			return true;
		}
		SponsorPayLogger.i(TAG, "Game key value is not valid");
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
	public void videosAvailable(Context context) {
		sendValidationEvent(campaignAvailable ? SPTPNValidationResult.SPTPNValidationSuccess
						: SPTPNValidationResult.SPTPNValidationNoVideoAvailable);
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
		Map<String, Object> config = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(ADAPTER_NAME);
		return config != null ? config.get(key) : null;
	}

	@Override
	public boolean supportMediationFormat(SPMediationFormat format) {
		switch (format) {
		case BrandEngage:
			return true;
		default:
			return false;
		}
	}

	@Override
	public void validate(Context context, SPMediationFormat adFormat,
			SPMediationValidationEvent validationEvent,
			HashMap<String, String> contextData) {
		switch (adFormat) {
		case BrandEngage:
			videosAvailable(context, validationEvent, contextData);
			break;
		default:
			validationEvent.validationEventResult(getName(), SPTPNValidationResult.SPTPNValidationAdapterNotIntegrated, contextData);
		}
	}

	@Override
	public void startEngagement(Activity parentActivity,
			SPMediationFormat adFormat,
			SPMediationEngagementEvent engagementEvent,
			HashMap<String, String> contextData) {
		switch (adFormat) {
		case BrandEngage:
			startVideo(parentActivity, (SPMediationVideoEvent) engagementEvent, contextData);
			break;
		default:
//			engagementEvent.validationEventResult(getName(), SPTPNValidationResult.SPTPNValidationAdapterNotIntegrated, contextData);
		}
	}

}
