package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.mediation.TremorMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.tremorvideo.sdk.android.videoad.TremorVideo;

public class TremorInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<TremorMediationAdapter> {

	private static final String TAG = "TremorInterstitialMediationAdapter";

	public TremorInterstitialMediationAdapter(TremorMediationAdapter adapter) {
		super(adapter);
	}

	@Override
	protected boolean show(Activity parentActivity) {
		if (TremorVideo.isAdReady()) {
			try {
				return TremorVideo.showAd(parentActivity, 0);
			} catch (Exception ex) {
				SponsorPayLogger.e(TAG, "An exception has been thrown while displaying the ad.", ex);
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	protected void checkForAds(Context context) {
		TremorVideo.start(); // sends an ad request and starts video download
	}

}
