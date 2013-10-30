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
	
	private HashMap<String, SPMediationAdaptor> mAdaptors;
	
	public SPMediationCoordinator() {
		mAdaptors = new HashMap<String, SPMediationAdaptor>();
	}
	
	public void startThirdPartySDKs(Activity activity) {
		if (mThirdPartySDKsStarted) {
			return;
		}
		SponsorPayLogger.d(TAG, "Starting mediation providers...");
		for (Entry<String, List<String>> entry : SPMediationConfigurator.INSTANCE
				.getMediationAdaptors().entrySet()) {
			String className = entry.getKey();
			try {
				@SuppressWarnings("unchecked")
				Class<SPMediationAdaptor> adaptorClass = (Class<SPMediationAdaptor>) Class.forName(className);
				SPMediationAdaptor adaptor = adaptorClass.newInstance();
				String name = adaptor.getName();
				String version = adaptor.getVersion();
				
				SponsorPayLogger.d(TAG, String.format("Starting adaptor %s version %s", name, version));
				
				if (entry.getValue().contains(version)) {
					SponsorPayLogger.d(TAG, "Adaptor version is compatible with SDK. Proceeding...");
					if (adaptor.startAdaptor(activity)) {
						SponsorPayLogger.d(TAG, "Adaptor has been started successfully");
						mAdaptors.put(name.toLowerCase(), adaptor);
					}
				}
			} catch (ClassNotFoundException e) {
				SponsorPayLogger.e(TAG, "Adaptor not found - " + className, e);
			} catch (IllegalAccessException e) {
				SponsorPayLogger.e(TAG, "An error occured", e);
			} catch (InstantiationException e) {
				SponsorPayLogger.e(TAG, "An error occured while trying to instantiate " + className, e);
			}
		}
		
		notifyAdaptorsList(activity);
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
		return mAdaptors.containsKey(name.toLowerCase());
	}
	
	public void validateProvider(Context context, String adaptorName,
			HashMap<String, String> contextData, SPMediationValidationEvent validationEvent) {
		if (isProviderAvailable(adaptorName)) {
			mAdaptors.get(adaptorName.toLowerCase()).videosAvailable(context, validationEvent, contextData);
		} else {
			validationEvent.validationEventResult(adaptorName, SPTPNValidationResult.SPTPNValidationNoVideoAvailable, contextData);
		}
	}
	
	public void startProviderEngagement(Activity parentActivity, String adaptorName,
			HashMap<String, String> contextData,
			SPMediationVideoEvent videoEvent) {
		if (isProviderAvailable(adaptorName)) {
			mAdaptors.get(adaptorName.toLowerCase()).startVideo(parentActivity, videoEvent, contextData);
		} else {
			videoEvent.videoEventOccured(adaptorName, SPTPNVideoEvent.SPTPNVideoEventError, contextData);
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
	private void notifyAdaptorsList(Activity activity) {
		try {
			Method method = activity.getClass().getDeclaredMethod("notifyAdaptorsList", List.class);
			List<String> list = new LinkedList<String>(mAdaptors.keySet());
			method.invoke(activity, list);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}
}
