/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.mediation.mbe.UnityAdsVideoMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.unity3d.ads.android.UnityAds;

public class UnityAdsMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "UnityAdsAdapter";
	private static final String ADAPTER_VERSION = "2.1.0";
	private static final String ADAPTER_NAME = "Applifier";

	public static final String GAME_ID_KEY = "game.id.key";

	private UnityAdsVideoMediationAdapter mVideoMediationAdapter = new UnityAdsVideoMediationAdapter(this);

	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting UnityAds (former Applifier) adapter - SDK version " + UnityAds.getSDKVersion());
		String gameKey = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, GAME_ID_KEY, String.class);
		if (StringUtils.notNullNorEmpty(gameKey)) {
			UnityAds.init(activity, gameKey, mVideoMediationAdapter);
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
	public UnityAdsVideoMediationAdapter getVideoMediationAdapter() {
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
