package com.sponsorpay.sdk.android.publisher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;

public class OfferBannerRequest implements AsyncRequest.ResultListener {

	private static final String OFFERBANNER_PRODUCTION_BASE_URL = "http://iframe.sponsorpay.com/mobile";
	private static final String OFFERBANNER_STAGING_BASE_URL = "http://staging.iframe.sponsorpay.com/mobile";

	private static final String OFFERBANNER_PRODUCTION_DOMAIN = "http://iframe.sponsorpay.com";
	private static final String OFFERBANNER_STAGING_DOMAIN = "http://staging.iframe.sponsorpay.com";

	private static final String URL_PARAM_OFFERBANNER_KEY = "banner";

	private static final String STATE_OFFSET_COUNT_KEY = "OFFERBANNER_AVAILABLE_RESPONSE_COUNT";

	private Context mContext;
	private SPOfferBannerListener mListener;
	private String mUserId;
	private OfferBanner.AdShape mOfferBannerAdShape;
	private HostInfo mHostInfo;

	private String mBaseUrl;
	private String mBaseDomain;

	private String mCurrencyName;

	private AsyncRequest mAsyncRequest;

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

	public void onAsyncRequestComplete(AsyncRequest request) {
		if (mAsyncRequest.hasSucessfulStatusCode()) {
			OfferBanner banner = new OfferBanner(mContext, mBaseDomain,
					mAsyncRequest.getResponseBody(), mAsyncRequest.getCookieStrings(),
					mOfferBannerAdShape);

			incrementPersistedBannerOffset();

			mListener.onSPOfferBannerAvailable(banner);
		} else if (mAsyncRequest.didRequestTriggerException()) {
			mListener.onSPOfferBannerRequestError(this);
		} else {
			mListener.onSPOfferBannerNotAvailable(this);
		}
	}

	private void incrementPersistedBannerOffset() {
		SharedPreferences prefs = fetchSharedPreferences();

		int bannerOffset = fetchPersistedBannerOffset(prefs);
		bannerOffset++;

		prefs.edit().putInt(STATE_OFFSET_COUNT_KEY, bannerOffset).commit();
	}

	private SharedPreferences fetchSharedPreferences() {
		return mContext.getSharedPreferences(SponsorPayPublisher.PREFERENCES_FILENAME,
				Context.MODE_PRIVATE);
	}

	private int fetchPersistedBannerOffset() {
		return fetchPersistedBannerOffset(fetchSharedPreferences());
	}

	private int fetchPersistedBannerOffset(SharedPreferences prefs) {
		return prefs.getInt(STATE_OFFSET_COUNT_KEY, 0);
	}

	public int getHttpStatusCode() {
		if (mAsyncRequest != null) {
			return mAsyncRequest.getHttpStatusCode();
		} else {
			return -1;
		}
	}

	public Exception getRequestException() {
		if (mAsyncRequest != null) {
			return mAsyncRequest.getRequestTriggeredException();
		} else {
			return null;
		}
	}
}
