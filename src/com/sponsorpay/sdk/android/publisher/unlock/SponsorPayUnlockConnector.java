/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.unlock;

import java.util.Map;

import android.content.Context;

import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.AbstractConnector;
import com.sponsorpay.sdk.android.publisher.AbstractResponse;
import com.sponsorpay.sdk.android.publisher.AbstractResponse.RequestErrorType;
import com.sponsorpay.sdk.android.publisher.AsyncRequest;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

/**
 * <p>
 * Provides services to access the SponsorPay Unlock Server.
 * </p>
 */
public class SponsorPayUnlockConnector extends AbstractConnector implements
		SPUnlockResponseListener {
	/*
	 * API Resource URLs.
	 */
	private static final String SP_UNLOCK_SERVER_STAGING_BASE_URL = "https://staging-iframe.sponsorpay.com/vcs/v1/";
	private static final String SP_UNLOCK_SERVER_PRODUCTION_BASE_URL = "https://api.sponsorpay.com/vcs/v1/";
	private static final String SP_UNLOCK_REQUEST_RESOURCE = "items.json";

	/**
	 * {@link SPUnlockResponseListener} registered by the developer's code to be notified of the
	 * result of requests to the back end.
	 */
	private SPUnlockResponseListener mUserListener;

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
		super(context, credentialsToken);

		mUserListener = userListener;
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

		String baseUrl = SponsorPayPublisher.shouldUseStagingUrls() ? SP_UNLOCK_SERVER_STAGING_BASE_URL
				: SP_UNLOCK_SERVER_PRODUCTION_BASE_URL;

		String requestUrl = UrlBuilder.newBuilder(baseUrl + SP_UNLOCK_REQUEST_RESOURCE, mCredentials)
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

		response.setResponseListener(this);
		response.parseAndCallListener(mCredentials.getSecurityToken());
	}

	/**
	 * Implemented from {@link SPUnlockResponseListener}. Forwards the call to the user listener.
	 */
	@Override
	public void onSPUnlockRequestError(AbstractResponse response) {
		mUserListener.onSPUnlockRequestError(response);
	}

	/**
	 * Implemented from {@link SPUnlockResponseListener}. Forwards the call to the user listener.
	 */
	@Override
	public void onSPUnlockItemsStatusResponseReceived(UnlockedItemsResponse response) {
		mUserListener.onSPUnlockItemsStatusResponseReceived(response);
	}
}
