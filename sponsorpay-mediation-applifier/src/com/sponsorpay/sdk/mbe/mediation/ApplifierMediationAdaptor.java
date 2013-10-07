package com.sponsorpay.sdk.mbe.mediation;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.applifier.impact.android.ApplifierImpact;
import com.applifier.impact.android.IApplifierImpactListener;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationAdaptor;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationValidationEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class ApplifierMediationAdaptor implements SPMediationAdaptor, IApplifierImpactListener{

	private static final String TAG = "ApplifierAdaptor";

	private static final String ADAPTOR_VERSION = "1.0.0";
//	public static final String ADATPOR_NAME = "MockMediatedNetwork";
	private static final String ADATPOR_NAME = "Applifier";

	public static final Object GAME_ID_KEY = "game.id.key";

	private boolean campaignAvailable;

	private SPMediationVideoEvent videoEvent;
	private Map<String, String> videoContextData;

	private boolean videoSkipped = true;

	@Override
	public boolean startAdaptor(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Applifier adaptor - SDK version " + ApplifierImpact.getSDKVersion());
		Map<String, Object> configs = SPMediationConfigurator.INSTANCE.getConfigurationForAdaptor(ADATPOR_NAME);
		new ApplifierImpact(activity, configs.get(GAME_ID_KEY).toString(), this);
		return true;
	}

	@Override
	public String getName() {
		return ADATPOR_NAME;
	}

	@Override
	public String getVersion() {
		return ADAPTOR_VERSION;
	}

	@Override
	public void videosAvailable(Context context, SPMediationValidationEvent event,
			Map<String, String> contextData) {
		// lower case validation name bug
		event.validationEventResult(getName().toLowerCase(),
				campaignAvailable ? SPTPNValidationResult.SPTPNValidationSuccess
						: SPTPNValidationResult.SPTPNValidationNoVideoAvailable,
				contextData);
	}

	@Override
	public void startVideo(Activity parentActivity,
			SPMediationVideoEvent event, Map<String, String> contextData) {
		if (ApplifierImpact.instance.canShowCampaigns()) {
			videoSkipped = true;
			videoEvent = event;
			videoContextData = contextData;
			
			ApplifierImpact.instance.changeActivity(parentActivity);
			Map<String, Object> optionsMap = getOptionsMaps();
			if (!ApplifierImpact.instance.showImpact(optionsMap)) {
				event.videoEventOccured(getName(), SPTPNVideoEvent.SPTPNVideoEventError, contextData);
			}
		} else {
			event.videoEventOccured(getName(), SPTPNVideoEvent.SPTPNVideoEventError, contextData);
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
		if (videoSkipped) {
			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventAborted);
		} else {
			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventFinished);
		}
		videoEvent = null;
		videoContextData = null;
	}

	@Override
	public void onImpactOpen() {
		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventStarted);
	}

	@Override
	public void onVideoCompleted(String rewardItemKey, boolean skipped) {
		videoSkipped  = skipped;
	}

	@Override
	public void onVideoStarted() {
		// if we send the event here, then we can have a 
		// START event timeout because of the carousel
//		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventStarted);
	}
	
	//HELPER method
	private void sendVideoEvent(SPTPNVideoEvent event) {
		if (videoEvent != null) {
			videoEvent.videoEventOccured(getName(),
					event, videoContextData);
		}
	}
	
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
		Map<String, Object> config = SPMediationConfigurator.INSTANCE.getConfigurationForAdaptor(ADATPOR_NAME);
		return config != null ? config.get(key) : null;
	}

}
