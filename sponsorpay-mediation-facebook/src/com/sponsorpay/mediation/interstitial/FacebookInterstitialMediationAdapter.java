package com.sponsorpay.mediation.interstitial;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.sponsorpay.mediation.FacebookMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class FacebookInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<FacebookMediationAdapter> implements InterstitialAdListener {

	private InterstitialAd mInterstitialAd;

	public FacebookInterstitialMediationAdapter(FacebookMediationAdapter adapter, Activity pActivity) {
		super(adapter);
		AdSettings.addTestDevice(mAdapter.getTestDeviceId());
		this.mActivityRef = new WeakReference<Activity>(pActivity);
		// TODO Auto-generated constructor stub
	}

	/** from SPInterstitialMediationAdapter */
	@Override
	protected boolean show(Activity parentActivity) {
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
		mActivityRef.get().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mInterstitialAd = new InterstitialAd(context, mAdapter.getPlacementId());
				mInterstitialAd.setAdListener(FacebookInterstitialMediationAdapter.this);
				mInterstitialAd.loadAd();
			}
		});
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
		fireShowErrorEvent("Facebook ad error (" + pError.getErrorCode() + "): " + pError.getErrorMessage());
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
