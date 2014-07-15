package com.sponsorpay.mediation.interstitial;

import com.sponsorpay.mediation.helper.TremorInterstitialAdapterHelper;
import com.sponsorpay.utils.SponsorPayLogger;
import com.tremorvideo.sdk.android.videoad.TremorVideo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TremorInterstitialActivity extends Activity {
	
	private static final String TAG = "TremorInterstitialActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showInterstitial();
	}
	
	private void showInterstitial(){
		if (TremorVideo.isAdReady()) {
			try {
				TremorVideo.showAd(this, 1);
			} catch (Exception ex) {
				SponsorPayLogger.e(TAG, "An exception has been thrown while displaying the ad.", ex);
				TremorInterstitialAdapterHelper.getTremorInterstitialMediationAdapter().requestAdValidationError(ex);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		TremorInterstitialAdapterHelper.getTremorInterstitialMediationAdapter().processActivityResult(requestCode);
	}
}
