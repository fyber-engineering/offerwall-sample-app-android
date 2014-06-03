/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.util.HashMap;
import java.util.Map;

public class SponsorPayBaseUrlProvider {

	private static SponsorPayBaseUrlProvider INSTANCE = new SponsorPayBaseUrlProvider();

	private SPUrlProvider mUrlProviderOverride;
	
	@SuppressWarnings("serial")
	private Map<String, String> urls = new HashMap<String, String>() {{
		put("actions", "https://service.sponsorpay.com/actions/v2");
		put("installs", "https://service.sponsorpay.com/installs/v2");
		put("vcs", "https://api.sponsorpay.com/vcs/v1/new_credit.json");
		put("mbe", "https://iframe.sponsorpay.com/mbe");
		put("ofw", "https://iframe.sponsorpay.com/mobile");
		put("interstitial", "https://engine.sponsorpay.com/interstitial");
		put("tracker", "https://engine.sponsorpay.com/tracker");
	}};
	
	private SponsorPayBaseUrlProvider() {
	}

	private String getUrl(String product) {
		String baseUrl = null;
		if (mUrlProviderOverride != null) {
			baseUrl = mUrlProviderOverride.getBaseUrl(product);
		} 
		if (StringUtils.nullOrEmpty(baseUrl)) {
			baseUrl = urls.get(product);
		}
		return baseUrl;
	}
	
	private void setOverride(SPUrlProvider provider) {
		mUrlProviderOverride = provider;
	}
	
	public static void setProviderOverride(SPUrlProvider provider) {
		SponsorPayBaseUrlProvider.INSTANCE.setOverride(provider);
	}

	public static String getBaseUrl(String product) {
		return SponsorPayBaseUrlProvider.INSTANCE.getUrl(product);
	}
}
