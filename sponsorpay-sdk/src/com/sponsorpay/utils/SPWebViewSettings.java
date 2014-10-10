/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.lang.reflect.Method;

import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;

public class SPWebViewSettings {

	private static final String TAG = "SPWebViewSettings";

	public static void enablePlugins(WebSettings settings) {
		if (Build.VERSION.SDK_INT < 20 ) {
			try {
				Method pluginStateMethod = WebSettings.class.getMethod("setPluginState", PluginState.class);
				pluginStateMethod.invoke(settings, PluginState.ON);
			} catch (Exception e) {
				SponsorPayLogger.d(TAG, "Unable to enable plugin support for the webview" );
			}
		}
	}
	
}
