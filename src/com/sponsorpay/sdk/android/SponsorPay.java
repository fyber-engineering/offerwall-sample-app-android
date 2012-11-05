/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android;

import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.session.SPSessionManager;

import android.content.Context;

/**
 * Contains version information of the SponsorPay SDK.
 */
public class SponsorPay {
	public static final int MAJOR_RELEASE_NUMBER = 1;
	public static final int MINOR_RELEASE_NUMBER = 10;
	public static final int BUGFIX_RELEASE_NUMBER = 0;
	public static final String RELEASE_VERSION_STRING = String.format("%d.%d.%d",
			MAJOR_RELEASE_NUMBER, MINOR_RELEASE_NUMBER, BUGFIX_RELEASE_NUMBER);
	
	
	public static String start(String appId, String userId,
			String securityToken, Context context) {
		String sessionToken = SPSessionManager.getSession(appId, userId, securityToken,
						context);
		SponsorPayAdvertiser.register(sessionToken, context, null);
		return sessionToken;
	}
	
	
	
}
