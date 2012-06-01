package com.sponsorpay.sdk.android.testapp.utils;

public class StringUtils {

	public static final String EMPTY_STRING = "";
	
	public static final boolean nullOrEmpty(String string) {
		return string == null || string.trim().equals(EMPTY_STRING);
	}
	
	public static final boolean notNullNorEmpty(String string) {
		return !nullOrEmpty(string);
	}

}
