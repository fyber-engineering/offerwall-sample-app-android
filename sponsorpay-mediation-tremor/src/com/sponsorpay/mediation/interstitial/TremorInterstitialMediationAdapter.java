package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sponsorpay.mediation.TremorMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.tremorvideo.sdk.android.adapter.TremorAdapterCallbackListener;
import com.tremorvideo.sdk.android.videoad.TremorVideo;

public class TremorInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<TremorMediationAdapter> implements ITremorAdapterHelperInterface,
		TremorAdapterCallbackListener {

	private static final String TAG = "TremorInterstitialMediationAdapter";

	public static final int RESULT_CODE_SUCCESS = 1;

	public TremorInterstitialMediationAdapter(TremorMediationAdapter adapter) {
		super(adapter);
		TremorVideo.start(); // sends an ad request and starts video download
	}

	@Override
	protected boolean show(Activity parentActivity) {
		if (TremorVideo.isAdReady()) {
			Intent tIntent = new Intent(parentActivity, TremorInterstitialActivity.class);
			parentActivity.startActivity(tIntent);
			SponsorPayLogger.d(TAG, "Ad is ready to show!");
			return true;
		} else {
			SponsorPayLogger.d(TAG, "Ad is not ready to show yet!");
			return false;
		}
	}

	@Override
	protected void checkForAds(Context context) {
		SponsorPayLogger.d(TAG, "check for ads!");
		if (TremorVideo.isAdReady()) {
			setAdAvailable();
		}
	}

	@Override
	public void processActivityResult(int pResultCode) {
		if (pResultCode == RESULT_CODE_SUCCESS) {
			fireImpressionEvent();
		} else {
			fireShowErrorEvent("Ad wasn't shown successfully");
		}
	}

	@Override
	public void requestAdValidationError(Throwable thr) {
		fireValidationErrorEvent("An exception has been caught while trying to display the interstitial: "
				+ thr.getMessage() + ". The cause: " + thr.getCause());
	}

}
