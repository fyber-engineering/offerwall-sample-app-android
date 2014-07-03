/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.mbe.mediation.SPMediationValidationEvent;
import com.sponsorpay.publisher.mbe.mediation.SPMediationVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.utils.SponsorPayLogger;

public class SPMediationCoordinator {

	public final static SPMediationCoordinator INSTANCE =  new SPMediationCoordinator();
	
	private static final String TAG = "SPMediationCoordinator";

	private boolean mThirdPartySDKsStarted = false;
	
	private HashMap<String, SPMediationAdapter> mAdapters;
	
	private SPMediationCoordinator() {
		mAdapters = new HashMap<String, SPMediationAdapter>() {
			private static final long serialVersionUID = 3512263289646462602L;

			@Override
			public SPMediationAdapter get(Object key) {
				return super.get(key.toString().toLowerCase());
			}
			
			@Override
			public SPMediationAdapter put(String key, SPMediationAdapter value) {
				return super.put(key.toString().toLowerCase(), value);
			}
		};
	}
	
	public void startMediationAdapters(final Activity activity) {
		if (mThirdPartySDKsStarted) {
			return;
		}
		
		mThirdPartySDKsStarted = true;
		
		// not using new thread handler because of the required Looper.quit to kill it 
		new Thread("SPMediationCoordinator") {
			public void run() {
				SponsorPayLogger.d(TAG, "Starting mediation networks...");
				for (Entry<String, List<String>> entry : SPMediationConfigurator.INSTANCE
						.getMediationAdapters().entrySet()) {
					String className = entry.getKey();
					try {
						@SuppressWarnings("unchecked")
						Class<SPMediationAdapter> adapterClass = (Class<SPMediationAdapter>) Class.forName(className);
						SPMediationAdapter adapter = adapterClass.newInstance();
						String name = adapter.getName();
						String version = adapter.getVersion();
						
						SponsorPayLogger.d(TAG, String.format("Starting adapter %s version %s", name, version));
						
						if (entry.getValue().contains(version)) {
							SponsorPayLogger.d(TAG, "Adapter version is compatible with SDK. Proceeding...");
							if (adapter.startAdapter(activity)) {
								SponsorPayLogger.d(TAG, "Adapter has been started successfully");
								mAdapters.put(name, adapter);
							}
						}
					} catch (ClassNotFoundException e) {
						SponsorPayLogger.e(TAG, "Adapter not found - " + className, e);
					} catch (IllegalAccessException e) {
						SponsorPayLogger.e(TAG, "An error occured", e);
					} catch (InstantiationException e) {
						SponsorPayLogger.e(TAG, "An error occured while trying to instantiate " + className, e);
					}
				}
				SponsorPayLogger.d(TAG, "Initialization complete...");
				
				notifyAdaptersList(activity);
			};
		}.start();
	}
	
	public boolean isNetworkAvailable(String name, SPMediationAdFormat adFormat) {
		SPMediationAdapter adapter = mAdapters.get(name);
		if (adapter != null) {
			return adapter.supportMediationFormat(adFormat);
		} else {
			return false;
		}
	}
	
	
	// Rewarded Videos
	public void validateVideoNetwork(Context context, String adapterName,
			HashMap<String, String> contextData,
			SPMediationValidationEvent validationEvent) {
		if (isNetworkAvailable(adapterName, SPMediationAdFormat.RewardedVideo)) {
			mAdapters.get(adapterName).validateVideoNetwork(context, validationEvent, contextData);
		} else {
			validationEvent.validationEventResult(adapterName, SPTPNVideoValidationResult.SPTPNValidationAdapterNotIntegrated, contextData);
		}
	}
	
	public void startVideoEngagement(Activity parentActivity, String adapterName, 
			HashMap<String, String> contextData,
			SPMediationVideoEvent videoEvent) {
		if (isNetworkAvailable(adapterName, SPMediationAdFormat.RewardedVideo)) {
			mAdapters.get(adapterName).startVideoEngagement(parentActivity, videoEvent, contextData);
		} else {
			videoEvent.videoEventOccured(adapterName, SPTPNVideoEvent.SPTPNVideoEventAdapterNotIntegrated, contextData);
		}
	}
	
	// Interstitials
	public boolean validateInterstitialNetwork(Context context, SPInterstitialAd ad) {
		String adapterName = ad.getProviderType();
		if (isNetworkAvailable(adapterName, SPMediationAdFormat.Interstitial)) {
			return mAdapters.get(adapterName).validateInterstitialNetwork(context, ad);
		} else {
			return false;
		}
	}
	
	public boolean showInterstitial(Activity parentActivity, SPInterstitialAd ad) {
		String adapterName = ad.getProviderType();
		if (isNetworkAvailable(adapterName, SPMediationAdFormat.Interstitial)) {
			return mAdapters.get(adapterName).showInterstitial(parentActivity, ad);
		} else {
			return false;
		}
	}
	
	// 
	private void notifyAdaptersList(Activity activity) {
		try {
			Method method = activity.getClass().getDeclaredMethod("notifyMediationAdaptersList", List.class);
			List<String> list = new LinkedList<String>(mAdapters.keySet());
			method.invoke(activity, list);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}
}
