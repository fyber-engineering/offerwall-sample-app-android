/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import android.util.Log;

public class SponsorPayLogger {

	private static boolean logging = false;

	public static boolean toggleLogging() {
		logging = !logging;
		return logging;
	}
	
	public static boolean isLogging() {
		return logging;
	}

	public static boolean enableLogging(boolean shouldLog) {
		logging = shouldLog;
		return logging;
	}

	public static void e(String tag, String message) {
		if (logging) {
			Log.e(tag, message);
		}
	}

	public static void e(String tag, String message, Exception exception) {
		if (logging) {
			Log.w(tag, message, exception);
		}
	}

	public static void d(String tag, String message) {
		if (logging) {
			Log.d(tag, message);
		}
	}

	public static void i(String tag, String message) {
		if (logging) {
			Log.i(tag, message);
		}
	}

	public static void v(String tag, String message) {
		if (logging) {
			Log.v(tag, message);
		}
	}

	public static void w(String tag, String message) {
		if (logging) {
			Log.w(tag, message);
		}
	}

	public static void w(String tag, String message, Exception exception) {
		if (logging) {
			Log.w(tag, message, exception);
		}
	}


}
