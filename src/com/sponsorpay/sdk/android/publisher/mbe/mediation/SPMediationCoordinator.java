/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;

import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPMediationCoordinator {

	private static final String SP_GET_OFFERS = "Sponsorpay.MBE.SDKInterface.do_getOffer()";
	private static final String SP_TPN_JSON_KEY = "uses_tpn";

	private static final String TAG = "SPMediationCoordinator";

	private boolean thirdPartySDKsStarted = false;
	
	private HashMap<String, SPMediationAdaptor> adaptors;
	
	public SPMediationCoordinator() {
		adaptors = new HashMap<String, SPMediationAdaptor>();
	}
	
	public void startThirdPartySDKs() {
		if (thirdPartySDKsStarted) {
			return;
		}
		for(String className : SPMediationConfigurator.INSTANCE.getMediationAdaptors() ) {
			try {
				@SuppressWarnings("unchecked")
				Class<SPMediationAdaptor> adaptorClass = (Class<SPMediationAdaptor>) Class.forName(className);
				SPMediationAdaptor adaptor = adaptorClass.newInstance();
				String name = adaptor.getName();
				SponsorPayLogger.d(TAG, "Starting adaptor " + name);
				if (adaptor.startAdaptor()) {
					adaptors.put(name.toLowerCase(), adaptor);
					SponsorPayLogger.d(TAG, "Adaptor started");
				}
			} catch (ClassNotFoundException e) {
				SponsorPayLogger.e(TAG, "Adaptor not found - " + className, e);
			} catch (IllegalAccessException e) {
				SponsorPayLogger.e(TAG, "An error occured", e);
			} catch (InstantiationException e) {
				SponsorPayLogger.e(TAG, "An error occured while trying to instantiate " + className, e);
			}
		}
		
		thirdPartySDKsStarted = true;
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
		return adaptors.containsKey(name.toLowerCase());
	}
	

	public void validateProvider(String adaptorName,
			HashMap<String, String> contextData, SPMediationValidationEvent validationEvent) {
		if (isProviderAvailable(adaptorName)) {
			adaptors.get(adaptorName.toLowerCase()).videosAvailable(validationEvent, contextData);
		} else {
			validationEvent.validationEventResult(adaptorName, SPTPNValidationResult.SPTPNValidationNoVideoAvailable, contextData);
		}
	}
	

	public void startProviderEngagement(Activity parentActivity, String adaptorName,
			HashMap<String, String> contextData,
			SPMediationVideoEvent videoEvent) {
		if (isProviderAvailable(adaptorName)) {
			adaptors.get(adaptorName.toLowerCase()).startVideo(parentActivity, videoEvent, contextData);
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
	public void setValue(String value)
	{
		returnValue = value;
		try { latch.countDown(); } catch (Exception e) {} 
	}
 
	/**
	 * Gets the interface name
	 * @return
	 */
	public String getInterfaceName(){
		return this.interfaceName;
	}
	
}
