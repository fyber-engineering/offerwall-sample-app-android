/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.mbe;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.mediation.VungleMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.utils.SponsorPayLogger;
import com.vungle.publisher.EventListener;
import com.vungle.publisher.VunglePub;


public class VungleVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<VungleMediationAdapter> implements
		EventListener {
	
	public VungleVideoMediationAdapter(VungleMediationAdapter adapter) {
		super(adapter);
	}

	@Override
	public void videosAvailable(Context context) {
		sendValidationEvent(VunglePub.getInstance().isCachedAdAvailable() ? SPTPNVideoValidationResult.SPTPNValidationSuccess
				: SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		VunglePub vunglePub = VunglePub.getInstance();
		if (vunglePub.isCachedAdAvailable()) {
			vunglePub.playAd();
		} else {
			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventNoVideo);
			clearVideoEvent();
		}
	}
	
	// Vungle EventListener interface 
	@Override
	public void onAdEnd() {
		// this is fired before onVungleView method
//		notifyCloseEngagement();
	}

	@Override
	public void onAdStart() {
		notifyVideoStarted();
	}

	@Override
	public void onCachedAdAvailable() {
		SponsorPayLogger.d(getName(), "onCachedAdAvailable");
	}

	@Override
	public void onVideoView(boolean isCompletedView, int watchedMillis, int videoDurationMillis) {
        if (isCompletedView) {
        	// we notify only about the finished event due to the lack of event separation
        	// the mediation offer will do the conversion and the close
        	sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventFinished);
        } else {
        	sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventAborted);
        }
        // we have to clear the video event manually
        clearVideoEvent();
	}
	
}
