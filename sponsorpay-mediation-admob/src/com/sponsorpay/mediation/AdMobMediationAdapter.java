/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.sponsorpay.mediation.interstitial.AdMobIntersitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;


public class AdMobMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "AdMobMediationAdapter";
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "AdMob";
	
	private static String ADD_UNIT_ID= "add.unit.id";
	
	private AdMobIntersitialMediationAdapter mInterstitialAdapter;
	
	@Override
	public boolean startAdapter(final Activity activity) {
		//get admob version
		SponsorPayLogger.d(TAG, "Starting Admob SDK version ");
		if (StringUtils.notNullNorEmpty(addUnitId())) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mInterstitialAdapter = new AdMobIntersitialMediationAdapter(AdMobMediationAdapter.this, activity);
				}
			});
			return true;
		} else {
			SponsorPayLogger.i(TAG, "App ID value is not valid.");
			return false;
		}
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
	public SPBrandEngageMediationAdapter<SPMediationAdapter> getVideoMediationAdapter() {
		return null;
	}
	
	@Override
	public AdMobIntersitialMediationAdapter getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}
	
	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

	public String addUnitId() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, ADD_UNIT_ID , String.class);
	}


}