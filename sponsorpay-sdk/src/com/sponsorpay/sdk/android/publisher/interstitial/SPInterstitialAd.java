package com.sponsorpay.sdk.android.publisher.interstitial;

public class SPInterstitialAd {

	private String mProviderType;
	private String mAdId;

	public SPInterstitialAd(String providerType, String adId) {
		mProviderType = providerType;
		mAdId = adId;
	}

	public String getProviderType() {
		return mProviderType;
	}

	public String getAdId() {
		return mAdId;
	}

}
