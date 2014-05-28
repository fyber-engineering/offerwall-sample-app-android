package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;

public class FacebookMediationAdapter extends SPMediationAdapter {

	@Override
	public boolean startAdapter(Activity activity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SPBrandEngageMediationAdapter<? extends SPMediationAdapter> getVideoMediationAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SPInterstitialMediationAdapter<? extends SPMediationAdapter> getInterstitialMediationAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		// TODO Auto-generated method stub
		return null;
	}

}
