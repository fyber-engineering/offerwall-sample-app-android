/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * Class holding the information about a specific Intersitial Ad.
 * </p>
 * 
 * This class is meant to be used only internally.
 */
public class SPInterstitialAd {

	/**
	 * The provider type of the ad
	 */
	private String mProviderType;
	
	/**
	 * SponsorPay internal id of the ad
	 */
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
