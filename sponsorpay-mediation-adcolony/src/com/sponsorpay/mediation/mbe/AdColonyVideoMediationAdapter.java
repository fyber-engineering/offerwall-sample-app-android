/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.mbe;

import android.app.Activity;
import android.content.Context;

import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyAd;
import com.jirbo.adcolony.AdColonyAdListener;
import com.jirbo.adcolony.AdColonyV4VCAd;
import com.jirbo.adcolony.AdColonyV4VCListener;
import com.jirbo.adcolony.AdColonyV4VCReward;
import com.sponsorpay.mediation.AdColonyMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;

public class AdColonyVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<AdColonyMediationAdapter> implements
		AdColonyV4VCListener, AdColonyAdListener {

	private AdColonyV4VCAd mV4VCAd;

	public AdColonyVideoMediationAdapter(AdColonyMediationAdapter adapter) {
		super(adapter);
		AdColony.addV4VCListener(this);
	}

	@Override
	public void videosAvailable(Context context) {
		mV4VCAd = new AdColonyV4VCAd();
		mV4VCAd.withListener(this);
		if (mV4VCAd.isReady()) {
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationSuccess);
		} else {
			mV4VCAd = null;
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
		}
	}

	@Override
	public void startVideo(Activity parentActivity) {
		if (mV4VCAd != null ) {
			mV4VCAd.show();
		} else {
			notifyVideoError();
		}
	}

	// AdColonyV4VCListener
	@Override
	public void onAdColonyV4VCReward(AdColonyV4VCReward reward) {
		if (reward.success()) {
			setVideoPlayed();
		}
	}

	//AdColonyAdListener
	@Override
	public void onAdColonyAdAttemptFinished(AdColonyAd ad) {
		if (ad.notShown() || ad.noFill()) {
			notifyVideoError();
		} else {
			notifyCloseEngagement();
		}
		mV4VCAd = null;
	}

	@Override
	public void onAdColonyAdStarted(AdColonyAd ad) {
		notifyVideoStarted();
	}

}
