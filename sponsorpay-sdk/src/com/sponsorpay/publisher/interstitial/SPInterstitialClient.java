/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.mediation.SPMediationCoordinator;
import com.sponsorpay.utils.SponsorPayLogger;

/**
 * <p>
 * Provides methods to request and show Interstitial Ads and notifies its
 * listener {@link SPInterstitialAdListener} of the ad status result.
 * </p>
 * 
 * Before requesting an ad, make sure the have called the {@link
 * SponsorPay#start(String, String, String, Activity)} method. At this point you
 * can determine if offers are available with {@link #requestAds(SPCredentials, Activity)}.
 * 
 * When an ad is closed, you must restart the process, querying for new ads
 * before being able to display any ad again.
 * 
 * Note - Offer availability ({@link #requestAds(SPCredentials, Activity)}) cannot 
 * be requested while an ad is being displayed. Call {@link #canRequestAds()} if 
 * you're not sure that this instance is in a state in which a request for offers 
 * is possible.
 */
public class SPInterstitialClient {

	private static final String TAG = "SPInterstitialClient";

	/**
	 * Singleton instance of {@link SPInterstitialClient}
	 */
	public static final SPInterstitialClient INSTANCE = new SPInterstitialClient();
	
	public static final String SP_REQUEST_ID_PARAMETER_KEY = "request_id";
	
	private Map<String, String> mCustomParameters;

	private SPInterstitialClientState mState;

	private SPInterstitialAd mAd;

	private Activity mActivity;
	private SPCredentials mCredentials;
	private String mRequestId;
	
	private SPInterstitialAdListener mAdStateListener;
	private SPInterstitialRequestListener mRequestListener;
	
	private SPInterstitialClient() {
		mState = SPInterstitialClientState.READY_TO_CHECK_OFFERS;
	}
	
	/**
	 * Queries the server for Interstitial ads availability. 
	 * 
	 * Ads availability cannot be requested while an ad is being displayed or the
	 * server is currently being queried. Call {@link #canRequestAds()} if you're not
	 * sure that this instance is in a state in which a request for ads is possible.
	 * 
	 * @param credentials
	 * 			The credentials that will be used for this query. 
	 * 			@see SPCredentials
	 * 			@see SponsorPay#start(String, String, String, Activity)
	 * @param activity
	 * 			The calling activity
	 * @return true if a request is being made, false otherwise
	 */
	public boolean requestAds(SPCredentials credentials, Activity activity) {
		if (canRequestAds()) {
			startQueryingForAds(credentials, activity);
			return true;
		} else {
			SponsorPayLogger.d(TAG, "SPInterstitialClient cannot request offers at this point. " +
					"It might be requesting offers right now or an offer might be currently " +
					"being presented to the user.");
			return false;
		}
	}

	private void startQueryingForAds(SPCredentials credentials, Activity activity) {
		mAd = null;
		mCredentials = credentials;
		mActivity = activity;
		mRequestId = UUID.randomUUID().toString();
		SPInterstitialRequester.requestAds(credentials, mRequestId, mCustomParameters);
		setState(SPInterstitialClientState.REQUESTING_OFFERS);
	}

	/**
	 * @return true if the client is in a state that allows to request for
	 *         ads, false otherwise
	 */
	public boolean canRequestAds() {
		return mState.canRequestAds();
	}

	private boolean canChangeParameters() {
		return mState.canChangeParameters();
	}
	
	/**
	 * Sets the additional custom parameters used for this ad.
	 * 
	 * @param parameters
	 * 			The additional parameters map
	 * 
	 * @return true if successful in setting the parameters, false otherwise
	 */
	public boolean setCustomParameters(Map<String, String> parameters) {
		if (canChangeParameters()) {
			mCustomParameters = parameters;
			setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
			return true;
		} else {
			SponsorPayLogger.d(TAG, "Cannot change custom parameters while a request to the " +
					"server is going on or an offer is being presented to the user.");
			return false;
		}
	}

