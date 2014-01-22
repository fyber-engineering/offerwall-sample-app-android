/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.appia.sdk.Appia;
import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.mediation.interstitial.AppiaIntersitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;


public class AppiaMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "AppiaAdapter";
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "Appia";
	
	private static final String SITE_ID = "siteId";
	
	private AppiaIntersitialMediationAdapter mInterstitialAdapter;
	
	@Override
	public boolean startAdapter(Activity activity) {
		//No SDK version for Appia
		SponsorPayLogger.d(TAG, "Starting Appia adapter");
		Integer siteId = null;
		try {
			siteId = Integer.decode(SPMediationConfigurator.getConfiguration(ADAPTER_NAME, SITE_ID, String.class));
			Appia appia = Appia.getAppia(activity);
			appia.setSiteId(siteId);
			mInterstitialAdapter = new AppiaIntersitialMediationAdapter(this, appia, activity);
			return true;
		} catch (Exception e) {
			SponsorPayLogger.i(TAG, "Site ID value is not valid.");
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
	public AppiaIntersitialMediationAdapter getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}
	
	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}
	

}
