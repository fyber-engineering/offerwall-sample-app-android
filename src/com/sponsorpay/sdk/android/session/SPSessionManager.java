package com.sponsorpay.sdk.android.session;

import java.util.HashMap;
import java.util.Set;

import android.content.Context;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;


public class SPSessionManager {
	
	private static final String TAG = "SPSessionManager";
	
	private static SPSessionManager INSTANCE = new SPSessionManager();
	
	private HashMap<String, SPSession> tokenSessionMap = new HashMap<String, SPSession>();
	
	private String currentSession;
	
	private SPSessionManager() {
		SponsorPayLogger.d(TAG, "Session manager constructor");
	}

	private SPSession getSessionFromToken(String token) {
		SponsorPayLogger.d(TAG, "Session token: " + token);
		SPSession session = tokenSessionMap.get(token);
		if (session == null) {
			//FIXME throw a SP exception
			throw new RuntimeException("The session doesn't exist. You have to execute bla bla bla");
		}
		return session;
	}
	
	private String getSessionToken(String appId, String userId,
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
			currentSession = session.getSessionToken();
			tokenSessionMap.put(currentSession, session);
			SponsorPayAdvertiser.register(currentSession, context, null);
		} else if (StringUtils.nullOrEmpty(session.getSecurityToken())) {
			session.setSecurityToken(securityToken);
		}
		return session.getSessionToken();
	}
	
	public static SPSession getCurrentSession() {
		if (StringUtils.nullOrEmpty(INSTANCE.currentSession)) {
			throw new RuntimeException("There's no session yet.");
		}
		return INSTANCE.getSessionFromToken(INSTANCE.currentSession);
	}
	
	public static SPSession getSession(String sessionToken) {
		return INSTANCE.getSessionFromToken(sessionToken);
	}
	
	public static String getSession(String appId, String userId,
			String securityToken, Context context) {
		return INSTANCE.getSessionToken(appId, userId, securityToken, context);
	}
	
	public static Set<String> getAllSessions() {
		return INSTANCE.tokenSessionMap.keySet();
	}
	
}
