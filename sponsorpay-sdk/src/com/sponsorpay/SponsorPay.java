/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.sponsorpay.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.mediation.SPMediationConfigurationRequester;
import com.sponsorpay.utils.HostInfo;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.cookies.PersistentHttpCookieStore;


/**
 * <p>
 * This class purpose is to create, handle and manage the {@link SPCredentials} objects.
 * </p>
 * 
 * <p>
 * It provide convenience methods for all the required operations through the use
 * of static methods.   
 * </p>
 *
 */
public class SponsorPay {
	public static final int MAJOR_RELEASE_NUMBER = 6;
	public static final int MINOR_RELEASE_NUMBER = 5;
	public static final int BUGFIX_RELEASE_NUMBER = 0;
	public static final String RELEASE_VERSION_STRING = MAJOR_RELEASE_NUMBER + "." + 
				MINOR_RELEASE_NUMBER + "." + BUGFIX_RELEASE_NUMBER;
	
	public static final String TAG = "SponsorPay";
	
	protected static SponsorPay INSTANCE = new SponsorPay();
	
	private HashMap<String, SPCredentials> tokensMap = new HashMap<String, SPCredentials>();
	
	private SPCredentials currentCredentials;
	private HostInfo mHostInfo;
	
	protected SponsorPay() {
	}

	private SPCredentials getCredentialsFromToken(String token) {
		SPCredentials credendials = tokensMap.get(token);
		if (credendials == null) {
			throw new RuntimeException("There are no credentials identified by " + token +
					"\nYou have to execute SponsorPay.start method first.");
		}
		return credendials;
	}
	
	protected String getCredentialsToken(String appId, String userId,
			String securityToken, Context context) {
		SPCredentials credentials;
		
		credentials = tokensMap.get(SPCredentials.getCredentialsToken(
				appId, userId));
		if (credentials == null) {
			credentials = new SPCredentials(appId, userId, securityToken, context);
			tokensMap.put(credentials.getCredentialsToken(), credentials);
		} else if (StringUtils.notNullNorEmpty(securityToken)) {
			credentials.setSecurityToken(securityToken);
		}
		currentCredentials = credentials;
		return currentCredentials.getCredentialsToken();
	}
	
	/**
	 * Return the current {@link SPCredentials} or throws a {@link RuntimeException} if there's none.
	 * 
	 * @return the current {@link SPCredentials}
	 */
	public static SPCredentials getCurrentCredentials() {
		if (INSTANCE.currentCredentials == null) {
			throw new RuntimeException("Please start the SDK before accessing any of its resources.\n" +
					"You have to execute SponsorPay.start method first.");
		}
		return INSTANCE.currentCredentials;
	}
	
	/**
	 * Return the {@link SPCredentials} identified by the credentials token ID or throws a
	 * {@link RuntimeException} if there's none.
	 * 
	 * @param credentialsToken
	 * 			The token id of the credentials.
	 * @return the {@link SPCredentials} identified by the credentials token.
	 */
	public static SPCredentials getCredentials(String credentialsToken) {
		return INSTANCE.getCredentialsFromToken(credentialsToken);
	}
	
	/**
	 * <p>
	 * Gets or creates a credentials object with the provided parameters, initializes it with SponsorPay servers 
	 * and sets it as the current credentials. Throws a {@link IllegalArgumentException} if appId is null.
	 * </p>
	 * <p>
	 * If a matching credentials object is found for the pair appId-userId, the securityToken is updated
	 * with the one provided as parameter (unless null is provided).
	 * </p> 
	 * 
	 * @param appId
	 *            Application ID assigned by SponsorPay. Provide null to read the Application ID
	 *            from the Application Manifest.
	 * @param userId
	 *            The ID of the user for which the delta of coins will be requested.
	 * @param securityToken
	 *            Security Token associated with the provided Application ID. It's used to sign the
	 *            requests and verify the server responses.
	 * @param activity
	 *            Current Android activity.
	 *            
	 * @return the credentials token that identify the credentials for the provided
	 * 			parameters.
	 */
	public static String start(String appId, String userId,
			String securityToken, Activity activity) {
		
		if (HostInfo.isSupportedDevice()) {
			Log.i(TAG, "Only devices running Android API level 10 and above are supported");
		}
		Set<String> credentials = new HashSet<String>(SponsorPay.getAllCredentials());
		Context context = activity.getApplicationContext();
		boolean firstStart = false;
		if (credentials.isEmpty()) {
			HostInfo.getHostInfo(context);
			firstStart = true;
		}
		String credentialsToken = INSTANCE.getCredentialsToken(appId, userId, securityToken,
						context);
		if (firstStart) {
			CookieSyncManager.createInstance(activity);
			CookieStore store = new PersistentHttpCookieStore(activity);
			CookieManager cookieManager = new CookieManager(store,CookiePolicy.ACCEPT_ORIGINAL_SERVER);
			CookieHandler.setDefault(cookieManager);
			SPMediationConfigurationRequester.requestConfig(INSTANCE.currentCredentials, activity);
		}
		if (!credentials.contains(credentialsToken)) {
			SponsorPayAdvertiser.register(context);
		}
		
		return credentialsToken;
	}

	public static HostInfo getHostInfo() {
		return INSTANCE.mHostInfo;
	}
	
	/**
	 * Returns a set of all valid credentials token IDs
	 * 
	 * @return
	 */
	public static Set<String> getAllCredentials() {
		return INSTANCE.tokensMap.keySet();
	}
	
}
