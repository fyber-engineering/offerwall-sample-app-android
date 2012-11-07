/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * <p>
 * This class purpose is to create, handle and manage the {@link SPSession} objects.
 * </p>
 * 
 * <p>
 * It provide convenience methods for all the required operations through the use
 * of static methods.   
 * </p>
 *
 */
public class SPSessionManager {
	
	private static final String TAG = "SPSessionManager";
	
	protected static SPSessionManager INSTANCE = new SPSessionManager();
	
	private HashMap<String, SPSession> tokenSessionMap = new HashMap<String, SPSession>();
	
	private String currentSession;
	
	protected SPSessionManager() {
		SponsorPayLogger.d(TAG, "Session manager constructor");
	}

	private SPSession getSessionFromToken(String token) {
		SponsorPayLogger.d(TAG, "Session token: " + token);
		SPSession session = tokenSessionMap.get(token);
		if (session == null) {
			throw new RuntimeException("There is no session identified by " + token +
					"\nYou have to execute SponsorPay.start method first.");
		}
		return session;
	}
	
	protected String getSessionToken(String appId, String userId,
			String securityToken, Context context) {
		SPSession session;
		// to be removed when we no longer support appid set in manifest
		try {
			session = tokenSessionMap.get(SPSession.getSessionToken(
					appId, userId));
		} catch (Exception e) {
			HostInfo hi = new HostInfo(context);
			session = tokenSessionMap.get(SPSession.getSessionToken(hi.getAppId(), userId));
		}
		if (session == null) {
			session = new SPSession(appId, userId, securityToken, context);
			tokenSessionMap.put(session.getSessionToken(), session);
		} else if (StringUtils.notNullNorEmpty(securityToken)) {
			session.setSecurityToken(securityToken);
		}
		currentSession = session.getSessionToken();
		return currentSession;
	}
	
	
	/**
	 * Return the current {@link SPSession} or throws a {@link RuntimeException} if there's none.
	 * 
	 * @return the current {@link SPSession}
	 */
	public static SPSession getCurrentSession() {
		if (StringUtils.nullOrEmpty(INSTANCE.currentSession)) {
			throw new RuntimeException("No session was created yet.\n" +
					"You have to execute SponsorPay.start method first.");
		}
		return INSTANCE.getSessionFromToken(INSTANCE.currentSession);
	}
	
	/**
	 * Return the {@link SPSession} identified by the session token ID or throws a
	 * {@link RuntimeException} if there's none.
	 * 
	 * @param sessionToken
	 * 			The token id of the session.
	 * @return the {@link SPSession} identified by the session token.
	 */
	public static SPSession getSession(String sessionToken) {
		return INSTANCE.getSessionFromToken(sessionToken);
	}
	
	/**
	 * <p>
	 * Gets or creates a session with the provided parameters and sets it as the 
	 * current session. Throws an {@link IllegalArgumentException} if appId is null.
	 * </p>
	 * <p>
	 * IF a matching session if found for the pair appId-userId, the securityToken is updated
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
	 * @return the session token that identify the session for the provided
	 * 			parameters.
	 * 
	 * @deprecated This method will be removed from a future release of the SDK.
	 */
	public static String getSession(String appId, String userId,
			String securityToken, Context context) {
		return INSTANCE.getSessionToken(appId, userId, securityToken, context);
	}
	
	/**
	 * <p>
	 * Gets or creates a session with the provided parameters, initializes it with SponsorPay servers 
	 * and sets it as the current session. Throws a {@link IllegalArgumentException} if appId is null.
	 * </p>
	 * <p>
	 * IF a matching session if found for the pair appId-userId, the securityToken is updated
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
	 * @return
	 */
	public static String initialize(String appId, String userId,
			String securityToken, Context context) {
		Set<String> sessions = new HashSet<String>(SPSessionManager.getAllSessions());
		String sessionToken = INSTANCE.getSessionToken(appId, userId, securityToken,
						context);
		if (!sessions.contains(sessionToken)) {
			SponsorPayAdvertiser.register(context);
		}
//		StackTraceElement[] element = new Exception().getStackTrace();
//		element[0].
		return sessionToken;
	}
	
	/**
	 * Returns a set of all valid session token IDs
	 * 
	 * @return
	 */
	public static Set<String> getAllSessions() {
		return INSTANCE.tokenSessionMap.keySet();
	}
	
}
