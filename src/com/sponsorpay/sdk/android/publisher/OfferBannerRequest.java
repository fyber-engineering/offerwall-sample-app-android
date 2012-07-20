/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.OfferBanner.AdShape;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * Requests a new offer banner to the SponsorPay server and notifies the registered
 * {@link SPOfferBannerListener} of the result of the request.
 */
public class OfferBannerRequest implements AsyncRequest.AsyncRequestResultListener {

	private static final String OFFERBANNER_PRODUCTION_BASE_URL = "https://iframe.sponsorpay.com/mobile";
	private static final String OFFERBANNER_STAGING_BASE_URL = "https://staging-iframe.sponsorpay.com/mobile";

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
	private UserId mUserId;

	/**
	 * {@link AdShape} whose description will be sent to the server in the request.
	 */
	private OfferBanner.AdShape mOfferBannerAdShape;

	/**
	 * {@link HostInfo} instance containing the Application ID and data about the device.
	 */
	private HostInfo mHostInfo;

	/**
	 * Currency Name to be sent in the request.
	 */
	private String mCurrencyName;

	/**
	 * Map of custom key/values to add to the parameters on the OfferBanner request URL.
	 */
	private Map<String, String> mCustomParams;

	private String mOverridingUrl;

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
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public OfferBannerRequest(Context context, String userId, HostInfo hostInfo,
			SPOfferBannerListener listener, OfferBanner.AdShape offerBannerAdShape,
			String currencyName, Map<String, String> customParams) {

		mContext = context;
		mListener = listener;
		mUserId = UserId.make(context, userId);
		mOfferBannerAdShape = offerBannerAdShape;
		mHostInfo = hostInfo;
		mCurrencyName = currencyName;
		mCustomParams = customParams;
	}

	public void setOverridingUrl(String overridingUrl) {
		mOverridingUrl = overridingUrl;
	}

	/**
	 * Generates the request URL and loads it on the background.
	 */
	public void requestOfferBanner() {

		String offerBannerUrl = determineUrl();

		SponsorPayLogger.i(OfferBanner.LOG_TAG, "Offer Banner Request URL: " + offerBannerUrl);

		mAsyncRequest = new AsyncRequest(offerBannerUrl, this);
		mAsyncRequest.execute();
	}

	private String buildUrl() {
		String[] offerBannerUrlExtraKeys = new String[] { URL_PARAM_OFFERBANNER_KEY,
				UrlBuilder.URL_PARAM_ALLOW_CAMPAIGN_KEY, UrlBuilder.URL_PARAM_OFFSET_KEY };
		String[] offerBannerUrlExtraValues = new String[] { UrlBuilder.URL_PARAM_VALUE_ON,
				UrlBuilder.URL_PARAM_VALUE_ON, String.valueOf(fetchPersistedBannerOffset()) };

		Map<String, String> extraKeysValues = UrlBuilder.mapKeysToValues(offerBannerUrlExtraKeys,
				offerBannerUrlExtraValues);

		if (mCurrencyName != null && !"".equals(mCurrencyName)) {
			extraKeysValues.put(UrlBuilder.URL_PARAM_CURRENCY_NAME_KEY, mCurrencyName);
		}

		if (mCustomParams != null) {
			extraKeysValues.putAll(mCustomParams);
		}

		String baseUrl;

		if (SponsorPayPublisher.shouldUseStagingUrls()) {
			baseUrl = OFFERBANNER_STAGING_BASE_URL;
		} else {
			baseUrl = OFFERBANNER_PRODUCTION_BASE_URL;
		}

		return UrlBuilder.newBuilder(baseUrl, mHostInfo).setUserId(mUserId.toString())
				.addExtraKeysValues(extraKeysValues).addScreenMetrics().buildUrl();
	}

	private String determineUrl() {
		if (StringUtils.notNullNorEmpty(mOverridingUrl)) {
			return mOverridingUrl;
		} else {
			return buildUrl();
		}
	}

	/**
	 * Called by the {@link #mAsyncRequest} instance when the background request's results are
	 * available. Will notify the registered {@link SPOfferBannerListener}.
	 */
	@Override
	public void onAsyncRequestComplete(AsyncRequest request) {
		SponsorPayLogger.i(OfferBanner.LOG_TAG, "onAsyncRequestComplete, returned status code: "
				+ request.getHttpStatusCode());
		if (mAsyncRequest.hasSucessfulStatusCode()) {
			OfferBanner banner = new OfferBanner(mContext, mAsyncRequest.getRequestUrl(),
					mAsyncRequest.getResponseBody(), mAsyncRequest.getCookieStrings(),
					mOfferBannerAdShape);

			incrementPersistedBannerOffset();

			mListener.onSPOfferBannerAvailable(banner);
		} else if (mAsyncRequest.didRequestThrowError()) {
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
	 * Returns the local error thrown when trying to send the request. An exception typically means
	 * that there was a problem connecting to the network, but checking the type of the returned
	 * error can give a more accurate cause for the error.
	 * 
	 * @deprecated Use getRequestThrownError() instead, which returns a Throwable instance or null.
	 *             This method will be removed in a future release.
	 */
	public Exception getRequestException() {
		Exception retval = null;

		if (mAsyncRequest != null) {
			Throwable requestError = mAsyncRequest.getRequestThrownError();
			if (requestError != null && Exception.class.isAssignableFrom(requestError.getClass()))
				retval = (Exception) requestError;
		}

		return retval;
	}

	/**
	 * Returns the local error thrown when trying to send the request. An exception typically means
	 * that there was a problem connecting to the network, but checking the type of the returned
	 * error can give a more accurate cause for the error.
	 */
	public Throwable getRequestThrownError() {
		if (mAsyncRequest != null)
			return mAsyncRequest.getRequestThrownError();
		else
			return null;
	}
}
