/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation;

import java.util.HashMap;
import java.util.Set;

import android.app.Activity;

import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.mediation.interstitial.MockMediatedInterstitialAdapter;
import com.sponsorpay.sdk.mediation.mbe.MockMediatedVideoAdapter;

public class MockMediatedAdapter extends SPMediationAdapter {

	private static final String TAG = "MockMediatedAdapter";
	public static final String ADAPTER_NAME = "MockMediatedNetwork";
	private static final String VERSION_STRING = "2.0.0";

	private MockMediatedVideoAdapter mVideoMediationAdapter;
	private MockMediatedInterstitialAdapter mInterstitialMediationAdapter;

	private HashMap<String, Object> mConfigs;
	
	public MockMediatedAdapter() {
		mConfigs = new HashMap<String, Object>();
		mVideoMediationAdapter = new MockMediatedVideoAdapter(this, mConfigs);
		mInterstitialMediationAdapter = new MockMediatedInterstitialAdapter(this, mConfigs);
	}
	
	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting mock mediated network adapter");
		SPMediationConfigurator.INSTANCE.setConfigurationForAdapter(getName(), mConfigs);
		return true;
	}
	
	@Override
	public String getName() {
		return ADAPTER_NAME;
	}
	
	@Override
	public String getVersion() {
		return VERSION_STRING;
	}

	@Override
	public MockMediatedVideoAdapter getVideoMediationAdapter() {
		return mVideoMediationAdapter;
	}

	@Override
	public MockMediatedInterstitialAdapter getInterstitialMediationAdapter() {
		return mInterstitialMediationAdapter;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

}
