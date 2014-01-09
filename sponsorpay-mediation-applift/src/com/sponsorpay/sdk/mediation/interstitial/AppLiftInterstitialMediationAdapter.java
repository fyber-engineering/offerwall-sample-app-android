package com.sponsorpay.sdk.mediation.interstitial;

import com.applift.playads.api.PlayAdsListener;
import com.applift.playads.api.PlayAdsPromo;
import com.applift.playads.api.PlayAdsType;
import com.sponsorpay.sdk.android.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.mediation.AppLiftMediationAdapter;

public class AppLiftInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<AppLiftMediationAdapter> implements PlayAdsListener{

	public AppLiftInterstitialMediationAdapter(AppLiftMediationAdapter adapter) {
		super(adapter);
	}

	@Override
	public void onCached(PlayAdsType type) {
	}

	@Override
	public void onShown(PlayAdsType type) {
		
	}

	@Override
	public void onTapped(PlayAdsPromo promo) {
		
	}
	
	@Override
	public void onError(Exception exception) {
		
	}

}
