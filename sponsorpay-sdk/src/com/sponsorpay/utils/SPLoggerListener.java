/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import com.sponsorpay.utils.SponsorPayLogger.Level;


public interface SPLoggerListener {
	
	public void log(Level level, String tag, String message, Exception exception);
	
}
