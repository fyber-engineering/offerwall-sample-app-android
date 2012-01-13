/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.OfferBanner.AdShape;

/**
 * Requests a new offer banner to the SponsorPay server and notifies the registered
 * {@link SPOfferBannerListener} of the result of the request.
 */
public class OfferBannerRequest implements AsyncRequest.ResultListener {

	private static final String OFFERBANNER_PRODUCTION_BASE_URL = "http://iframe.sponsorpay.com/mobile";
	private static final String OFFERBANNER_STAGING_BASE_URL = "http://staging.iframe.sponsorpay.com/mobile";

	private static final String OFFERBANNER_PRODUCTION_DOMAIN = "http://iframe.sponsorpay.com";
	private static final String OFFERBANNER_STAGING_DOMAIN = "http://staging.iframe.sponsorpay.com";

	private static final String URL_PARAM_OFFERBANNER_KEY = "banner";

	private static final String STATE_OFFSET_COUNT_KEY = "OFFERBANNER_AVAILABLE_RESPONSE_COUNT";

	/**
	 * Android application context.
	 */
	private Context mContext;

	/**
	 * Registered {@link SPOfferBannerListener} which will be notified of the result of the request.
	 */
	private SPOfferBannerListener mListener;

	/**
	 * User ID to include in the request.
	 */
	private String mUserId;

	/**
	 * {@link AdShape} whose description will be sent to the server in the request.
	 */
	private OfferBanner.AdShape mOfferBannerAdShape;

	/**
	 * {@link HostInfo} instance containing the Application ID and data about the device.
	 */
	private HostInfo mHostInfo;

	/**
	 * BaseUrl of the resource from which the ad will be requested.
	 */
	private String mBaseUrl;

	/**
	 * Base domain of the request URL which will be passed to the WebView which displays the
	 * returned banner to load associated resources (like images).
	 */
	private String mBaseDomain;

	/**
	 * Currency Name to be sent in the request.
	 */
	private String mCurrencyName;

	/**
	 * {@link AsyncRequest} used to send the request in the background.
	 */
	private AsyncRequest mAsyncRequest;

	/**
	 * Initializes a new instance and sends the request immediately.
	 * 
	 * @param context
	 *            Android application context.
	 * @param userId
	 *            ID of the user on whose behalf the banner will be requested.
	 * @param hostInfo
	 *            {@link HostInfo} instance containing the Application ID and data about the device.
	 * @param listener
	 *            {@link SPOfferBannerListener} which will be notified of the result of the request.
	 * @param offerBannerAdShape
	 *            {@link AdShape} of the requested banner.
	 * @param currencyName
	 *            Currency Name to be sent in the request.
	 */
	public OfferBannerRequest(Context context, String userId, HostInfo hostInfo,
			SPOfferBannerListener listener, OfferBanner.AdShape offerBannerAdShape,
			String currencyName) {

		mContext = context;
		mListener = listener;
		mUserId = userId;
		mOfferBannerAdShape = offerBannerAdShape;
		mHostInfo = hostInfo;
		mCurrencyName = currencyName;

		requestOfferBanner();
	}

	/**
	 * Generates the request URL and sends it on the background.
	 */
	private void requestOfferBanner() {

		String[] offerBannerUrlExtraKeys = new String[] { URL_PARAM_OFFERBANNER_KEY,
				UrlBuilder.URL_PARAM_ALLOW_CAMPAIGN_KEY, UrlBuilder.URL_PARAM_OFFSET_KEY,
				UrlBuilder.URL_PARAM_CURRENCY_NAME_KEY };
		String[] offerBannerUrlExtraValues = new String[] { UrlBuilder.URL_PARAM_VALUE_ON,
				UrlBuilder.URL_PARAM_VALUE_ON, String.valueOf(fetchPersistedBannerOffset()),
				mCurrencyName };

		if (SponsorPayPublisher.shouldUseStagingUrls()) {
			mBaseUrl = OFFERBANNER_STAGING_BASE_URL;
			mBaseDomain = OFFERBANNER_STAGING_DOMAIN;
		} else {
			mBaseUrl = OFFERBANNER_PRODUCTION_BASE_URL;
			mBaseDomain = OFFERBANNER_PRODUCTION_DOMAIN;
		}

		String offerBannerUrl = UrlBuilder.buildUrl(mBaseUrl, mUserId, mHostInfo,
				offerBannerUrlExtraKeys, offerBannerUrlExtraValues);

		Log.i(OfferBanner.LOG_TAG, "Offer Banner Request URL: " + offerBannerUrl);

		mAsyncRequest = new AsyncRequest(offerBannerUrl, this);
		mAsyncRequest.execute();
	}

	/**
	 * Called by the {@link #mAsyncRequest} instance when the background request's results are
	 * available. Will notify the registered {@link SPOfferBannerListener}.
	 */
	@Override
	public void onAsyncRequestComplete(AsyncRequest request) {
		Log.i(OfferBanner.LOG_TAG, "onAsyncRequestComplete, returned status code: "
				+ request.getHttpStatusCode());
		if (mAsyncRequest.hasSucessfulStatusCode()) {
			OfferBanner banner = new OfferBanner(mContext, mBaseDomain, mAsyncRequest
					.getResponseBody(), mAsyncRequest.getCookieStrings(), mOfferBannerAdShape);

			incrementPersistedBannerOffset();

			mListener.onSPOfferBannerAvailable(banner);
		} else if (mAsyncRequest.didRequestTriggerException()) {
			mListener.onSPOfferBannerRequestError(this);
		} else {
			mListener.onSPOfferBannerNotAvailable(this);
		}
	}

	/**
	 * Increments and persists the banner offset sent on every request to get different banners in a
	 * round-robin fashion.
	 */
	private void incrementPersistedBannerOffset() {
		SharedPreferences prefs = fetchSharedPreferences();

		int bannerOffset = fetchPersistedBannerOffset(prefs);
		bannerOffset++;

		prefs.edit().putInt(STATE_OFFSET_COUNT_KEY, bannerOffset).commit();
	}

	/**
	 * Returns the shared preferences file used by the Publisher functionality of the SponsorPay
	 * SDK.
	 */
	private SharedPreferences fetchSharedPreferences() {
		return mContext.getSharedPreferences(SponsorPayPublisher.PREFERENCES_FILENAME,
				Context.MODE_PRIVATE);
	}

	/**
	 * Fetches the persisted offset sent on the banner requests.
	 */
	private int fetchPersistedBannerOffset() {
		return fetchPersistedBannerOffset(fetchSharedPreferences());
	}

	/**
	 * Fetches the persisted offset sent on the banner requests.
	 * 
	 * @param prefs
	 *            The {@link SharedPreferences} instance which will be used to fetch this value.
	 * @return The persisted offset value.
	 */
	private int fetchPersistedBannerOffset(SharedPreferences prefs) {
		return prefs.getInt(STATE_OFFSET_COUNT_KEY, 0);
	}

	/**
	 * Gets the HTTP status code returned as a result of this request.
	 */
	public int getHttpStatusCode() {
		if (mAsyncRequest != null) {
			return mAsyncRequest.getHttpStatusCode();
		} else {
			return -1;
		}
	}

	/**
	 * Returns the local exception triggered when trying to send the request. An exception typically
	 * means that there was a problem connecting to the network, but checking the type of the
	 * returned exception can give a more accurate cause for the error.
	 */
	public Exception getRequestException() {
		if (mAsyncRequest != null) {
			return mAsyncRequest.getRequestTriggeredException();
		} else {
			return null;
		}
	}
}
