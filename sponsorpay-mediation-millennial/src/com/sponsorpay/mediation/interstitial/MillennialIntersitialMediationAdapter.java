/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.interstitial;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMInterstitial;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.RequestListener;
import com.sponsorpay.mediation.MillennialMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;

public class MillennialIntersitialMediationAdapter extends
		SPInterstitialMediationAdapter<MillennialMediationAdapter> implements RequestListener  {

	public static final String RUNTIME_METADATA_KEY = "runtimeMetadata";

	private static final String TAG = "MillennialIntersitialMediationAdapter";
	private static final String METADATA_KEY = "metadata";
	
	private MMInterstitial mInterstitial;

	public MillennialIntersitialMediationAdapter(MillennialMediationAdapter adapter, Activity activity) {
		super(adapter);
		this.mActivityRef = new WeakReference<Activity>(activity);
		mInterstitial = new MMInterstitial(activity);
		String appId = mAdapter.getAppid();
		mInterstitial.setApid(appId);
		//Set your metadata in the MMRequest object
		MMRequest request = new MMRequest();

		Map<String, String> requestMetadata = new HashMap<String, String>();
		requestMetadata.putAll(getConfigMetadata());
		requestMetadata.putAll(getRuntimeMetadata());
		request.setMetaValues(requestMetadata);
		
		//Add the MMRequest object to your MMInterstitial.
		mInterstitial.setMMRequest(request);
		mInterstitial.setListener(this);
		checkForAds(activity);
	}

	@Override
	public boolean show(Activity parentActivity) {
		return mInterstitial.display();
	}


	@Override
	protected void checkForAds(Context context) {
		if (mInterstitial.isAdAvailable() ) {
			setAdAvailable();
		} else if(getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mInterstitial.fetch();
				}
			});
		}
	}

	//RequestListener
	@Override
	public void MMAdOverlayClosed(MMAd ad) {
		fireCloseEvent();
	}

	@Override
	public void MMAdOverlayLaunched(MMAd ad) {
		fireImpressionEvent();
	}

	@Override
	public void MMAdRequestIsCaching(MMAd ad) {
		// do nothing
	}

	@Override
	public void onSingleTap(MMAd ad) {
		fireClickEvent();
	}

	@Override
	public void requestCompleted(MMAd ad) {
		setAdAvailable();
	}

	@Override
	public void requestFailed(MMAd ad, MMException exception) {
		// ad is already precached, mark it as available
		if (exception.getCode() == 17) {
			setAdAvailable();
		} else {
			fireValidationErrorEvent(exception.getLocalizedMessage());
		}
	}
	
	// Helpers
	private Map<String, String> getConfigMetadata() {
		JSONObject metadata = SPMediationConfigurator.getConfiguration(getName(), METADATA_KEY, JSONObject.class);
		if(metadata != null) {
			try {
				Map<String, String> map = new HashMap<String, String>(
						metadata.length());
				@SuppressWarnings("unchecked")
				Iterator<String> itr = metadata.keys();
				while (itr.hasNext()) {
					String key = itr.next();
					String value = metadata.getString(key);
					map.put(key, value);
				}
				return map;
			} catch (JSONException e) {
				SponsorPayLogger.d(TAG, "Error parsing json - " + e.getLocalizedMessage() );
			}
		}
		return Collections.emptyMap();
	}
	
	@SuppressWarnings({ "unchecked" })
	private Map<String, String> getRuntimeMetadata() {
		Map<String, String> runtimeMetadata = SPMediationConfigurator.getConfiguration(getName(), RUNTIME_METADATA_KEY, HashMap.class);
		if (runtimeMetadata != null) {
			return runtimeMetadata;
		}
		return Collections.emptyMap();
	}
	
}