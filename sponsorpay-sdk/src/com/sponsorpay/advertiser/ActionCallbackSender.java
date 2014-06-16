/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.advertiser;

import java.util.HashMap;
import java.util.Map;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;

/**
 * Runs in the background the Action Report Callback HTTP request.
 */
public class ActionCallbackSender extends AbstractCallbackSender {

	/**
	 * The API resource URL to contact when talking to the SponsorPay Advertiser API
	 */
	private static final String ACTIONS_URL_KEY = "actions"; 

	/**
	 * The key for encoding the action id parameter.
	 */
	private static final String ACTION_ID_KEY = "action_id";

	protected String mActionId;

	/**
	 * <p>
	 * Constructor. Reports the action completion.
	 * </p>
	 * 
	 * @param actionId
	 * 			  the id of the action to be reported
	 * @param credentials
	 *            the credentials used for this callback
	 * @param state
	 *            the advertiser state for getting information about previous callbacks
	 */

	public ActionCallbackSender(String actionId, SPCredentials credentials, SponsorPayAdvertiserState state) {
		super(credentials, state);
		mActionId = actionId;
	}

	@Override
	protected String getAnswerReceived() {
		return mState.getCallbackReceivedSuccessfulResponse(mActionId);
	}
	
	@Override
	protected String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(ACTIONS_URL_KEY);
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
