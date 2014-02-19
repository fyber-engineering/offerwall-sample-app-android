package com.sponsorpay.mediation;
/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */


import java.util.Set;

import android.app.Activity;

import com.inmobi.commons.InMobi;
import com.sponsorpay.mediation.interstitial.InMobiIntersitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;


public class InMobiMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "InMobi";
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "InMobi";
	
	private static final String PROPERTY_ID = "propertyId";
	
	private InMobiIntersitialMediationAdapter mInterstitialAdapter; 
	
	@Override
	public boolean startAdapter(Activity activity) {
		//No SDK version for Appia
		SponsorPayLogger.d(TAG, "Starting InMobi SDK version  " + InMobi.getVersion());
		String propertyId = getPropertyId();
		if (StringUtils.notNullNorEmpty(propertyId)) {
			InMobi.initialize(activity, propertyId);
			mInterstitialAdapter = new InMobiIntersitialMediationAdapter(this, activity);
			return true;
		}
		SponsorPayLogger.d(TAG, "Property Id does not contain a valid value");
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
	public SPBrandEngageMediationAdapter<SPMediationAdapter> getVideoMediationAdapter() {
		return null;
	}
	
	@Override
	public InMobiIntersitialMediationAdapter getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}
	
	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}
	
	public String getPropertyId() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, PROPERTY_ID, String.class);
	}

}
