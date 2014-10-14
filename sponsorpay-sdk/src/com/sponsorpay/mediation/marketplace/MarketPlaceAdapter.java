/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.marketplace;

import java.util.Set;

import android.app.Activity;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.publisher.interstitial.marketplace.MarketPlaceInterstitial;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;

public class MarketPlaceAdapter extends SPMediationAdapter {

	private static final String ADAPTER_NAME = "Fyber";
	
	private MarketPlaceInterstitial mInterstitial;

	@Override
	public boolean startAdapter(Activity activity) {
		mInterstitial = new MarketPlaceInterstitial(this);
		return true;
	}

	@Override
	public String getName() {
		return ADAPTER_NAME;
	}

	@Override
	public String getVersion() {
		return SponsorPay.RELEASE_VERSION_STRING;
	}

	@Override
	public SPBrandEngageMediationAdapter<? extends SPMediationAdapter> getVideoMediationAdapter() {
		return null;
	}

	@Override
	public MarketPlaceInterstitial getInterstitialMediationAdapter() {
		return mInterstitial;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

}
