package com.sponsorpay.sdk.android.publisher;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.HttpResponseParser;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.currency.VirtualCurrencyConnector;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class OfferBannerRequest {

	private static String USER_AGENT_HEADER_NAME = "User-Agent";
	private static String USER_AGENT_HEADER_VALUE = "Android";

	private class AsyncRequestTask extends AsyncTask<Void, Void, Void> {
		/**
		 * URL for the request that will be performed in the background.
		 */
		private String mRequestUrl;

		/**
		 * Status code of the server's response.
		 */
		public int statusCode;

		/**
		 * Server's response body.
		 */
		public String responseBody;

		/**
		 * Cookies returned by the server.
		 */
		public String[] cookieStrings;

		/**
		 * Stores an exception triggered when launching the request, usually caused by network
		 * connectivity problem.
		 */
		public Exception requestException;

		public AsyncRequestTask(String requestUrl) {
			mRequestUrl = requestUrl;
		}

		/**
		 * Performs the request in the background. Called by the parent {@link AsyncTask} when
		 * {@link #execute(Void...)} is invoked.
		 * 
		 * @param
		 * @return
		 */
		@Override
		protected Void doInBackground(Void... params) {
			HttpUriRequest request = new HttpGet(mRequestUrl);
			request.addHeader(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_VALUE);
			HttpClient client = new DefaultHttpClient();

			requestException = null;

			try {
				HttpResponse response = client.execute(request);
				statusCode = response.getStatusLine().getStatusCode();
				responseBody = HttpResponseParser.extractResponseString(response);

				Header[] cookieHeaders = response.getHeaders("Set-Cookie");

				// Populate result cookies with values of cookieHeaders
				if (cookieHeaders.length > 0) {
					cookieStrings = new String[cookieHeaders.length];
					for (int i = 0; i < cookieHeaders.length; i++) {
						cookieStrings[i] = cookieHeaders[i].getValue();
					}
				}
			} catch (Exception e) {
				Log.e(OfferBanner.LOG_TAG, "Exception triggered when executing request: " + e);
				requestException = e;
			}
			return null;
		}

		/**
		 * Called in the original thread when a response from the server is available. Notifies the
		 * host {@link VirtualCurrencyConnector}.
		 * 
		 * @param result
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			OfferBannerRequest.this.onAsyncRequestTaskComplete();
		}
	}

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

	private AsyncRequestTask mAsyncRequestTask;

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

		mAsyncRequestTask = new AsyncRequestTask(offerBannerUrl);
		mAsyncRequestTask.execute();
	}

	private void onAsyncRequestTaskComplete() {
		if (isOfferBannerAvailableAccordingToStatusCode(mAsyncRequestTask.statusCode)) {
			OfferBanner banner = new OfferBanner(mContext, mBaseDomain,
					mAsyncRequestTask.responseBody, mAsyncRequestTask.cookieStrings,
					mOfferBannerAdShape);

			incrementPersistedBannerOffset();

			mListener.onSPOfferBannerAvailable(banner);
		} else if (mAsyncRequestTask.requestException != null) {
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
		if (mAsyncRequestTask != null) {
			return mAsyncRequestTask.statusCode;
		} else {
			return -1;
		}
	}

	public Exception getRequestException() {
		if (mAsyncRequestTask != null) {
			return mAsyncRequestTask.requestException;
		} else {
			return null;
		}
	}

	/**
	 * Takes an HTTP status code and returns whether it means that a banner ad is available.
	 * 
	 * @param statusCode
	 *            The HTTP status code as int.
	 * @return True for offer banner available, false otherwise.
	 */
	public static boolean isOfferBannerAvailableAccordingToStatusCode(int statusCode) {
		// "OK" and "Redirect" codes mean we've got a banner
		return statusCode >= 200 && statusCode < 400;
	}
}
