/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.mbe;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.mediation.VungleMediationAdapter;
import com.vungle.sdk.VunglePub;
import com.vungle.sdk.VunglePub.EventListener;

public class VungleVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<VungleMediationAdapter> implements
		EventListener {
			
	private static final float MIN_PLAY_REQUIRED = 0.1f;

	private static final String SHOW_CLOSE_BUTTON = "show.close.button";
	private static final String VIDEO_WATCHED_AT = "video.considered.watched.at";

	private float mVideoWatchedAt;
	
	public VungleVideoMediationAdapter(VungleMediationAdapter adapter) {
		super(adapter);
		setVideoWatchedAt();
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
					getName(), SHOW_CLOSE_BUTTON, Boolean.FALSE,	Boolean.class);
			VunglePub.displayIncentivizedAdvert(showClose);
		} else {
			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventNoVideo);
			clearVideoEvent();
		}
	}

	private void setVideoWatchedAt() {
		try {
			mVideoWatchedAt = Float.parseFloat(SPMediationConfigurator
					.getConfiguration(getName(), VIDEO_WATCHED_AT,
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