	private void setState(SPInterstitialClientState newState) {
		mState = newState;
		switch (mState) {
		case READY_TO_CHECK_OFFERS:
			mActivity = null;
			break;
		case READY_TO_SHOW_OFFERS:
			break;
		case SHOWING_OFFERS:
			break;
		default:
			break;
		}
	}

	public void processAds(SPInterstitialAd[] ads) {
		setState(SPInterstitialClientState.VALIDATING_OFFERS);
		SPInterstitialAdsProcessor.processAds(ads);
	}

	/**
	 * Method called to set the ad to be shown when {@link #showInterstitial(Activity)}
	 * is called
	 * 
	 * @param ad
	 * 			the ad to be shown
	 */
	public void availableAd(SPInterstitialAd ad) {
		if (ad != null) {
			mAd = ad;
			if(mRequestListener != null) {
				mRequestListener.onSPInterstitialAdAvailable(new Intent(mActivity, SPInterstitialActivity.class));
			}
			setState(SPInterstitialClientState.READY_TO_SHOW_OFFERS);
			// listener offer available true
		} else {
			if(mRequestListener != null) {
				mRequestListener.onSPInterstitialAdNotAvailable();
			}
			setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
			// listener offer available false
		}
	}

	/**
	 * Called to validate if an ad from the mediated network is avaialble
	 * 
	 * @param ad
	 * 			the ad to be validated
	 * @return
	 * 			true if an ad is available, false otherwise
	 */
	public boolean validateAd(SPInterstitialAd ad) {
		return SPMediationCoordinator.INSTANCE.validateInterstitialNetwork(
				mActivity, ad);
	}
	
	/**
	 * Starts an available interstitial ad.
	 * 
	 * @param parentActivity
	 * 			The activity that will be used to show the engagement
	 * 
	 * @return true if the ad can be shown
	 */
	public boolean showInterstitial(Activity parentActivity) {
		if (mState.canShowAds()) {
			boolean showAd = SPMediationCoordinator.INSTANCE.showInterstitial(parentActivity,
					mAd);
			if (showAd) {
				if (mAdStateListener != null) {
					mAdStateListener.onSPInterstitialAdShown();
				}
				setState(SPInterstitialClientState.SHOWING_OFFERS);
			}
			return showAd;
		} else {
			return false;
		}
	}

	/**
	 * Fire Interstitial events, adding the request_id to the event
	 *  
	 * @param ad
	 * 			the ad to which the event is related
	 * @param event
	 * 			the event to be fired
	 */
	public void fireEvent(SPInterstitialAd ad,
			SPInterstitialEvent event) {
		fireEvent(ad, event, null);
	}
	
	/**
	 * Fire Interstitial events, adding the request_id to the event
	 *  
	 * @param ad
	 * 			the ad to which the event is related
	 * @param event
	 * 			the event to be fired
	 * @param message
	 * 			the message to be sent
	 */
	public void fireEvent(SPInterstitialAd ad,
			SPInterstitialEvent event, String message) {
		SPInterstitialEventDispatcher.trigger(mCredentials, mRequestId, ad, event);
		switch (event) {
		case ShowClick:
			setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
			if (mAdStateListener != null) {
				mAdStateListener.onSPInterstitialAdClosed(SPInterstitialAdCloseReason.ReasonUserClickedOnAd);
			}
			break;
		case ShowClose:
			setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
			if (mAdStateListener != null) {
				mAdStateListener.onSPInterstitialAdClosed(SPInterstitialAdCloseReason.ReasonUserClosedAd);
			}
			break;
		case ValidationError:
		case ShowError:
			setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
			SponsorPayLogger.d(TAG, "An error occurred. Message: " + message);
			if (mAdStateListener != null) {
				mAdStateListener.onSPInterstitialAdError(message);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Sets the request listener to be notified of the ad request
	 *
	 * @param listener
	 */
	public void setRequestListener(SPInterstitialRequestListener listener) {
		mRequestListener = listener;
	}
	
	/**
	 * Sets the ad status listener to be notified of the ad status changes
	 * @param listener
	 */
	public void setAdStateListener(SPInterstitialAdListener listener) {
		mAdStateListener = listener;
	}
	
}
