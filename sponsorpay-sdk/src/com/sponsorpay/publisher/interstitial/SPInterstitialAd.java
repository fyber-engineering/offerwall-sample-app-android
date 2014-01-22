package com.sponsorpay.publisher.interstitial;

import java.util.Collections;
import java.util.Map;

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

	public Map<String, String> getContextData() {
		return Collections.emptyMap();
	}

}
