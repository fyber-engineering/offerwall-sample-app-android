/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial;

public interface SPInterstitialRequestListener {
	
	public void onSPInterstitialAdAvailable(boolean isAdAvailable);
	
	public void onSPInterstitialAdShown();
	
	public void onSPInterstitialAdClosed(SPInterstitialAdCloseReason reason);
	
	public void onSPInterstitialAdError(String error);
	
}
