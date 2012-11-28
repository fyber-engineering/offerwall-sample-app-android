/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import java.util.HashMap;
import java.util.Map;

import com.sponsorpay.sdk.android.credentials.SPCredentials;

/**
 * Runs in the background the Advertiser Callback HTTP request.
 */
public class RewardedActionCallbackSender extends AbstractCallbackSender {

	/**
	 * The API resource URL to contact when talking to the SponsorPay Advertiser API
	 */
	private static final String API_PRODUCTION_RESOURCE_URL = "https://service.sponsorpay.com/installs/v2";
	private static final String API_STAGING_RESOURCE_URL = "https://staging.sws.sponsorpay.com/installs/v2";
	
	/**
	 * The key for encoding the action id parameter.
	 */
	private static final String ACTION_ID_KEY = "action_id";

	protected String mActionId;

	/**
	 * <p>
	 * Constructor. Sets the request callback listener and stores the host information.
	 * </p>
	 * See {@link AdvertiserHostInfo} and {@link APIResultListener}.
	 * 
	 * @param credentials
	 *            the credentials used for this callback
	 * @param state
	 *            the advertiser state for getting information about previous callbacks
	 */

	public RewardedActionCallbackSender(String actionId, SPCredentials credentials, SponsorPayAdvertiserState state) {
		super(credentials, state);
		mActionId = actionId;
	}

	@Override
	protected String getAnswerReceived() {
		return mState.getCallbackReceivedSuccessfulResponse(mActionId);
	}
	
	@Override
	protected String getBaseUrl() {
		return SponsorPayAdvertiser.shouldUseStagingUrls() ? API_STAGING_RESOURCE_URL
				: API_PRODUCTION_RESOURCE_URL;
	}

	@Override
	protected Map<String, String> getParams() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(ACTION_ID_KEY, mActionId);
		return map;
	}
	
	@Override
	protected void processRequest(Boolean callbackWasSuccessful) {
		mState.setCallbackReceivedSuccessfulResponse(mActionId, true);
	}

}
