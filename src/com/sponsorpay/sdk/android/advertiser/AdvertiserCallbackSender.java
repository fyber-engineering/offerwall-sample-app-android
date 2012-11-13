/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.AsyncTask;

import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.utils.SPHttpClient;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * Runs in the background the Advertiser Callback HTTP request.
 */
public class AdvertiserCallbackSender extends AsyncTask<String, Void, Boolean> {

	/**
	 * HTTP status code that the response should have in order to determine that the API has been
	 * contacted successfully.
	 */
	private static final int SUCCESFUL_HTTP_STATUS_CODE = 200;

	/**
	 * The API resource URL to contact when talking to the SponsorPay Advertiser API
	 */
	private static final String API_PRODUCTION_RESOURCE_URL = "https://service.sponsorpay.com/installs/v2";
	private static final String API_STAGING_RESOURCE_URL = "https://staging.sws.sponsorpay.com/installs/v2";

	/**
	 * The key for encoding the parameter corresponding to whether a previous invocation of the
	 * advertiser callback had received a successful response.
	 */
	private static final String SUCCESSFUL_ANSWER_RECEIVED_KEY = "answer_received";

	/**
	 * SubID URL parameter key
	 */
	private static final String INSTALL_SUBID_KEY = "subid";

	/**
	 * The HTTP request that will be executed to contact the API with the callback request
	 */
	private HttpUriRequest mHttpRequest;

	/**
	 * The response returned by the SponsorPay API
	 */
	private HttpResponse mHttpResponse;

	/**
	 * The HTTP client employed to call the Sponsorpay API
	 */
	private HttpClient mHttpClient;

	/**
	 * True if the advertiser callback was sent and received a successful response in a previous
	 * invocation.
	 */
	private boolean mWasAlreadySuccessful = false;

	/**
	 * Install subID to report on the advertiser callback
	 */
	private String mInstallSubId;

	/**
	 * Map of custom parameters to be sent in the callback request.
	 */
	private Map<String, String> mCustomParams;

	/**
	 * Interface to be implemented by parties interested in the response from the SponsorPay server
	 * for the advertiser callback.
	 */
	public interface APIResultListener {

		/**
		 * Invoked when we receive a response for the advertiser callback request.
		 * 
		 * @param wasSuccessful
		 *            true if the request was successful, false otherwise.
		 */
		void onAPIResponse(boolean wasSuccessful);
	}

	/**
	 * Registered listener for the result of the advertiser callback request.
	 */
	private APIResultListener mListener;

	private SPCredentials mCredentials;

	/**
	 * <p>
	 * Constructor. Sets the request callback listener and stores the host information.
	 * </p>
	 * See {@link AdvertiserHostInfo} and {@link APIResultListener}.
	 * 
	 * @param credentials
	 *            the credentials used for this callback
	 * @param listener
	 *            the callback listener
	 */
	public AdvertiserCallbackSender(SPCredentials credentials, APIResultListener listener) {
		mListener = listener;
		mCredentials = credentials;
	}

	/**
	 * Sets the map of custom parameters to be sent in the callback request.
	 */
	public void setCustomParams(Map<String, String> customParams) {
		mCustomParams = customParams;
	}

	/**
	 * Sets the install subID to report on the advertiser callback.
	 * 
	 * @param subIdValue
	 *            The install subID received on the app instalation.
	 */
	public void setInstallSubId(String subIdValue) {
		SponsorPayLogger.d(AdvertiserCallbackSender.class.getSimpleName(), "SubID value set to " + subIdValue);
		mInstallSubId = subIdValue;
	}

	/**
	 * Sets whether a previous invocation of the advertiser callback had received a successful
	 * response.
	 */
	public void setWasAlreadySuccessful(boolean value) {
		mWasAlreadySuccessful = value;
	}

