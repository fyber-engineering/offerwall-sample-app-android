/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe;

import android.app.Activity;
import android.content.Intent;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.utils.SponsorPayLogger;

/**
 * <p>
 * Wrapper calls to perform a BrandEngage request and to notify a {@link SPBrandEngageClientStatusListener}
 * of the result
 * </p>
 * This class is not intended to be used directly or subclassed, it is used internally
 * by {@link SponsorPayPublisher}
 */
public class SPBrandEngageRequest implements SPBrandEngageClientStatusListener {

	private static final String TAG = "SPBrandEngageRequest";
	private Activity mActivity;
	private SPCredentials mCredentials;
	private SPBrandEngageRequestListener mListener;
	private SPBrandEngageClient mBrandEngageClient;

	public SPBrandEngageRequest(SPCredentials credentials, Activity activity,
			SPBrandEngageClient brandEngageClient, SPBrandEngageRequestListener listener) {
		mCredentials = credentials;
		mActivity = activity;
		mBrandEngageClient = brandEngageClient;
		mListener = listener;
	}
	
	public void askForOffers() {
		mBrandEngageClient.setStatusListener(this);
		mBrandEngageClient.requestOffers(mCredentials, mActivity);
	}
	
	private Intent getMBEActivity() {
		if (mBrandEngageClient.canStartEngagement()) {
			// check if played through a TPN
			return new Intent(mActivity, SPBrandEngageActivity.class);
		}
		SponsorPayLogger.d(TAG, "Undefined error");
		return null;
	}

	@Override
	public void didReceiveOffers(boolean areOffersAvaliable) {
		if (areOffersAvaliable) {
			mListener.onSPBrandEngageOffersAvailable(getMBEActivity());			
		} else {
			mListener.onSPBrandEngageOffersNotAvailable();
		}
	}

	@Override
	public void didChangeStatus(SPBrandEngageClientStatus newStatus) {
		if (newStatus == SPBrandEngageClientStatus.ERROR) {
			mListener.onSPBrandEngageError("An error happened while trying to get offers from mBE");
		}
	}
	
}
