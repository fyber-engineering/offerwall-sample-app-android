package com.sponsorpay.sdk.android;

import java.util.HashMap;

import com.sponsorpay.sdk.android.session.SPSession;

public class SPSessionManager {
	
	public static SPSessionManager INSTANCE = new SPSessionManager();
	
	private HashMap<String, SPSession> tokenSessionMap;
	
	private SPSession currentSession;
	
	private SPSessionManager() {
	}
	
	public SPSession getCurrentSession() {
		return currentSession;
	}
	
	public SPSession getSessionForToken(String token) {
		return tokenSessionMap.get(token);
	}
	
}
