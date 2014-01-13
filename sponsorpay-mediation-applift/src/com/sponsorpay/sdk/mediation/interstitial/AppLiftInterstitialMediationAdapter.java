package com.sponsorpay.sdk.mediation.interstitial;

import android.app.Activity;
import android.content.Context;

import com.applift.playads.PlayAds;
import com.applift.playads.api.PlayAdsListener;
import com.applift.playads.api.PlayAdsPromo;
import com.applift.playads.api.PlayAdsType;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.sdk.android.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.mediation.AppLiftMediationAdapter;

public class AppLiftInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<AppLiftMediationAdapter> implements PlayAdsListener{

	public AppLiftInterstitialMediationAdapter(AppLiftMediationAdapter adapter) {
		super(adapter);
	}
	
	public void start() {
		PlayAds.cache();
	}
	
	@Override
	public boolean interstitialAvailable(Context context, SPInterstitialAd ad) {
		return isAdAvailable();
	}
	
	@Override
	public boolean show(Activity parentActivity) {
		PlayAds.show(parentActivity);
		return true;
	}

	@Override
	public void onCached(PlayAdsType type) {
		setAdAvailable();
	}

	@Override
	public void onShown(PlayAdsType type) {
		fireImpressionEvent();
	}

	@Override
	public void onTapped(PlayAdsPromo promo) {
		fireClickEvent();		
		PlayAds.cache();
	}
	
	@Override
	public void onError(Exception exception) {
		fireErrorEvent();
		PlayAds.cache();
	}

}
