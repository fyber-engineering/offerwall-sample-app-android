/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
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

	private static final String TAG = "EbuzzingMediationAdapter";
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "Ebuzzing";
	
	private static final String APP_ID = "app.id";
//	private static final String SOUND_ENABLED = "sound.enabled";
//	private static final String AUTO_ROTATION_ENABLED = "auto.rotation.enabled";
//	private static final String BACK_BUTTON_ENABLED = "back.button.enabled";

	private EbuzzingVideoMediationAdapter mVideoMediationAdapter;
		

	@Override
	public boolean startAdapter(Activity activity) {
		
		SponsorPayLogger.d(TAG, "Starting Ebuzzing adapter - SDK version " + EbzConstants.EBUZZING_SDK_VERSION);
		String appId = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_ID, String.class);
		if (StringUtils.notNullNorEmpty(appId)) {
			SponsorPayLogger.i(TAG, "Using App ID = " + appId);
			mVideoMediationAdapter = new EbuzzingVideoMediationAdapter(this);
		}
		SponsorPayLogger.d(TAG, "App Id  must have a valid value!");
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