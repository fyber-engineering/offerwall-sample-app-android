package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.sponsorpay.mediation.interstitial.FacebookInterstitialMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;

public class FacebookMediationAdapter extends SPMediationAdapter {

	private static final String TAG = FacebookMediationAdapter.class.getSimpleName();
	private static final String ADAPTER_VERSION = "0.1";

	private static final String ADAPTER_NAME = "Facebook";
	private FacebookInterstitialMediationAdapter mInterstitialAdapter;

	/**
	 * Initializes the wrapped SDK, usually with the necessary credentials.
	 * 
	 * Parameters: activity - The parent activity calling this method
	 * 
	 * Returns: true if the adapter was successfully started, false otherwise
	 */

	@Override
	public boolean startAdapter(Activity activity) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns:the name of the wrapped network.
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns:the version of the wrapped network.
	 */
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SPBrandEngageMediationAdapter<? extends SPMediationAdapter> getVideoMediationAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the InterstitialAdapter which has been instantiated in
	 * startAdapter(Activity)
	 */
	@Override
	public SPInterstitialMediationAdapter<? extends SPMediationAdapter> getInterstitialMediationAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		// TODO Auto-generated method stub
		return null;
	}

}
