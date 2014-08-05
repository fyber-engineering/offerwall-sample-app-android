/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial.mediation;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.mediation.SPMediationCoordinator;
import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.interstitial.SPInterstitialClient;
import com.sponsorpay.publisher.interstitial.SPInterstitialEvent;

/**
 * <p>
 * Base class for Interstitial Mediation adapter
 * </p>
 * 
 * This class defines the required methods to every adapter and provides convenience methods
 * handling validation and events notifications. The {@link SPMediationCoordinator} will
 * communicate the results back to the {@link SPInterstitialClient}.
 * 
 */
public abstract class SPInterstitialMediationAdapter<V extends SPMediationAdapter> {

	/**
	 * The base {@link SPMediationAdapter} for this network
	 */
	protected V mAdapter;
	
	/**
	 * {@link WeakReference} to the {@link Activity} used for the current
	 * checks. 
	 */
	protected WeakReference<Activity> mActivityRef;

	/*
	 * The interstitial ad to be shown
	 */
	private SPInterstitialAd mAd;
	
	/*
	 * Internal state indicating that an ad is available for this network
	 */
	private boolean mAdAvailable;
	
	/*
	 * Internal helper storing if the ad has been clicked by the user
	 */
	private boolean mHasBeenClicked = false;

	
	public SPInterstitialMediationAdapter(V adapter) {
		mAdapter = adapter;
	}
	
	/* ======================================================
	 *              Adapter specific methods
	 * ======================================================
	 */

	/**
	 * Instructs the wrapped network SDK to show the interstitial ad.
	 * 
	 * @param parentActivity 
	 * 		If the wrapped SDK needs a parent activity, it can use the provided one.
	 * @return
	 * 		boolean indicating that the ad was successfully shown to the user
	 */
	protected abstract boolean show(Activity parentActivity);
	
 	/**
	 * Instructs the wrapped network SDK to check/precache for available ads.
	 *   
	 * @param context
	 * 			The activity context
	 */
	protected abstract void checkForAds(Context context);

	/**
	 * Method called from the {@link SPMediationCoordinator} to check intersitial ads
	 * availability for this network.
	 *   
	 * @param context
	 * 			The activity context
	 * @param ad
	 * 			The {@link SPInterstitialAd} for this query.
	 * @return
	 *			true is an ad is available 
	 */
	public boolean isAdAvailable(Context context,
			SPInterstitialAd ad) {
		if (!isAdAvailable()) {
			mAd = ad;
			checkForAds(context);
			return false;
		}
		return true;
	}
	
	/**
	 * Method called from the {@link SPMediationCoordinator} to start an intersitial
	 * for this network. This method returns a boolean indicating if the ad was shown to
	 * the user.
	 * 
	 * It will stores th {@link SPInterstitialAd}, the {@link Activity} and reset the 
	 * clicked status.
	 * 
	 * @param parentActivity
	 * 			The parent activity 
	 * @param ad
	 * 			The {@link SPInterstitialAd} for this query.
	 * @return
	 *			Boolean indicating that the ad was shown 
	 */
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
	
	/* ======================================================
	 *               Convenience methods
	 * ======================================================
	 */
	
	/**
	 * @return the name of the wrapped network.
	 */
	protected String getName() {
		return mAdapter.getName();
	}
	
	/**
	 * Marks this adapter as having an ad ready to be shown
	 */
	protected void setAdAvailable() {
		mAdAvailable = true;
	}
	
	/**
	 * @return true is an ad is avaialble to be shown
	 */
	private boolean isAdAvailable() {
		return mAdAvailable;
	}
	
	/**
	 * Method that will fire the impression event to SP tracking server.
	 */
	protected void fireImpressionEvent() {
		fireEvent(SPInterstitialEvent.ShowImpression);
	}
	
	/**
	 * Method that will fire the click event to SP tracking server.
	 */
	protected void fireClickEvent() {
		mHasBeenClicked = true;
		fireEvent(SPInterstitialEvent.ShowClick);
	}
	
	/**
	 * Convenience method that fires the close event to SP 
	 * tracking server only if a click event has not occurred before.
	 * 
	 * Additionally, this method resets the internal state of ads 
	 * availability and requests a new ad. 
	 */
	protected void fireCloseEvent() {
		if (!mHasBeenClicked) {
			fireEvent(SPInterstitialEvent.ShowClose);
		}
		resetState();
		checkForAds(getActivity());
	}

	/**
	 * Convenience method that fires the validation error event to SP 
	 * tracking server, resetting the internal ad availability state.
	 */
	protected void fireValidationErrorEvent(String message) {
		resetState();
		fireEvent(SPInterstitialEvent.ValidationError, message);
	}
	
	/**
	 * Convenience method that fires the error event to SP 
	 * tracking server if an ad failed to be shown properly.
	 * 
	 * Additionally, this method resets the internal state of ads 
	 * availability and requests a new ad. 
	 */
	protected void fireShowErrorEvent(String message) {
		resetState();
		fireEvent(SPInterstitialEvent.ShowError, message);
		checkForAds(getActivity());
	}
	
	/* ======================================================
	 *                      Helpers
	 * ======================================================
	 */
	
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
	
	/**
	 * @return the activity stored in the {@link WeakReference}, null if none
	 */
	protected Activity getActivity() {
		if (mActivityRef != null) {
			return mActivityRef.get();
		}
		return null;
	}

}
