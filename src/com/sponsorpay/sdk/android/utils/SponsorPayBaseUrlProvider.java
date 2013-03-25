/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SponsorPayBaseUrlProvider {

	private static SponsorPayBaseUrlProvider INSTANCE = new SponsorPayBaseUrlProvider();
	private static final String TAG = "SponsorPayBaseUrlProvider";
	
	private SPUrlProvider mUrlProviderOverride;
	private ResourceBundle mBaseUrls;
	
	private SponsorPayBaseUrlProvider() {
		try {
			mBaseUrls = ResourceBundle.getBundle("com/sponsorpay/sdk/android/urls");
		} catch (MissingResourceException e) {
			SponsorPayLogger.e(TAG, "An error happened while initializing url provider", e);
		}
	}

	private String getUrl(String product) {
		String baseUrl = null;
		if (mUrlProviderOverride != null) {
			baseUrl = mUrlProviderOverride.getBaseUrl(product);
		} 
		if (StringUtils.nullOrEmpty(baseUrl)) {
			baseUrl = mBaseUrls.getString(product);
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
