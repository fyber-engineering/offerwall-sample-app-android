/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe.mediation;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

public class SPBrandEngageMediationJSInterface {
	
	private static final String TAG = "SPBrandEngageMediationJSInterface";

	private static final String SP_GET_OFFERS = "Sponsorpay.MBE.SDKInterface.do_getOffer()";
	private static final String SP_TPN_JSON_KEY = "uses_tpn";
	
	public boolean playThroughTirdParty(WebView webView) {
		String jsResult = getJSValue(webView, SP_GET_OFFERS);
		if (StringUtils.notNullNorEmpty(jsResult)) {
			try {
				JSONObject json = new JSONObject(jsResult);
				return json.getBoolean(SP_TPN_JSON_KEY);
			} catch (JSONException e) {
				SponsorPayLogger.e(TAG, e.getLocalizedMessage(), e);
			}
		}
		return false;
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
			String code = "javascript:window." + interfaceName + ".setValue((function(){try{return " 
				+ expression + "+\"\";}catch(js_eval_err){return '';}})());";
			webView.loadUrl(code);
	 
			try {   
	            // Set a 1 second timeout in case there's an error
				latch.await(1, TimeUnit.SECONDS);
				return returnValue;
			} catch (InterruptedException e) {
				SponsorPayLogger.e(TAG, "Interrupted", e);
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
	
}
