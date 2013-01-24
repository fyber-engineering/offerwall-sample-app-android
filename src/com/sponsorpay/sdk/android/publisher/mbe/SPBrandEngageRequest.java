package com.sponsorpay.sdk.android.publisher.mbe;

import android.app.Activity;
import android.content.Intent;

import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPBrandEngageRequest implements SPBrandEngageClientStatusListener {

	private static final String TAG = "SPBrandEngageRequest";
	private Activity mActivity;
	private SPCredentials mCredentials;
	private SPBrandEngageRequestListener mListener; 

	public SPBrandEngageRequest(SPCredentials credentials, Activity activity,
			SPBrandEngageRequestListener listener) {
		mCredentials = credentials;
		mActivity = activity;
		mListener = listener;
	}
	
	public void askForOffers() {
		SPBrandEngageClient.INSTANCE.setStatusListener(this);
		SPBrandEngageClient.INSTANCE.requestOffers(mCredentials, mActivity);
	}
	
	private Intent getMBEActivity() {
		if (SPBrandEngageClient.INSTANCE.canStartEngagement()) {
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
