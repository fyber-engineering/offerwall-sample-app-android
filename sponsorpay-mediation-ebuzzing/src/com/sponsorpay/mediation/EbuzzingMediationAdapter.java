/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.ebuzzing.sdk.adserver.EbzConstants;
import com.sponsorpay.mediation.mbe.EbuzzingVideoMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;


public class EbuzzingMediationAdapter extends SPMediationAdapter {

	private static final String TAG               = "EbuzzingMediationAdapter";
	private static final String ADAPTER_VERSION   = "1.0.0";
	private static final String ADAPTER_NAME      = "Ebuzzing";
	private static final String INTERSTITIAL_TAG  = "interstitial.tag";

	private EbuzzingVideoMediationAdapter mVideoMediationAdapter;
		

	@Override
	public boolean startAdapter(Activity activity) {
		
		SponsorPayLogger.d(TAG, "Starting Ebuzzing adapter - SDK version " + EbzConstants.EBUZZING_SDK_VERSION);
		String intersitialTag = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, INTERSTITIAL_TAG, String.class);
		
		if (StringUtils.notNullNorEmpty(intersitialTag)) {
			SponsorPayLogger.i(TAG, "Interstitial tag = " + intersitialTag);
			mVideoMediationAdapter = new EbuzzingVideoMediationAdapter(this, intersitialTag, activity);
			
			return true;
		}
			
		SponsorPayLogger.d(TAG, "Interstitial tag must have a valid value!");

		return false;
	}

	@Override
	public String getName() {
		return ADAPTER_NAME;
	}

	@Override
	public String getVersion() {
		return ADAPTER_VERSION;
	}
	

	@Override
	public EbuzzingVideoMediationAdapter getVideoMediationAdapter() {
		return mVideoMediationAdapter;
	}
	
	@Override
	public SPInterstitialMediationAdapter<SPMediationAdapter> getInterstitialMediationAdapter() {
		return null;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

}