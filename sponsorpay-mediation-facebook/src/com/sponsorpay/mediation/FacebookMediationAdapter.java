package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;

import com.sponsorpay.mediation.interstitial.FacebookInterstitialMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;

public class FacebookMediationAdapter extends SPMediationAdapter {

	private static final String TAG = FacebookMediationAdapter.class.getSimpleName();
	private static final String ADAPTER_VERSION = "1.0.0";

	private static final String ADAPTER_NAME = "Appia";
//	private static final String ADAPTER_NAME = "Facebook";
	private FacebookInterstitialMediationAdapter mInterstitialAdapter;

	private static final String PLACEMENT_ID_KEY = "placementId";
	private static final String TEST_DEVICE_HASH_KEY = "testDeviceHash";

	/**
	 * Initializes the wrapped SDK, usually with the necessary credentials.
	 * 
	 * Parameters: activity - The parent activity calling this method
	 * 
	 * Returns: true if the adapter was successfully started, false otherwise
	 */

	@Override
	public boolean startAdapter(Activity activity) {
		// TODO do work here
		SponsorPayLogger.d(TAG, "Starting Facebook Advertising SDK.");
		mInterstitialAdapter = new FacebookInterstitialMediationAdapter(this, activity);
		return true;
	}

	/**
	 * Returns:the name of the wrapped network.
	 */
	@Override
	public String getName() {
		return ADAPTER_NAME;
	}

	/**
	 * Returns:the version of the wrapped network.
	 */
	@Override
	public String getVersion() {
		return ADAPTER_VERSION;
	}

	@Override
	public SPBrandEngageMediationAdapter<? extends SPMediationAdapter> getVideoMediationAdapter() {
		// there is no video adapter
		return null;
	}

	/**
	 * Returns the InterstitialAdapter which has been instantiated in
	 * startAdapter(Activity)
	 */
	@Override
	public SPInterstitialMediationAdapter<? extends SPMediationAdapter> getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

	public String getPlacementId() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, PLACEMENT_ID_KEY, String.class);
	}

	public String getTestDeviceId() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, TEST_DEVICE_HASH_KEY, String.class);
	}

}
