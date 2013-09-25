/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPMediationConfigurator {

	private static final String TAG = "SPMediationConfigurator";
	
	public static SPMediationConfigurator INSTANCE = new SPMediationConfigurator();

	private HashMap<String, HashMap<String, Object>> configurations;
	
	private SPMediationConfigurator() {
		configurations = new HashMap<String, HashMap<String, Object>>();
	}
	
	public List<String> getMediationAdaptors() {
		SponsorPayLogger.d(TAG, "Getting compatible adapters for SDK v" + SponsorPay.RELEASE_VERSION_STRING );
		LinkedList<String> adaptors = new LinkedList<String>();
		adaptors.add("com.sponsorpay.sdk.mbe.mediation.MockMediatedAdaptor");
		
		return adaptors;
	}
	
	public HashMap<String, Object> getConfigurationForAdaptor(String adaptor) {
		return configurations.get(adaptor);
	}
	
	public boolean setConfigurationForAdaptor(String adaptor, HashMap<String, Object> configurations) {
		return this.configurations.put(adaptor, configurations) != null;
	}
	
}
