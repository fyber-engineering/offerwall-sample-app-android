package com.sponsorpay.publisher.interstitial.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.interstitial.SPInterstitialRequestListener;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class SPIntersitialAdapter  extends SPInterstitialMediationAdapter<SPAdapter> implements SPInterstitialRequestListener{

	public SPIntersitialAdapter(SPAdapter adapter, Activity activity) {
		super(adapter);
		SponsorPayPublisher.getIntentForInterstitialActivity(activity, this);
	}

	@Override
	protected boolean show(Activity parentActivity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void checkForAds(Context context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSPInterstitialAdAvailable(Intent interstitialActivity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSPInterstitialAdNotAvailable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSPInterstitialAdError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}


}
