/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.interstitial;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMInterstitial;
import com.inmobi.monetization.IMInterstitialListener;
import com.sponsorpay.mediation.InMobiMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class InMobiIntersitialMediationAdapter extends
		SPInterstitialMediationAdapter<InMobiMediationAdapter> implements IMInterstitialListener{

	private IMInterstitial mInterstitial;

	public InMobiIntersitialMediationAdapter(InMobiMediationAdapter adapter, final Activity activity) {
		super(adapter);
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mInterstitial = new IMInterstitial(activity, mAdapter.getPropertyId());
				mInterstitial.setIMInterstitialListener(InMobiIntersitialMediationAdapter.this);
				checkForAds(null);
			}
		});
	}

	@Override
	protected boolean show(Activity parentActivity) {
		if (mInterstitial.getState() == IMInterstitial.State.READY) {
			mInterstitial.show();
			return true;
		}
		return false;
	}

	@Override
	protected void checkForAds(Context context) {
		if (mInterstitial.getState() != IMInterstitial.State.LOADING) {
			mInterstitial.loadInterstitial();
		}
	}

	@Override
	public void onDismissInterstitialScreen(IMInterstitial ad) {
		fireCloseEvent();
	}

	@Override
	public void onInterstitialFailed(IMInterstitial ad, IMErrorCode errorCode) {
		fireErrorEvent("Interstitial failed to load with errorCode - " + errorCode.toString());
	}

	@Override
	public void onInterstitialInteraction(IMInterstitial ad, Map<String, String> params) {
	}

	@Override
	public void onInterstitialLoaded(IMInterstitial ad) {
		setAdAvailable();
	}

	@Override
	public void onLeaveApplication(IMInterstitial ad) {
		fireClickEvent();
	}

	@Override
	public void onShowInterstitialScreen(IMInterstitial ad) {
		fireImpressionEvent();
	}


}