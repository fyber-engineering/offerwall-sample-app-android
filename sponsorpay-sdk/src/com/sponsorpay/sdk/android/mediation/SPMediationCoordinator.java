/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.mediation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

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
				SponsorPayLogger.d(TAG, "Starting mediation providers...");
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
								mAdapters.put(name.toLowerCase(), adapter);
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
	
	public boolean isProviderAvailable(String name, SPMediationFormat adFormat) {
		SPMediationAdapter adapter = mAdapters.get(name.toLowerCase());
		if (adapter != null) {
			return adapter.supportMediationFormat(adFormat);
		} else {
			return false;
		}
	}
	
	public void validateProvider(Context context, String adapterName, SPMediationFormat adFormat,
			HashMap<String, String> contextData, SPMediationValidationEvent validationEvent) {
		if (isProviderAvailable(adapterName, adFormat)) {
			mAdapters.get(adapterName).validate(context, adFormat, validationEvent, contextData);
		} else {
			validationEvent.validationEventResult(adapterName, SPTPNValidationResult.SPTPNValidationAdapterNotIntegrated, contextData);
		}
	}
	
	public void startProviderEngagement(Activity parentActivity, String adapterName, SPMediationFormat adFormat,
			HashMap<String, String> contextData,
			SPMediationEngagementEvent engagementEvent) {
		if (isProviderAvailable(adapterName, adFormat)) {
			mAdapters.get(adapterName).startEngagement(parentActivity, adFormat, engagementEvent, contextData);
		} else {
			//FIXME this should be generic
//			videoEvent.videoEventOccured(adapterName, SPTPNVideoEvent.SPTPNVideoEventError, contextData);
		}
	}
	
	// 
	private void notifyAdaptersList(Activity activity) {
		try {
			Method method = activity.getClass().getDeclaredMethod("notifyAdaptersList", List.class);
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
