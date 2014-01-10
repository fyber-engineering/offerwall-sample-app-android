/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial.mediation;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialClient;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialEvent;

public abstract class SPInterstitialMediationAdapter<V extends SPMediationAdapter> {

	protected V mAdapter;
	private SPInterstitialAd mAd;
	private boolean mAdAvailable;
	private Activity mActivity;

	public SPInterstitialMediationAdapter(V adapter) {
		mAdapter = adapter;
	}

	public abstract boolean interstitialAvailable(Context context,
			SPInterstitialAd ad);

	public boolean show(Activity parentActivity,
			SPInterstitialAd ad) {
		if (isAdAvailable()) {
			mActivity = parentActivity;
			mAd = ad;
			return show(parentActivity);
		}
		interstitialAvailable(parentActivity, ad);
		return false;
	}
	
	public abstract boolean show(Activity parentActivity);
	
	// Convenience methods
	
	protected void setAdAvailable() {
		mAdAvailable = true;
	}
	
	protected boolean isAdAvailable() {
		return mAdAvailable;
	}
	
	protected void requestAd() {
		interstitialAvailable(mActivity, null);
		mAd = null;
		mAdAvailable = false;
		mActivity = null;
//		interstitialAvailable(context, ad)
	}
	
	protected void fireImpressionEvent() {
		fireEvent(SPInterstitialEvent.ShowImpression);
	}
	
	protected void fireClickEvent() {
		fireEvent(SPInterstitialEvent.ShowClick);
	}
	
	protected void fireCloseEvent() {
		fireEvent(SPInterstitialEvent.ShowClose);
//		mAd = null;
		requestAd();
	}
	
	protected void fireErrorEvent() {
		fireEvent(SPInterstitialEvent.Error);
		requestAd();
//		mAd = null;
	}
	
	private void fireEvent(SPInterstitialEvent event) {
		if (mAd != null) {
			SPInterstitialClient.INSTANCE.fireEvent(mAd, event);
		} else {
			//FIXME log
		}
	}

}
