/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;

import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.mediation.mbe.VungleVideoMediationAdapter;
import com.vungle.sdk.VunglePub;
import com.vungle.sdk.VunglePub.EventListener;

public class VungleMediationAdapter extends SPMediationAdapter implements EventListener {
			
	private static final String TAG = "VungleAdapter";

	private static final String ADAPTER_VERSION = "2.0.0";

	private static final String ADAPTER_NAME = "Vungle";
	
	private static final String APP_ID = "app.id";
	private static final String SOUND_ENABLED = "sound.enabled";
	private static final String AUTO_ROTATION_ENABLED = "auto.rotation.enabled";
	private static final String BACK_BUTTON_ENABLED = "back.button.enabled";

	private VungleVideoMediationAdapter mVideoMediationAdapter = new VungleVideoMediationAdapter(this);
	
	private HashSet<EventListener> mVungleListeners = new HashSet<VunglePub.EventListener>();
	

	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Vungle adapter - SDK version " + VunglePub.getVersionString());
		String appId = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_ID, String.class);
		if (StringUtils.notNullNorEmpty(appId)) {
			SponsorPayLogger.i(TAG, "Using App ID = " + appId);
			VunglePub.setSoundEnabled(SPMediationConfigurator.getConfiguration(
					ADAPTER_NAME, SOUND_ENABLED, Boolean.TRUE, Boolean.class));
			VunglePub.setAutoRotation(SPMediationConfigurator.getConfiguration(
					ADAPTER_NAME, AUTO_ROTATION_ENABLED, Boolean.FALSE,	Boolean.class));
			VunglePub.setBackButtonEnabled(SPMediationConfigurator.getConfiguration(
					ADAPTER_NAME, BACK_BUTTON_ENABLED, Boolean.FALSE, Boolean.class));
			VunglePub.init(activity, appId);
			VunglePub.setEventListener(this);
			mVungleListeners.add(mVideoMediationAdapter);
			return true;
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
	public VungleVideoMediationAdapter getVideoMediationAdapter() {
		return mVideoMediationAdapter;
	}
	
	@Override
	public SPInterstitialMediationAdapter<SPMediationAdapter> getInterstitialMediationAdapter() {
		return null;
	}

	@Override
	protected Set<EventListener> getListeners() {
		return mVungleListeners;
	}
	
	// Vungle EventListener interface 
	@Override
	public void onVungleAdEnd() {
		notifyListeners((Object[])null);
	}

	@Override
	public void onVungleAdStart() {
		notifyListeners((Object[])null);
	}

	@Override
	public void onVungleView(double watchedSeconds, double totalAdSeconds) {
		notifyListeners(new Object[]{watchedSeconds, totalAdSeconds}, new Class[]{double.class, double.class});
	}
	
}
