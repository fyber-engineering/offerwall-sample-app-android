package com.sponsorpay.sdk.android.session;

import java.util.HashMap;
import java.util.UUID;

public class SPSession {

	private final UUID mToken = UUID.randomUUID();
	
	private final String mAppId;
	private final String mUserId;
	private final String mSecurityToken;
	
	private final HashMap<String, String> mParameters;
	
	public SPSession(String appId, String userId, String securityToken,
			HashMap<String, String> parameters) {
		mAppId = appId;
		mUserId = userId;
		mSecurityToken = securityToken;
		mParameters = parameters;
	}
	
	// TODO read from asset property file
//	public SPSession() {
//		mAppId = StringUtils.EMPTY_STRING;
//		mUserId = StringUtils.EMPTY_STRING;
//		mSecurityToken = StringUtils.EMPTY_STRING;
//		mParameters = null;
//	}
	
	public String getUrl() {
		return "";
	}
	
	
	@Override
	public int hashCode() {
		return mToken.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof SPSession) {
			return o.hashCode() == hashCode();
		}
		return false;
	}
	
}
