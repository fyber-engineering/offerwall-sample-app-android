/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.unlock;

import java.util.Map;

import android.content.Context;

import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.AbstractConnector;
import com.sponsorpay.sdk.android.publisher.AbstractResponse.RequestErrorType;
import com.sponsorpay.sdk.android.publisher.AsyncRequest;
import com.sponsorpay.sdk.android.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.UrlBuilder;

/**
 * <p>
 * Provides services to access the SponsorPay Unlock Server.
 * </p>
 */
public class SponsorPayUnlockConnector extends AbstractConnector<SPUnlockResponseListener> { 
	
	/*
	 * API Resource URLs.
	 */
	private static final String UNLOCK_URL_KEY = "unlock_items";

	/**
	 * Initializes a new SponsorPayUnlockConnector instance.
	 * 
	 * @param context
	 *            Android application context.
	 * @param credentialsToken
	 *            The token identifying the {@link SPCredentials} to be used.
	 * @param userListener
	 *            Listener which will be notified of asynchronous responses to the requests sent by
	 *            this app.
	 */
	public SponsorPayUnlockConnector(Context context, String credentialsToken, 
			SPUnlockResponseListener userListener) {
		super(context, credentialsToken, userListener);
	}

	/**
	 * Sends a request to fetch the status of the Unlock items. The result will be delivered
	 * asynchronously to the registered listener.
	 */
	public void fetchItemsStatus() {
		String[] requestUrlExtraKeys = new String[] { URL_PARAM_KEY_TIMESTAMP };
		String[] requestUrlExtraValues = new String[] { getCurrentUnixTimestampAsString() };

		Map<String, String> extraKeysValues = UrlBuilder.mapKeysToValues(requestUrlExtraKeys,
				requestUrlExtraValues);

		if (mCustomParameters != null) {
			extraKeysValues.putAll(mCustomParameters);
		}

		String baseUrl = SponsorPayBaseUrlProvider.getBaseUrl(UNLOCK_URL_KEY);
		
		String requestUrl = UrlBuilder.newBuilder(baseUrl, mCredentials)
				.addExtraKeysValues(extraKeysValues).addScreenMetrics().buildUrl();

		SponsorPayLogger.d(getClass().getSimpleName(),
				"Unlock items status request will be sent to URL + params: " + requestUrl);

		AsyncRequest requestTask = new AsyncRequest(requestUrl, this);
		requestTask.execute();
	}

	@Override
	public void onAsyncRequestComplete(AsyncRequest requestTask) {
		SponsorPayLogger.d(getClass().getSimpleName(), String.format(
				"SP Unlock server Response, status code: %d, response body: %s, signature: %s",
				requestTask.getHttpStatusCode(), requestTask.getResponseBody(), requestTask
						.getResponseSignature()));

		UnlockedItemsResponse response = new UnlockedItemsResponse();
		if (requestTask.didRequestThrowError()) {
			response.setErrorType(RequestErrorType.ERROR_NO_INTERNET_CONNECTION);
		} else {
			response.setResponseData(requestTask.getHttpStatusCode(),
					requestTask.getResponseBody(), requestTask.getResponseSignature());
		}

		response.setResponseListener(mUserListener);
		response.parseAndCallListener(mCredentials.getSecurityToken());
	}
}
