/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import java.util.Map;

import com.sponsorpay.sdk.android.credentials.SPCredentials;

/**
 * Runs in the background the Action Report Callback HTTP request.
 */
public class InstallCallbackSender extends AbstractCallbackSender{
	
	/**
	 * The API resource URL to contact when talking to the SponsorPay Advertiser API
	 */
	private static final String API_PRODUCTION_RESOURCE_URL = "https://service.sponsorpay.com/installs/v2";
	private static final String API_STAGING_RESOURCE_URL = "https://staging-sws.sponsorpay.com/installs/v2";
	
	/**
	 * Map of custom parameters to be sent in the callback request.
	 */
	private Map<String, String> mCustomParams;

	/**
	 * <p>
	 * Constructor. Reports an application installation.
	 * </p>
	 * 
	 * @param credentials
	 *            the credentials used for this callback
	 * @param state
	 *            the advertiser state for getting information about previous callbacks
	 */
	public InstallCallbackSender(SPCredentials credentials, SponsorPayAdvertiserState state) {
		super(credentials, state);
	}

	/**
	 * Sets the map of custom parameters to be sent in the callback request.
	 */
	public void setCustomParams(Map<String, String> customParams) {
		mCustomParams = customParams;
	}

	@Override
	protected String getBaseUrl() {
		return SponsorPayAdvertiser.shouldUseStagingUrls() ? API_STAGING_RESOURCE_URL
				: API_PRODUCTION_RESOURCE_URL;
	}
	
	@Override
	protected String getAnswerReceived() {
		return mState.getCallbackReceivedSuccessfulResponse(null);
	}
	
	@Override
	protected Map<String, String> getParams() {
		return mCustomParams;
	}
	
	@Override
	protected void processRequest(Boolean callbackWasSuccessful) {
		mState.setCallbackReceivedSuccessfulResponse(null, true);
	}

}
