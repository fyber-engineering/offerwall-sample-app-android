/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial.marketplace;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.mediation.marketplace.MarketPlaceAdapter;
import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.StringUtils;

public class MarketPlaceInterstitial extends
		SPInterstitialMediationAdapter<MarketPlaceAdapter> {

	public MarketPlaceInterstitial(MarketPlaceAdapter adapter) {
		super(adapter);
	}

	@Override
	public boolean isAdAvailable(Context context, SPInterstitialAd ad) {
		String htmlContent = ad.getContextData().get("html");
		return StringUtils.notNullNorEmpty(htmlContent);
	}
	
	@Override
	protected boolean show(Activity parentActivity) {
		return false;
	}

	@Override
	protected void checkForAds(Context context) {
	}

}
