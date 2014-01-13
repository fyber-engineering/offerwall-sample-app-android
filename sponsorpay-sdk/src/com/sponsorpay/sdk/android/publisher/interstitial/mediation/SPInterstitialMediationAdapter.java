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
	private boolean mHasClickOccured = false;

	public SPInterstitialMediationAdapter(V adapter) {
		mAdapter = adapter;
	}
	
	public abstract boolean show(Activity parentActivity);

	public abstract boolean interstitialAvailable(Context context,
			SPInterstitialAd ad);

	public boolean show(Activity parentActivity,
			SPInterstitialAd ad) {
		if (isAdAvailable()) {
			mHasClickOccured = false;
			mActivity = parentActivity;
			mAd = ad;
			return show(parentActivity);
		}
		interstitialAvailable(parentActivity, ad);
		return false;
	}
	
	// Convenience methods
	
	protected void setAdAvailable() {
		mAdAvailable = true;
	}
	
	protected boolean isAdAvailable() {
		return mAdAvailable;
	}
	
	protected void requestAd() {
		mAd = null;
		mAdAvailable = false;
		interstitialAvailable(mActivity, null);
		mActivity = null;
//		interstitialAvailable(context, ad)
	}
	
	protected void fireImpressionEvent() {
		fireEvent(SPInterstitialEvent.ShowImpression);
	}
	
	protected void fireClickEvent() {
		mHasClickOccured = true;
		fireEvent(SPInterstitialEvent.ShowClick);
	}
	
	protected void fireCloseEvent() {
		if (!mHasClickOccured) {
			fireEvent(SPInterstitialEvent.ShowClose);
		}
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
