/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.millennialmedia.android.MMSDK;
import com.sponsorpay.mediation.interstitial.MillenialIntersitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;


public class MillenialMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "MillenialAdapter";
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "Millenial";
	
	private static String APP_ID = "app.id";
	
	private MillenialIntersitialMediationAdapter mInterstitialAdapter;
	
	@Override
	public boolean startAdapter(final Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Millenial SDK version " + MMSDK.VERSION );
		if (StringUtils.notNullNorEmpty(getAppid())) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					MMSDK.initialize(activity);
					mInterstitialAdapter = new MillenialIntersitialMediationAdapter(
							MillenialMediationAdapter.this, activity);
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
	public MillenialIntersitialMediationAdapter getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}
	
	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

	public String getAppid() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_ID , String.class);
	}
	

}
