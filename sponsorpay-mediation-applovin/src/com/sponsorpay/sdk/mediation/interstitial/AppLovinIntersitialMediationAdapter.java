package com.sponsorpay.sdk.mediation.interstitial;

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
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.sdk.android.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.mediation.AppLovinMediationAdapter;

public class AppLovinIntersitialMediationAdapter extends
		SPInterstitialMediationAdapter<AppLovinMediationAdapter> implements AppLovinAdLoadListener, AppLovinAdDisplayListener, AppLovinAdClickListener {

	private AppLovinInterstitialAdDialog mInterstitialDialog;
	private AppLovinSdk mAppLovinSdk;
	private AppLovinAd mAppLovinAd;

	public AppLovinIntersitialMediationAdapter(AppLovinMediationAdapter adapter, Activity activity) {
		super(adapter);
//		AppLovinSdk.initializeSdk(activity);
		mAppLovinSdk = AppLovinSdk.getInstance(activity);
		mInterstitialDialog = AppLovinInterstitialAd.create(mAppLovinSdk, activity);
		mInterstitialDialog.setAdLoadListener(this);
		mInterstitialDialog.setAdDisplayListener(this);
		mInterstitialDialog.setAdClickListener(this);
		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
	}

	@Override
	public boolean show(Activity parentActivity) {
//		AppLovinInterstitialAd.show( parentActivity );
		mInterstitialDialog.showAndRender(mAppLovinAd);
		return true;
	}

	@Override
	public boolean interstitialAvailable(Context context, SPInterstitialAd ad) {
		if (isAdAvailable()) {
			return true;
		};
		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
		return false;
	}

	// AppLovinAdLoadListener
	@Override
	public void adReceived(AppLovinAd ad) {
		mAppLovinAd = ad;
		setAdAvailable();
	}

	@Override
	public void failedToReceiveAd(int errorCode) {
		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
	}

	// AppLovinAdDisplayListener
	
	@Override
	public void adDisplayed(AppLovinAd ad) {
		fireImpressionEvent();
	}

	@Override
	public void adHidden(AppLovinAd ad) {
		fireCloseEvent();
//		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
	}

	// AppLovinAdClickListener
	@Override
	public void adClicked(AppLovinAd ad) {
		fireClickEvent();
//		mAppLovinSdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
	}

}
