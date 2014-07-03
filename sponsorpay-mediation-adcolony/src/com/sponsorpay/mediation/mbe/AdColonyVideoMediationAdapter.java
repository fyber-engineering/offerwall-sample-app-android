/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
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
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;

public class AdColonyVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<AdColonyMediationAdapter> implements
		AdColonyV4VCListener, AdColonyAdListener {
	
	private static final String CONFIRMATION_DIALOG = "with.confirmation.dialog";

	private AdColonyV4VCAd mV4VCAd;
	private Boolean mShouldShowConfirmationDialog;

	public AdColonyVideoMediationAdapter(AdColonyMediationAdapter adapter) {
		super(adapter);
		AdColony.addV4VCListener(this);
		mShouldShowConfirmationDialog = shouldShowConfirmationDialog();
	}

	@Override
	public void videosAvailable(Context context) {
		mV4VCAd = new AdColonyV4VCAd().withListener(this);
		if (mV4VCAd.isReady()) {
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationSuccess);
			mV4VCAd.withConfirmationDialog(mShouldShowConfirmationDialog);
		} else {
			mV4VCAd = null;
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
		}
	}

	@Override
	public void startVideo(Activity parentActivity) {
		if (mV4VCAd != null ) {
			AdColony.resume(parentActivity);
			mV4VCAd.show();
			if (mShouldShowConfirmationDialog) {
				notifyVideoStarted();
			}
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
		if (ad.noFill()) {
			notifyVideoError();
		} else {
			notifyCloseEngagement();
		}
		mV4VCAd = null;
		AdColony.pause();
	}

	@Override
	public void onAdColonyAdStarted(AdColonyAd ad) {
		if (!mShouldShowConfirmationDialog) {
			notifyVideoStarted();
		}
	}
	
	// Helper methods
	private Boolean shouldShowConfirmationDialog() {
		Boolean showConfirmationDialog = SPMediationConfigurator.getConfiguration(getName(), 
				CONFIRMATION_DIALOG, Boolean.FALSE, Boolean.class);
		return showConfirmationDialog;
	}

}
