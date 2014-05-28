package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.sponsorpay.mediation.FacebookMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class FacebookInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<FacebookMediationAdapter> implements InterstitialAdListener {

	private static final String PLACEMENT_ID = "some_id_from_facebook_goes_here";

	private InterstitialAd mInterstitialAd;

	public FacebookInterstitialMediationAdapter(FacebookMediationAdapter adapter) {
		super(adapter);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean show(Activity parentActivity) {
		if (mInterstitialAd != null) {
			mInterstitialAd.setAdListener(this);
			mInterstitialAd.show();
			return true;
		}
		return false;
	}

	@Override
	protected void checkForAds(Context context) {
		mInterstitialAd = new InterstitialAd(context, PLACEMENT_ID);
		mInterstitialAd.setAdListener(this);
		mInterstitialAd.loadAd();
	}

	@Override
	public void onAdClicked(Ad pAd) {
		// TODO Auto-generated method stub
		fireClickEvent();
	}

	@Override
	public void onAdLoaded(Ad pAd) {
		mInterstitialAd = (InterstitialAd) pAd;
		// TODO: what exactly to do now here????
	}

	@Override
	public void onError(Ad ad, AdError pError) {
		// TODO Auto-generated method stub
		fireShowErrorEvent("Facebook ad error (" + pError.getErrorCode() + "): " + pError.getErrorMessage());
	}

	@Override
	public void onInterstitialDismissed(Ad pAd) {
		// TODO Auto-generated method stub
		fireCloseEvent();
		mInterstitialAd = null;
	}

	@Override
	public void onInterstitialDisplayed(Ad pAd) {
		// TODO Auto-generated method stub
		fireImpressionEvent();
	}

}
