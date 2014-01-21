package com.sponsorpay.sdk.android.publisher.interstitial;

public interface SPInterstitialAdStateListener {

	public void onSPInterstitialAdShown();
	
	public void onSPInterstitialAdClosed(SPInterstitialAdCloseReason reason);
	
	public void onSPInterstitialAdError(String error);
	
}
