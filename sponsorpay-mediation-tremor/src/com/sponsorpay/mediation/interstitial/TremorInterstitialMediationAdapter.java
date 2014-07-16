package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sponsorpay.mediation.TremorMediationAdapter;
import com.sponsorpay.mediation.helper.TremorInterstitialAdapterHelper;
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

		// Put the adapter in the config, as it could be reached from the helper
		// activity within
		TremorInterstitialAdapterHelper.setTremorInterstitialMediationAdapter(this);
	}

	@Override
	protected boolean show(Activity parentActivity) {
		SponsorPayLogger.w(TAG, "show");
		if (TremorVideo.isAdReady()) {
			SponsorPayLogger.d(TAG, "Ad is ready to show!");
			Intent tIntent = new Intent(parentActivity, TremorInterstitialActivity.class);
			parentActivity.startActivity(tIntent);
			fireImpressionEvent();
			return true;
		} else {
			SponsorPayLogger.d(TAG, "Ad is not ready to show yet!");
			return false;
		}
	}

	@Override
	protected void checkForAds(Context context) {
		SponsorPayLogger.w(TAG, "checkForAds");
		// Start the TremorVideo background process, also mark the begining of a
		// user session
		TremorVideo.start();
		SponsorPayLogger.d(TAG, "TremorVideo.start() has been invoked");
		if (TremorVideo.isAdReady()) {
			SponsorPayLogger.i(TAG, "ad is ready!");
			setAdAvailable();
		}
	}

	@Override
	public void processActivityResult(int pResultCode) {
		SponsorPayLogger.w(TAG, "processActivityResult");
		if (pResultCode == RESULT_CODE_SUCCESS) {
			SponsorPayLogger.d(TAG, "firing impression event");
			fireCloseEvent();
		} else {
			SponsorPayLogger.d(TAG, "firing show error event");
			fireShowErrorEvent("Ad wasn't shown successfully");
		}
		TremorVideo.start();
	}

	@Override
	public void requestAdValidationError(Throwable thr) {
		SponsorPayLogger.w(TAG, "requestAdValidationError");
		fireValidationErrorEvent("An exception has been caught while trying to display the interstitial: "
				+ thr.getMessage() + ". The cause: " + thr.getCause());
	}

}
