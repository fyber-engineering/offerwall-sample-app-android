package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;
import android.util.Log;

import com.hyprmx.android.sdk.HyprMXHelper;
import com.sponsorpay.mediation.mbe.HyprMXVideoMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;

public class HyprMXMediationAdapter extends SPMediationAdapter {

	private static final String DISTRIBUTOR_ID = "distributorId";
	private static final String PROPERTY_ID = "propertyId";
	private static final String USER_ID = "userId";

	public static final String TAG = HyprMXMediationAdapter.class.getSimpleName(); // just
																					// TAG
	private static final String ADAPTER_VERSION = "2.0.0"; // to verify what
//	private static final String ADAPTER_VERSION = "1.0.0"; // to verify what
															// version of
															// adapter is
															// supported
	private static final String ADAPTER_NAME = "Applifier"; // user for getting
//	private static final String ADAPTER_NAME = "HyprMX"; // user for getting
															// info from
															// adapters.config

	// private static final String SDK_KEY = "applovin.sdk.key"; //used for
	// getting info from adapters.info
	private HyprMXVideoMediationAdapter mVideoAdapter; // reference to video
														// adapter

	@Override
	public boolean startAdapter(final Activity activity) {
		// TODO Auto-generated method stub
		// create video adapter instance
		Log.d(TAG, "starting Applifier - HyprMX adapter");
		mVideoAdapter = new HyprMXVideoMediationAdapter(this);
		Log.d(TAG, "adapter details - distributorID: " + getDistributorId() + ", propertyID: " + getPropertyId() + ", userID: " + getUserId());
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				HyprMXHelper.getInstance(activity, getDistributorId(), getPropertyId(), getUserId()); // This only needs to happen once, but won't hurt.
			}
		});
		return true; // indicates that adapter has been run successfully
	}

	@Override
	public String getName() {
		Log.d(TAG, "get name");
		return ADAPTER_NAME;
	}

	@Override
	public String getVersion() {
		Log.d(TAG, "get version");
		return ADAPTER_VERSION;
	}

	@Override
	public SPBrandEngageMediationAdapter<? extends SPMediationAdapter> getVideoMediationAdapter() {
		Log.d(TAG, "get video adapter");
		return mVideoAdapter;
	}

	@Override
	public SPInterstitialMediationAdapter<? extends SPMediationAdapter> getInterstitialMediationAdapter() {
		// no interstitial adapter - return null
		Log.d(TAG, "get interstitial adapter");
		return null;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		Log.d(TAG, "get listeners");
		// TODO to add, what needs to be added - need help?
		return null;
	}

	public String getDistributorId() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, DISTRIBUTOR_ID, String.class);
	}

	public String getUserId() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, USER_ID, String.class);
	}

	public String getPropertyId() {
		return SPMediationConfigurator.getConfiguration(ADAPTER_NAME, PROPERTY_ID, String.class);
	}

	// private Bundle getMetadata(Activity activity) {
	// ApplicationInfo ai = null;
	//
	// // Extract the meta data from the package manager
	// try {
	// ai = activity.getPackageManager().getApplicationInfo(
	// activity.getPackageName(), PackageManager.GET_META_DATA);
	// } catch (NameNotFoundException e) {
	// return null;
	// }
	//
	// if (ai.metaData == null) {
	// ai.metaData = new Bundle();
	// }
	//
	// return ai.metaData;
	// }

	// private String getValueFromAppMetadata(Activity activity) {
	// Object retrievedValue;
	// retrievedValue = getMetadata(activity).get(SDK_KEY);
	// return retrievedValue == null ? null : retrievedValue.toString();
	// }

}
