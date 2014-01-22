/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import android.content.Intent;

public interface SPInterstitialRequestListener {
	
	public void onSPInterstitialAdAvailable(Intent interstitialActivity);
	
	public void onSPInterstitialAdNotAvailable();
	
	public void onSPInterstitialAdError(String error);
	
}
