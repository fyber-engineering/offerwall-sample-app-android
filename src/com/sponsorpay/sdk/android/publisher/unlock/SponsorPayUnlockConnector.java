package com.sponsorpay.sdk.android.publisher.unlock;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.AbstractConnector;
import com.sponsorpay.sdk.android.publisher.AbstractResponse;
import com.sponsorpay.sdk.android.publisher.AbstractResponse.RequestErrorType;
import com.sponsorpay.sdk.android.publisher.AsyncRequest;

public class SponsorPayUnlockConnector extends AbstractConnector implements
		SPUnlockResponseListener {
	/*
	 * API Resource URLs.
	 */
	private static final String SP_UNLOCK_SERVER_BASE_URL = "http://api.sponsorpay.com/vcs/v1/";
	private static final String SP_UNLOCK_REQUEST_RESOURCE = "items.json";

	/**
	 * {@link SPUnlockResponseListener} registered by the developer's code to be notified of the
	 * result of requests to the back end.
	 */
	private SPUnlockResponseListener mUserListener;

	public SponsorPayUnlockConnector(Context context, String userId,
			SPUnlockResponseListener userListener, HostInfo hostInfo, String securityToken) {
		super(context, userId, hostInfo, securityToken);
		// TODO Auto-generated constructor stub
	}

	public void fetchItemsStatus() {
		String[] requestUrlExtraKeys = new String[] { URL_PARAM_KEY_TIMESTAMP };
		String[] requestUrlExtraValues = new String[] { getCurrentUnixTimestampAsString() };
		
		Map<String, String> extraKeysValues = UrlBuilder.mapKeysToValues(requestUrlExtraKeys,
				requestUrlExtraValues);

		if (mCustomParameters != null) {
			extraKeysValues.putAll(mCustomParameters);
		}
		
		String requestUrl = UrlBuilder.buildUrl(SP_UNLOCK_SERVER_BASE_URL
				+ SP_UNLOCK_REQUEST_RESOURCE, mUserId, mHostInfo, extraKeysValues,
				mSecurityToken);
		
		Log.d(getClass().getSimpleName(),
				"Delta of coins request will be sent to URL + params: " + requestUrl);
		
		AsyncRequest requestTask = new AsyncRequest(requestUrl, this);
		requestTask.execute();
	}

	@Override
	public void onAsyncRequestComplete(AsyncRequest requestTask) {
		Log.d(getClass().getSimpleName(), String.format(
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
		response.parseAndCallListener(mSecurityToken);
	}

	@Override
	public void onSPUnlockRequestError(AbstractResponse response) {
		mUserListener.onSPUnlockRequestError(response);
	}

	@Override
	public void onSPUnlockItemsStatusResponseReceived(UnlockedItemsResponse response) {
		mUserListener.onSPUnlockItemsStatusResponseReceived(response);
	}
}
