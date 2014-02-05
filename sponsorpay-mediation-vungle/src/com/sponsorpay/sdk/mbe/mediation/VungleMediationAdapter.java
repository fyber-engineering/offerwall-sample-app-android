/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mbe.mediation;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.vungle.sdk.VunglePub;
import com.vungle.sdk.VunglePub.EventListener;

public class VungleMediationAdapter extends SPMediationAdapter implements EventListener {
			
	private static final float MIN_PLAY_REQUIRED = 0.1f;

	private static final String TAG = "VungleAdapter";

	private static final String ADAPTER_VERSION = "1.0.1";

	private static final String ADAPTER_NAME = "Vungle";
	
	private static final String APP_ID = "app.id";
	private static final String SHOW_CLOSE_BUTTON = "show.close.button";
	private static final String SOUND_ENABLED = "sound.enabled";
	private static final String AUTO_ROTATION_ENABLED = "auto.rotation.enabled";
	private static final String BACK_BUTTON_ENABLED = "back.button.enabled";
	private static final String VIDEO_WATCHED_AT = "video.considered.watched.at";

	private float mVideoWatchedAt;
	

	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Vungle adapter - SDK version " + VunglePub.getVersionString());
		String appId = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_ID, String.class);
		if (StringUtils.notNullNorEmpty(appId)) {
			SponsorPayLogger.i(TAG, "Using App ID = " + appId);
			VunglePub.setAutoRotation(SPMediationConfigurator.getConfiguration(
					ADAPTER_NAME, AUTO_ROTATION_ENABLED, Boolean.FALSE,	Boolean.class));
			setVideoWatchedAt();
			VunglePub.init(activity, appId);
			VunglePub.setSoundEnabled(SPMediationConfigurator.getConfiguration(
					ADAPTER_NAME, SOUND_ENABLED, Boolean.TRUE, Boolean.class));
			VunglePub.setBackButtonEnabled(SPMediationConfigurator.getConfiguration(
					ADAPTER_NAME, BACK_BUTTON_ENABLED, Boolean.FALSE, Boolean.class));
			VunglePub.setEventListener(this);
			return true;
		}
		SponsorPayLogger.d(TAG, "App Id  must have a valid value!");
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
		sendValidationEvent(VunglePub.isVideoAvailable() ? SPTPNValidationResult.SPTPNValidationSuccess
				: SPTPNValidationResult.SPTPNValidationNoVideoAvailable);
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		if (VunglePub.isVideoAvailable()) {
			Boolean showClose = SPMediationConfigurator.getConfiguration(
					ADAPTER_NAME, SHOW_CLOSE_BUTTON, Boolean.FALSE,	Boolean.class);
			VunglePub.displayIncentivizedAdvert(showClose);
		} else {
			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventNoVideo);
			clearVideoEvent();
		}
	}


	private void setVideoWatchedAt() {
		try {
			mVideoWatchedAt = Float.parseFloat(SPMediationConfigurator
					.getConfiguration(ADAPTER_NAME, VIDEO_WATCHED_AT,
							"0.9", String.class));
			if (mVideoWatchedAt < MIN_PLAY_REQUIRED) {
				mVideoWatchedAt = MIN_PLAY_REQUIRED;
			} else if (mVideoWatchedAt > 1) {
				mVideoWatchedAt = 1;
			}
		} catch (NumberFormatException e) {
			mVideoWatchedAt = 0.9f;
		}
	}

	
	
	// Vungle EventListener interface 
	@Override
	public void onVungleAdEnd() {
		// this is fired before onVungleView method
//		notifyCloseEngagement();
	}

	@Override
	public void onVungleAdStart() {
		notifyVideoStarted();
	}

	@Override
	public void onVungleView(double watchedSeconds, double totalAdSeconds) {
        final double watchedPercent = watchedSeconds / totalAdSeconds;
        if (watchedPercent >= mVideoWatchedAt) {
        	// we notify only about the finished event tdue to the lack of event separation
        	// the mediation offer will do the conversion and the close
        	sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventFinished);
        } else {
        	sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventAborted);
        }
        clearVideoEvent();
	}
	
}
