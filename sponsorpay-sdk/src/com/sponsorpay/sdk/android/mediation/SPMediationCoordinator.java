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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationValidationEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPMediationCoordinator {

	private static final String TAG = "SPMediationCoordinator";

	private boolean mThirdPartySDKsStarted = false;
	
	private HashMap<String, SPMediationAdapter> mAdapters;
	
	public SPMediationCoordinator() {
		mAdapters = new HashMap<String, SPMediationAdapter>();
	}
	
	public void startThirdPartySDKs(Activity activity) {
		if (mThirdPartySDKsStarted) {
			return;
		}
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
		
		notifyAdaptersList(activity);
		mThirdPartySDKsStarted = true;
	}
	
	public boolean isProviderAvailable(String name) {
		return mAdapters.containsKey(name);
	}
	
	public void validateProvider(Context context, String adapterName,
			HashMap<String, String> contextData, SPMediationValidationEvent validationEvent) {
		if (isProviderAvailable(adapterName)) {
			mAdapters.get(adapterName).videosAvailable(context, validationEvent, contextData);
		} else {
			validationEvent.validationEventResult(adapterName, SPTPNValidationResult.SPTPNValidationNoVideoAvailable, contextData);
		}
	}
	
	public void startProviderEngagement(Activity parentActivity, String adapterName,
			HashMap<String, String> contextData,
			SPMediationVideoEvent videoEvent) {
		if (isProviderAvailable(adapterName)) {
			mAdapters.get(adapterName).startVideo(parentActivity, videoEvent, contextData);
		} else {
			videoEvent.videoEventOccured(adapterName, SPTPNVideoEvent.SPTPNVideoEventError, contextData);
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
