package com.sponsorpay.sdk.android.session;

import java.util.UUID;

import android.content.Context;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.publisher.UserId;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class SPSession {

	private final String mSessionToken;
	
	private final String mAppId;
	private final String mUserId;
	private String mSecurityToken;
	private final HostInfo mHostInfo;

	public SPSession(String appId, String userId, String securityToken, Context context) {
		mSecurityToken = securityToken;
		mHostInfo = new HostInfo(context);
		// to be removed when we no longer support appid in manifest
		if (StringUtils.notNullNorEmpty(appId)) {
			mAppId = appId;
			mHostInfo.setOverriddenAppId(appId);
	 	} else {
	 		mAppId = mHostInfo.getAppId();
	 	}
		mSessionToken = getSessionToken(mAppId, userId);
		if (StringUtils.nullOrEmpty(userId)) {
			mUserId = UserId.make(context, userId).toString();
		} else {
			mUserId = userId;
		}
	}
	
	public String getSessionToken() {
		return mSessionToken;
	}

	public String getAppId() {
		return mAppId;
	}

	public String getUserId() {
		return mUserId;
	}

	public String getSecurityToken() {
		return mSecurityToken;
	}
	
	public void setSecurityToken(String securityToken) {
		mSecurityToken = securityToken;
	}
	
	public HostInfo getHostInfo() {
		return mHostInfo;
	}
	
	public static String getSessionToken(String appId, String userId) {
		if (StringUtils.nullOrEmpty(appId)) {
			throw new IllegalArgumentException("AppID cannot be null!");
		}
		if (StringUtils.nullOrEmpty(userId)) {
			userId = StringUtils.EMPTY_STRING;
		}
		String token = appId + "-" + userId;
		return UUID.nameUUIDFromBytes(token.getBytes()).toString();
	}
	
	
}
