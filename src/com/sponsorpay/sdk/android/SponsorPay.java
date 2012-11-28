/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;

import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;


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
	public static final int MAJOR_RELEASE_NUMBER = 1;
	public static final int MINOR_RELEASE_NUMBER = 10;
	public static final int BUGFIX_RELEASE_NUMBER = 0;
	public static final String RELEASE_VERSION_STRING = String.format("%d.%d.%d",
			MAJOR_RELEASE_NUMBER, MINOR_RELEASE_NUMBER, BUGFIX_RELEASE_NUMBER);
	
	private static final String TAG = "SponsorPay";
	
	protected static SponsorPay INSTANCE = new SponsorPay();
	
	private HashMap<String, SPCredentials> tokensMap = new HashMap<String, SPCredentials>();
	
	private String currentCredentials;
	
	protected SponsorPay() {
	}

	private SPCredentials getCredentialsFromToken(String token) {
		SponsorPayLogger.d(TAG, "Credentials token: " + token);
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
		// to be removed when we no longer support appid set in manifest
		try {
			credentials = tokensMap.get(SPCredentials.getCredentialsToken(
					appId, userId));
		} catch (Exception e) {
			HostInfo hi = new HostInfo(context);
			credentials = tokensMap.get(SPCredentials.getCredentialsToken(hi.getAppId(), userId));
		}
		if (credentials == null) {
			credentials = new SPCredentials(appId, userId, securityToken, context);
			tokensMap.put(credentials.getCredentialsToken(), credentials);
		} else if (StringUtils.notNullNorEmpty(securityToken)) {
			credentials.setSecurityToken(securityToken);
		}
		currentCredentials = credentials.getCredentialsToken();
		return currentCredentials;
	}
	
	
	/**
	 * Return the current {@link SPCredentials} or throws a {@link RuntimeException} if there's none.
	 * 
	 * @return the current {@link SPCredentials}
	 */
	public static SPCredentials getCurrentCredentials() {
		if (StringUtils.nullOrEmpty(INSTANCE.currentCredentials)) {
			throw new RuntimeException("No credentials object was created yet.\n" +
					"You have to execute SponsorPay.start method first.");
		}
		return INSTANCE.getCredentialsFromToken(INSTANCE.currentCredentials);
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
	 * Gets or creates a credentials object with the provided parameters and sets it as the 
	 * current credentials. Throws an {@link IllegalArgumentException} if appId is null.
	 * </p>
	 * <p>
	 * If a matching credentials object is found for the pair appId-userId, the securityToken 
	 * is updated with the one provided as parameter (unless null is provided).
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
	 * @param context
	 *            Android application context.
	 *            
	 * @return the credentials token that identify the credentials for the provided
	 * 			parameters.
	 * 
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static String getCredentials(String appId, String userId,
			String securityToken, Context context) {
		return INSTANCE.getCredentialsToken(appId, userId, securityToken, context);
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
	 * @param context
	 *            Android application context.
	 *            
	 * @return the credentials token that identify the credentials for the provided
	 * 			parameters.
	 */
	public static String start(String appId, String userId,
			String securityToken, Context context) {
		Set<String> credentials = new HashSet<String>(SponsorPay.getAllCredentials());
		String credentialsToken = INSTANCE.getCredentialsToken(appId, userId, securityToken,
						context);
		if (!credentials.contains(credentialsToken)) {
			SponsorPayAdvertiser.register(context);
		}
		return credentialsToken;
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
