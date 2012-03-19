package com.sponsorpay.sdk.android.publisher;

import java.util.Map;

import android.content.Context;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.publisher.AsyncRequest.AsyncRequestResultListener;

public abstract class AbstractConnector implements AsyncRequestResultListener {
	/**
	 * Parameter key used to transmit the timestamp of the request.
	 */
	protected static final String URL_PARAM_KEY_TIMESTAMP = "timestamp";

	protected String remoteResourceUrl;
	
	/**
	 * Android application context.
	 */
	protected Context mContext;
	
	/**
	 * ID of the user for whom the requests will be made.
	 */
	protected UserId mUserId;
	
	/**
	 * {@link HostInfo} containing data about the host device and application, including its
	 * application ID.
	 */
	protected HostInfo mHostInfo;
	
	/**
	 * Security token used to sign requests to the server and verify its responses.
	 */
	protected String mSecurityToken;

	/**
	 * Map of custom key/values to add to the parameters on the requests.
	 */
	protected Map<String, String> mCustomParameters;
	
	protected AbstractConnector(Context context, String userId, HostInfo hostInfo, String securityToken) {
		mContext = context;
		mUserId = UserId.make(context, userId);
		mHostInfo = hostInfo;
		mSecurityToken = securityToken;
	}
	
	/**
	 * Sets a map of custom key/values to add to the parameters on the requests to the REST API.
	 */
	public void setCustomParameters(Map<String, String> customParams) {
		mCustomParameters = customParams;
	}
	
	/**
	 * Gets the current UNIX timestamp (in seconds) for the outbound requests.
	 * 
	 * @return
	 */
	protected String getCurrentUnixTimestampAsString() {
		final int MILLISECONDS_IN_SECOND = 1000;
		return String.valueOf(System.currentTimeMillis() / MILLISECONDS_IN_SECOND);
	}
}
