/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation;

import java.util.Set;

import android.app.Activity;

import com.applifier.impact.android.ApplifierImpact;
import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.mediation.mbe.ApplifierVideoMediationAdapter;

public class ApplifierMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "ApplifierAdapter";
	private static final String ADAPTER_VERSION = "2.0.0";
	private static final String ADAPTER_NAME = "Applifier";

	public static final String GAME_ID_KEY = "game.id.key";

	private ApplifierVideoMediationAdapter mVideoMediationAdapter = new ApplifierVideoMediationAdapter(this);

	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Applifier adapter - SDK version " + ApplifierImpact.getSDKVersion());
		String gameKey = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, GAME_ID_KEY, String.class);
		if (StringUtils.notNullNorEmpty(gameKey)) {
			new ApplifierImpact(activity, gameKey, mVideoMediationAdapter);
			return true;
		}
		SponsorPayLogger.i(TAG, "Game key value is not valid");
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
	public ApplifierVideoMediationAdapter getVideoMediationAdapter() {
		return mVideoMediationAdapter;
	}
	
	@Override
	public SPInterstitialMediationAdapter getInterstitialMediationAdapter() {
		return null;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

}
