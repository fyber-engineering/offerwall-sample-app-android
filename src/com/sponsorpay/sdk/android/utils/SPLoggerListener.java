/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import com.sponsorpay.sdk.android.utils.SponsorPayLogger.Level;


public interface SPLoggerListener {
	
	public void log(Level level, String tag, String message, Exception exception);
	
}
