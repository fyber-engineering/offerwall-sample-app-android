package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sponsorpay.mediation.helper.TremorInterstitialAdapterHelper;
import com.sponsorpay.utils.SponsorPayLogger;
import com.tremorvideo.sdk.android.videoad.TremorVideo;

public class TremorInterstitialActivity extends Activity {
	
	private static final String TAG = "TremorInterstitialActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SponsorPayLogger.w(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		SponsorPayLogger.w(TAG, "onResume");
		super.onResume();
		showInterstitial();
	}
	
	private void showInterstitial(){
		SponsorPayLogger.w(TAG, "showInterstitial");
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
		SponsorPayLogger.w(TAG, "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		SponsorPayLogger.w(TAG, "onActivityResult super passed");
		TremorInterstitialAdapterHelper.getTremorInterstitialMediationAdapter().processActivityResult(requestCode);
		finish();
		SponsorPayLogger.w(TAG, "activity finish() passed");
	}
	
	@Override
	protected void onDestroy() {
		SponsorPayLogger.w(TAG, "onDestroy");
		super.onDestroy();
	}
}
