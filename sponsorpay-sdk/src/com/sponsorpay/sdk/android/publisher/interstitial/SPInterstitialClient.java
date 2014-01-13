/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial;

import java.util.Map;
import java.util.UUID;

import android.app.Activity;

import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.mediation.SPMediationCoordinator;
import com.sponsorpay.sdk.android.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.UrlBuilder;

public class SPInterstitialClient {

	private static final String TAG = "SPInterstitialClient";

	public static final SPInterstitialClient INSTANCE = new SPInterstitialClient();
	
	private static final String INTERSTITIAL_URL_KEY = "interstitial";
	private static final String SP_REQUEST_ID_PARAMETER_KEY = "request_id";
	
//	private static final int TIMEOUT = 10000 ;

//	private static final int ENGAGEMENT_EVENT = 1;
//	private static final int VALIDATION_RESULT = 2;

//	private Handler mHandler;

	private Map<String, String> mCustomParameters;

	private SPInterstitialClientState mState;

	private SPInterstitialAd mAd;

	private Activity mActivity;
	private SPCredentials mCredentials;
	private String mRequestId;
	
	private SPInterstitialRequestListener mListener;
	
	private SPInterstitialClient() {
//		mHandler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//				case VALIDATION_RESULT:
//					SponsorPayLogger.d(TAG, "Timeout reached, canceling request...");
////					clearWebViewPage();
//					break;
//				case ENGAGEMENT_EVENT:
//					//something went wrong, show error dialog message
////					showErrorDialog(SponsorPayPublisher
////							.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
//					break;
//				}
//			}
//		};
		mState = SPInterstitialClientState.READY_TO_CHECK_OFFERS;
	}
	
	public boolean requestOffers(SPCredentials credentials, Activity activity) {
		if (canRequestOffers()) {
			startQueryingOffers(credentials, activity);
			return true;
		} else {
			SponsorPayLogger.d(TAG, "SPInterstitialClient cannot request offers at this point. " +
					"It might be requesting offers right now or an offer might be currently " +
					"being presented to the user.");
			return false;
		}
	}

	private void startQueryingOffers(SPCredentials credentials, Activity activity) {
		mCredentials = credentials;
		mActivity = activity;
		mRequestId = UUID.randomUUID().toString();
		String requestUrl = UrlBuilder.newBuilder(getBaseUrl(), credentials)
				.addExtraKeysValues(mCustomParameters)
				.addKeyValue(SP_REQUEST_ID_PARAMETER_KEY, mRequestId)
				.addScreenMetrics().buildUrl();
		SponsorPayLogger.d(TAG, "Loading URL: " + requestUrl);
		loadUrl(requestUrl);
		setState(SPInterstitialClientState.REQUESTING_OFFERS);
		
//		mHandler.sendEmptyMessageDelayed(VALIDATION_RESULT, TIMEOUT);
	}


	private void loadUrl(String requestUrl) {
		new SPInterstitialRequestTask().execute(requestUrl);
	}

	private boolean canRequestOffers() {
		return mState.canRequestOffers();
	}
	
	private boolean canChangeParameters() {
		return mState.canChangeParameters();
	}
	
	/**
	 * Sets the additional custom parameters used for this engagement.
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
	
	private String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(INTERSTITIAL_URL_KEY);
	}

	private void setState(SPInterstitialClientState newState) {
		mState = newState;
		switch (mState) {
		case READY_TO_CHECK_OFFERS:
			mAd = null;
			mActivity = null;
			mRequestId = null;
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
		new SPInterstitialAdsProcessorTask().execute(ads);
	}

	public void availableAd(SPInterstitialAd ad) {
		if (ad != null) {
			mAd = ad;
			if(mListener != null) {
				mListener.onSPInterstitialAdAvailable(true);
			}
			setState(SPInterstitialClientState.READY_TO_SHOW_OFFERS);
			// listener offer available true
		} else {
			if(mListener != null) {
				mListener.onSPInterstitialAdAvailable(false);
			}
			setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
			// listener offer available false
		}
	}

	public boolean validateAd(SPInterstitialAd ad) {
		return SPMediationCoordinator.INSTANCE.validateInterstitialProvider(
				mActivity, ad);
	}

	public boolean showInterstitial(Activity parentActivity) {
		if (mState.canShowOffers()) {
			boolean showAd = SPMediationCoordinator.INSTANCE.showInterstitial(parentActivity,
					mAd);
			if (showAd) {
				if (mListener != null) {
					mListener.onSPInterstitialAdShown();
				}
				setState(SPInterstitialClientState.SHOWING_OFFERS);
			}
			return showAd;
		} else {
			return false;
		}
	}

	public void fireEvent(SPInterstitialAd ad,
			SPInterstitialEvent event) {
		SPInterstitialEventDispatcher.trigger(mCredentials, mRequestId, ad, event);
		if (mListener != null) {
			switch (event) {
			case ShowClick:
				setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
				mListener.onSPInterstitialAdClosed(SPInterstitialAdCloseReason.ReasonUserClickedOnAd);
				break;
			case ShowClose:
				setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
				mListener.onSPInterstitialAdClosed(SPInterstitialAdCloseReason.ReasonUserClosedAd);
				break;
			case Error:
				setState(SPInterstitialClientState.READY_TO_CHECK_OFFERS);
				mListener.onSPInterstitialAdError("Some error occurred");
				break;
			default:
				break;
			}
		}
	}

	public void setListener(SPInterstitialRequestListener mListener) {
		this.mListener = mListener;
	}

}
