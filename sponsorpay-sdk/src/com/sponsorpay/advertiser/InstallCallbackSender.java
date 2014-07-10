/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.advertiser;

import java.util.Map;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;

/**
 * Runs in the background the Action Report Callback HTTP request.
 */
public class InstallCallbackSender extends AbstractCallbackSender{
	
	/**
	 * The API resource URL to contact when talking to the SponsorPay Advertiser API
	 */
	private static final String INSTALLS_URL_KEY = "installs";
	
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

	@Override
	protected String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(INSTALLS_URL_KEY);
	}
	
	@Override
	protected String getAnswerReceived() {
		return mState.getCallbackReceivedSuccessfulResponse(null);
	}
	
	@Override
	protected Map<String, String> getParams() {
		return null;
	}
	
	@Override
	protected void processRequest(Boolean callbackWasSuccessful) {
		mState.setCallbackReceivedSuccessfulResponse(null, true);
	}

}
