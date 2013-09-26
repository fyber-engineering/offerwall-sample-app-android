/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPMediationConfigurator {

	private static final String TAG = "SPMediationConfigurator";
	
	public static SPMediationConfigurator INSTANCE = new SPMediationConfigurator();

	private Map<String, Map<String, Object>> mConfigurations;
	
	private SPMediationConfigurator() {
		mConfigurations = new HashMap<String, Map<String, Object>>();
	}
	
	public List<String> getMediationAdaptors() {
		SponsorPayLogger.d(TAG, "Getting compatible adapters for SDK v" + SponsorPay.RELEASE_VERSION_STRING );
		
		//Use http request to get adaptors and versions
		// switch from list to map
		LinkedList<String> adaptors = new LinkedList<String>();
		adaptors.add("com.sponsorpay.sdk.mbe.mediation.MockMediatedAdaptor");
		
		return adaptors;
	}
	
	public Map<String, Object> getConfigurationForAdaptor(String adaptor) {
		return mConfigurations.get(adaptor);
	}
	
	public boolean setConfigurationForAdaptor(String adaptor, Map<String, Object> configurations) {
		return mConfigurations.put(adaptor, configurations) != null;
	}
	
}
