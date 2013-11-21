/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

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

import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPMediationCoordinator {

	private static final String SP_GET_OFFERS = "Sponsorpay.MBE.SDKInterface.do_getOffer()";
	private static final String SP_TPN_JSON_KEY = "uses_tpn";

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
		
		notifyAdaptersList(activity);
		mThirdPartySDKsStarted = true;
	}
	

	public boolean playThroughTirdParty(WebView webView) {
		String jsResult = 
				getJSValue(webView, SP_GET_OFFERS);
		if (jsResult != null) {
			try {
				JSONObject json = new JSONObject(jsResult);
				return json.getBoolean(SP_TPN_JSON_KEY);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean isProviderAvailable(String name) {
		return mAdapters.containsKey(name.toLowerCase());
	}
	
	public void validateProvider(Context context, String adapterName,
			HashMap<String, String> contextData, SPMediationValidationEvent validationEvent) {
		if (isProviderAvailable(adapterName)) {
			mAdapters.get(adapterName.toLowerCase()).videosAvailable(context, validationEvent, contextData);
		} else {
			validationEvent.validationEventResult(adapterName, SPTPNValidationResult.SPTPNValidationNoVideoAvailable, contextData);
		}
	}
	
	public void startProviderEngagement(Activity parentActivity, String adapterName,
			HashMap<String, String> contextData,
			SPMediationVideoEvent videoEvent) {
		if (isProviderAvailable(adapterName)) {
			mAdapters.get(adapterName.toLowerCase()).startVideo(parentActivity, videoEvent, contextData);
		} else {
			videoEvent.videoEventOccured(adapterName, SPTPNVideoEvent.SPTPNVideoEventError, contextData);
		}
	}
	
	// HELPER methods for sync JS reply
	// http://www.gutterbling.com/blog/synchronous-javascript-evaluation-in-android-webview/#codesyntax_1
	/** The javascript interface name for adding to web view. */
	private final String interfaceName = "SynchJS";
 
	/** Countdown latch used to wait for result. */
	private CountDownLatch latch = null;
 
	/** Return value to wait for. */
	private String returnValue;
	
	/**
	 * Evaluates the expression and returns the value.
	 * @param webView
	 * @param expression
	 * @return
	 */
	private String getJSValue(WebView webView, String expression)
	{
		if (webView != null) {
			latch = new CountDownLatch(1); 
			String code = "javascript:window." + interfaceName + ".setValue((function(){try{return " + expression
				+ "+\"\";}catch(js_eval_err){return '';}})());";
			webView.loadUrl(code);
	 
			try {   
	            // Set a 1 second timeout in case there's an error
				latch.await(1, TimeUnit.SECONDS);
				return returnValue;
			} catch (InterruptedException e) {
				Log.e(TAG, "Interrupted", e);
			}
		}
		return null;
	}
 
 
	/**
	 * Receives the value from the javascript.
	 * @param value
	 */
	@JavascriptInterface
	public void setValue(String value)
	{
		returnValue = value;
		try {
			latch.countDown();
		} catch (Exception e) {
		}
	}
 
	/**
	 * Gets the interface name
	 * @return
	 */
	public String getInterfaceName(){
		return this.interfaceName;
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
