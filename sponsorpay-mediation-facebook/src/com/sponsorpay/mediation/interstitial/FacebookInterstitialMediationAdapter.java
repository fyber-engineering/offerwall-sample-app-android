package com.sponsorpay.mediation.interstitial;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.sponsorpay.mediation.FacebookMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class FacebookInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<FacebookMediationAdapter> implements InterstitialAdListener {

	private static final String TAG = FacebookInterstitialMediationAdapter.class.getSimpleName();

	private InterstitialAd mInterstitialAd;

	public FacebookInterstitialMediationAdapter(FacebookMediationAdapter adapter, Activity pActivity) {
		super(adapter);
		Log.d(TAG, "my device hash: " + mAdapter.getTestDeviceId());
		AdSettings.addTestDevice(mAdapter.getTestDeviceId());
		// AdSettings.addTestDevice("93e88e38272aee6251e20be438ed249f"); //hubert samsung s3

		this.mActivityRef = new WeakReference<Activity>(pActivity);
		// only for testing purposes placed here getting an ad
		mActivityRef.get().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mInterstitialAd = new InterstitialAd(mActivityRef.get(), mAdapter.getPlacementId());
				mInterstitialAd.setAdListener(FacebookInterstitialMediationAdapter.this);
				mInterstitialAd.loadAd();
				setAdAvailable(); //only for testing purposes
			}
		});
		// TODO Auto-generated constructor stub
	}

	/** from SPInterstitialMediationAdapter */
	@Override
	protected boolean show(Activity parentActivity) {
		Log.d(TAG, "show() facebook ad interstitial");
		if (mInterstitialAd != null) {
			mInterstitialAd.setAdListener(this);
			mInterstitialAd.show();
			return true;
		}
		return false;
	}

	/** from SPInterstitialMediationAdapter */
	@Override
	protected void checkForAds(final Context context) {
		Log.d(TAG, "checkForAds");
	}

	/** from Facebook SDK */
	@Override
	public void onAdClicked(Ad pAd) {
		// TODO Auto-generated method stub
		fireClickEvent();
	}

	/** from Facebook SDK */
	@Override
	public void onAdLoaded(Ad pAd) {
		mInterstitialAd = (InterstitialAd) pAd;
		// TODO: what exactly to do now here????
	}

	/** from Facebook SDK */
	@Override
	public void onError(Ad ad, AdError pError) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Ad error (" + pError.getErrorCode() + "): " + pError.getErrorMessage());
		Log.getStackTraceString(new Exception());
		//fireShowErrorEvent("Facebook ad error (" + pError.getErrorCode() + "): " + pError.getErrorMessage());
	}

	/** from Facebook SDK */
	@Override
	public void onInterstitialDismissed(Ad pAd) {
		// TODO Auto-generated method stub
		fireCloseEvent();
		mInterstitialAd = null;
	}

	/** from Facebook SDK */
	@Override
	public void onInterstitialDisplayed(Ad pAd) {
		// TODO Auto-generated method stub
		fireImpressionEvent();
	}

}
