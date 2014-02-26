/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.interstitial;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMInterstitial;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.RequestListener;
import com.sponsorpay.mediation.MillenialMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class MillenialIntersitialMediationAdapter extends
		SPInterstitialMediationAdapter<MillenialMediationAdapter> implements RequestListener  {

	private MMInterstitial mInterstitial;

	public MillenialIntersitialMediationAdapter(MillenialMediationAdapter adapter, Activity activity) {
		super(adapter);
		this.mActivityRef = new WeakReference<Activity>(activity);
		mInterstitial = new MMInterstitial(activity);
		String appId = mAdapter.getAppid();
		mInterstitial.setApid(appId);
		//Set your metadata in the MMRequest object
		MMRequest request = new MMRequest();

		//Add the MMRequest object to your MMInterstitial.
		mInterstitial.setMMRequest(request);
		mInterstitial.setListener(this);
		checkForAds(activity);
	}

	@Override
	public boolean show(Activity parentActivity) {
		return mInterstitial.display();
	}


	@Override
	protected void checkForAds(Context context) {
		if (mInterstitial.isAdAvailable() ) {
			setAdAvailable();
		} else if(getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mInterstitial.fetch();
				}
			});
		}
	}

	//RequestListener
	@Override
	public void MMAdOverlayClosed(MMAd ad) {
		fireCloseEvent();
	}

	@Override
	public void MMAdOverlayLaunched(MMAd ad) {
		fireImpressionEvent();
	}

	@Override
	public void MMAdRequestIsCaching(MMAd ad) {
		// do nothing
	}

	@Override
	public void onSingleTap(MMAd ad) {
		fireClickEvent();
	}

	@Override
	public void requestCompleted(MMAd ad) {
		setAdAvailable();
	}

	@Override
	public void requestFailed(MMAd ad, MMException exception) {
		// ad is already precached, mark it as available
		if (exception.getCode() == 17) {
			setAdAvailable();
		} else {
			fireValidationErrorEvent(exception.getLocalizedMessage());
		}
	}
	
}