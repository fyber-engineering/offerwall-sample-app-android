package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sponsorpay.mediation.TremorMediationAdapter;
import com.sponsorpay.mediation.helper.TremorInterstitialAdapterHelper;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.tremorvideo.sdk.android.adapter.TremorAdapterCallbackListener;
import com.tremorvideo.sdk.android.videoad.TremorVideo;

public class TremorInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<TremorMediationAdapter> implements ITremorAdapterHelperInterface,
		TremorAdapterCallbackListener {

	public static final int RESULT_CODE_SUCCESS = 1;

	public TremorInterstitialMediationAdapter(TremorMediationAdapter adapter) {
		super(adapter);

		// Put the adapter in the config, as it could be reached from the helper
		// activity within
		TremorInterstitialAdapterHelper.setTremorInterstitialMediationAdapter(this);
	}

	@Override
	protected boolean show(Activity parentActivity) {
		if (TremorVideo.isAdReady()) {
			Intent tIntent = new Intent(parentActivity, TremorInterstitialActivity.class);
			parentActivity.startActivity(tIntent);
			fireImpressionEvent();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void checkForAds(Context context) {
		// Start the TremorVideo background process, also mark the begining of a
		// user session
		TremorVideo.start();
		if (TremorVideo.isAdReady()) {
			setAdAvailable();
		}
	}

	@Override
	public void processActivityResult(int pResultCode) {
		if (pResultCode == RESULT_CODE_SUCCESS) {
			fireCloseEvent();
		} else {
			fireShowErrorEvent("Ad wasn't shown successfully");
		}
		TremorVideo.start();
	}

	@Override
	public void requestAdValidationError(Throwable thr) {
		fireValidationErrorEvent("An exception has been caught while trying to display the interstitial: "
				+ thr.getMessage() + ". The cause: " + thr.getCause());
	}

}
