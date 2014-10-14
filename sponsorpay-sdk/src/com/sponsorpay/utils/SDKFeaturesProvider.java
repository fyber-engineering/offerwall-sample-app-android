/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

public class SDKFeaturesProvider implements SPParametersProvider {

	private static final String FEATURES_KEY = "sdk_features";
	private static final String[] FEATURES = {"MPI"}
	;
	private HashMap<String, String> mParameters;

	public SDKFeaturesProvider() {
		mParameters = new HashMap<String, String>();
		mParameters.put(FEATURES_KEY, TextUtils.join(",", FEATURES));
	}
	
	@Override
	public Map<String, String> getParameters() {
		return mParameters;
	}

}