	/**
	 * Triggers the callback request that contacts the Sponsorpay Advertiser API. If and when a
	 * successful response is received from the server, the {@link APIResultListener} registered
	 * through the constructor {@link #AdvertiserCallbackSender(SPCredentials, APIResultListener)} will
	 * be notified.
	 */
	public void trigger() {
		// The final URL with parameters is built right away, to make sure that possible runtime
		// exceptions triggered from the SDK to the integrator's code --due to a missing App ID
		// value or to an invalid collection of custom parameters-- are triggered on the calling
		// thread.
		execute(buildUrl());
	}

	private String buildUrl() {
		// Prepare HTTP request by URL-encoding the device information
		String baseUrl = SponsorPayAdvertiser.shouldUseStagingUrls() ? API_STAGING_RESOURCE_URL
				: API_PRODUCTION_RESOURCE_URL;

		Map<String, String> extraParams = UrlBuilder.mapKeysToValues(
				new String[] { SUCCESSFUL_ANSWER_RECEIVED_KEY },
				new String[] { mWasAlreadySuccessful ? "1" : "0" });

		if (StringUtils.notNullNorEmpty(mInstallSubId)) {
			extraParams.put(INSTALL_SUBID_KEY, mInstallSubId);
		}

		if (mCustomParams != null) {
			extraParams.putAll(mCustomParams);
		}

		String callbackUrl = UrlBuilder.newBuilder(baseUrl, mCredentials).addExtraKeysValues(
				extraParams).sendUserId(false).addScreenMetrics().buildUrl();

		SponsorPayLogger.d(AdvertiserCallbackSender.class.getSimpleName(),
				"Advertiser callback will be sent to: " + callbackUrl);

		return callbackUrl;
	}

	/**
	 * <p>
	 * Method overridden from {@link AsyncTask}. Executed on a background thread, runs the API
	 * contact request.
	 * </p>
	 * <p>
	 * Encodes the host information in the request URL, runs the request, waits for the response,
	 * parses its status code and lets the UI thread receive the result and notify the registered
	 * {@link APIResultListener}.
	 * <p/>
	 * 
	 * @param params
	 *            Only one parameter of type {@link String} containing the request URL is expected.
	 * @return True for a successful request, false otherwise. This value will be communicated to
	 *         the UI thread by the Android {@link AsyncTask} implementation.
	 */
	@Override
	protected Boolean doInBackground(String... params) {
		Boolean returnValue = null;

		String callbackUrl = params[0];

		mHttpRequest = new HttpGet(callbackUrl);
		mHttpClient = SPHttpClient.getHttpClient();

		try {
			mHttpResponse = mHttpClient.execute(mHttpRequest);

			// We're not parsing the response, just making sure that a successful status code has
			// been received.
			int responseStatusCode = mHttpResponse.getStatusLine().getStatusCode();
			
			mHttpResponse.getEntity().consumeContent();
			
			if (responseStatusCode == SUCCESFUL_HTTP_STATUS_CODE) {
				returnValue = true;
			} else {
				returnValue = false;
			}

			SponsorPayLogger.d(AdvertiserCallbackSender.class.getSimpleName(), "Server returned status code: "
					+ responseStatusCode);
		} catch (Exception e) {
			returnValue = false;
			SponsorPayLogger.e(AdvertiserCallbackSender.class.getSimpleName(),
					"An exception occurred when trying to send advertiser callback: " + e);
		}
		return returnValue;
	}

	/**
	 * This method is called by the Android {@link AsyncTask} implementation in the UI thread (or
	 * the thread which invoked {@link #trigger()}) when
	 * {@link #doInBackground(AdvertiserHostInfo...)} returns. It will invoke the registered
	 * {@link APIResultListener}
	 * 
	 * @param requestWasSuccessful
	 *            true if the response has a successful status code (equal to
	 *            {@link #SUCCESFUL_HTTP_STATUS_CODE}). false otherwise.
	 */
	@Override
	protected void onPostExecute(Boolean requestWasSuccessful) {
		super.onPostExecute(requestWasSuccessful);

		if (mListener != null) {
			mListener.onAPIResponse(requestWasSuccessful);
		}
	}
}
