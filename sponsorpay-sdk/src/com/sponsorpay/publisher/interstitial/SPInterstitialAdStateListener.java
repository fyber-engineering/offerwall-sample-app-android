/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

public interface SPInterstitialAdStateListener {

	public void onSPInterstitialAdShown();
	
	public void onSPInterstitialAdClosed(SPInterstitialAdCloseReason reason);
	
	public void onSPInterstitialAdError(String error);
	
}
