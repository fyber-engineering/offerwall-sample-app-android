/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial.mediation;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.interstitial.SPInterstitialClient;
import com.sponsorpay.publisher.interstitial.SPInterstitialEvent;

public abstract class SPInterstitialMediationAdapter<V extends SPMediationAdapter> {

	protected V mAdapter;
	protected WeakReference<Activity> mActivityRef;

	private SPInterstitialAd mAd;
	private boolean mAdAvailable;
	private boolean mHasBeenClicked = false;

	public SPInterstitialMediationAdapter(V adapter) {
		mAdapter = adapter;
	}
	
	protected abstract boolean show(Activity parentActivity);
	
	protected abstract void checkForAds(Context context);

	public boolean isAdAvailable(Context context,
			SPInterstitialAd ad) {
		if (!isAdAvailable()) {
			mAd = ad;
			checkForAds(context);
			return false;
		}
		return true;
	}

	public boolean show(Activity parentActivity,
			SPInterstitialAd ad) {
		if (isAdAvailable()) {
			mHasBeenClicked = false;
			mAd = ad;
			mActivityRef = new WeakReference<Activity>(parentActivity);
			return show(parentActivity);
		}
		checkForAds(parentActivity);
		return false;
	}
	
	// Convenience methods
	
	protected String getName() {
		return mAdapter.getName();
	}
	
	protected void setAdAvailable() {
		mAdAvailable = true;
	}
	
	private boolean isAdAvailable() {
		return mAdAvailable;
	}
	
	protected void fireImpressionEvent() {
		fireEvent(SPInterstitialEvent.ShowImpression);
	}
	
	protected void fireClickEvent() {
		mHasBeenClicked = true;
		fireEvent(SPInterstitialEvent.ShowClick);
	}
	
	protected void fireCloseEvent() {
		if (!mHasBeenClicked) {
			fireEvent(SPInterstitialEvent.ShowClose);
		}
		resetState();
		checkForAds(getActivity());
	}

	protected void fireValidationErrorEvent(String message) {
		resetState();
		fireEvent(SPInterstitialEvent.ValidationError, message);
	}
	
	protected void fireShowErrorEvent(String message) {
		resetState();
		fireEvent(SPInterstitialEvent.ShowError, message);
		checkForAds(getActivity());
	}
	
	private void fireEvent(SPInterstitialEvent event) {
		fireEvent(event, null);
	}
	
	private void fireEvent(SPInterstitialEvent event, String message) {
		if (mAd != null) {
			SPInterstitialClient.INSTANCE.fireEvent(mAd, event, message);
		}
	}
	
	private void resetState() {
		mHasBeenClicked = false;
		mAdAvailable = false;
	}
	
	
	protected Activity getActivity() {
		if (mActivityRef != null) {
			return mActivityRef.get();
		}
		return null;
	}

}
