/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

public class StringUtils {

	public static final String EMPTY_STRING = "";
	
	public static final boolean nullOrEmpty(String string) {
		return string == null || string.trim().equals(EMPTY_STRING);
	}
	
	public static final boolean notNullNorEmpty(String string) {
		return !nullOrEmpty(string);
	}

	public static String trim(String value) {
		if (value != null) {
			return value.trim();
		}
		return null;
	}
	
}
