package com.sponsorpay.publisher.interstitial;

public interface SPInterstitialAdStateListener {

	public void onSPInterstitialAdShown();
	
	public void onSPInterstitialAdClosed(SPInterstitialAdCloseReason reason);
	
	public void onSPInterstitialAdError(String error);
	
}
