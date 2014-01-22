package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.sponsorpay.mediation.AppLovinMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class AppLovinIntersitialMediationAdapter extends
		SPInterstitialMediationAdapter<AppLovinMediationAdapter> implements AppLovinAdLoadListener, AppLovinAdDisplayListener, AppLovinAdClickListener {

	private AppLovinInterstitialAdDialog mInterstitialDialog;
	private AppLovinSdk mAppLovinSdk;
	private AppLovinAd mAppLovinAd;
	
//	private AppLovinAdView adView;
	

	public AppLovinIntersitialMediationAdapter(AppLovinMediationAdapter adapter, Activity activity) {
		super(adapter);
//		AppLovinSdk.initializeSdk(activity);
		mAppLovinSdk = AppLovinSdk.getInstance(activity);
		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
		
//		adView = new AppLovinAdView( mAppLovinSdk, AppLovinAdSize.INTERSTITIAL, activity );
//		adView.setAdLoadListener(this);
//		adView.loadNextAd();

	}

	@Override
	public boolean show(Activity parentActivity) {
		if (mAppLovinAd != null) {
			mInterstitialDialog = AppLovinInterstitialAd.create(mAppLovinSdk, parentActivity);
//			mInterstitialDialog.setAdLoadListener(this);
			mInterstitialDialog.setAdDisplayListener(this);
			mInterstitialDialog.setAdClickListener(this);
			mInterstitialDialog.showAndRender(mAppLovinAd);
			return true;
		}
		return false;
		
//		parentActivity.addContentView(adView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		return true;
		
	}


	@Override
	protected void checkForAds(Context context) {
		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
	}
	
//	@Override
//	public boolean interstitialAvailable(Context context, SPInterstitialAd ad) {
//		if (isAdAvailable()) {
//			return true;
//		};
//		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
//		return false;
//	}

	// AppLovinAdLoadListener
	@Override
	public void adReceived(AppLovinAd ad) {
		mAppLovinAd = ad;
		setAdAvailable();
	}

	@Override
	public void failedToReceiveAd(int errorCode) {
		mAppLovinAd = null;
		fireErrorEvent("Applovin failedToReceiveAd with errorCode " + errorCode);
//		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
	}

	// AppLovinAdDisplayListener
	@Override
	public void adDisplayed(AppLovinAd ad) {
		fireImpressionEvent();
	}

	@Override
	public void adHidden(AppLovinAd ad) {
		fireCloseEvent();
		mInterstitialDialog = null;
	}

	// AppLovinAdClickListener
	@Override
	public void adClicked(AppLovinAd ad) {
		fireClickEvent();
		mInterstitialDialog.dismiss();
	}

}
