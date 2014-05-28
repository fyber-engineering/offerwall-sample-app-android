package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.mediation.FacebookMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class FacebookInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<FacebookMediationAdapter> {

	public FacebookInterstitialMediationAdapter(FacebookMediationAdapter adapter) {
		super(adapter);
		// TODO Auto-generated constructor stub
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

}
