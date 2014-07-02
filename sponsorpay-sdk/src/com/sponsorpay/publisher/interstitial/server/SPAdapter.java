package com.sponsorpay.publisher.interstitial.server;

import java.util.Set;

import android.app.Activity;

import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

public class SPAdapter extends SPMediationAdapter {
	
	private static final String TAG = "SPAdapter";
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "SP";
		
	private SPIntersitialAdapter mInterstitialAdapter;

	@Override
	public boolean startAdapter(Activity activity) {
		// get admob version
		SponsorPayLogger.d(TAG, "Starting Admob SDK");
//		if (StringUtils.notNullNorEmpty(getAdUnitId())) {
//			activity.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
					mInterstitialAdapter = new SPIntersitialAdapter(SPAdapter.this, activity);
					return true;
//				}
//			});
//			return true;
//		} else {
//			SponsorPayLogger.i(TAG, "Ad Unit ID value is not valid.");
//			return false;
//		}
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
	public SPIntersitialAdapter getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}
	
	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

}