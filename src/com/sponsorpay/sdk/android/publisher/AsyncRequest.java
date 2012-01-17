/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

import com.sponsorpay.sdk.android.HttpResponseParser;

/**
 * <p>
 * Requests and loads a resource using the HTTP GET method in the background. Will call the
 * {@link AsyncRequest.ResultListener} registered in the constructor in the same thread which
 * triggered the request / loading process. Uses the Android {@link AsyncTask} mechanism.
 * </p>
 */
public class AsyncRequest extends AsyncTask<Void, Void, Void> {

	public interface ResultListener {
		void onAsyncRequestComplete(AsyncRequest request);
	}

	public static boolean shouldLogVerbosely = false;

	public static String LOG_TAG = "AsyncRequest";

	/**
	 * Key of the User-Agent header sent on background requests.
	 */
	private static String USER_AGENT_HEADER_NAME = "User-Agent";

	/**
	 * Key of the Accept-Language header sent on background requests.
	 */
	private static String ACCEPT_LANGUAGE_HEADER_NAME = "Accept-Language";

	/**
	 * Value of the User-Agent header sent on background requests.
	 */
	private static String USER_AGENT_HEADER_VALUE = "Android";

	/**
	 * URL for the request that will be performed in the background.
	 */
	private String mRequestUrl;

	/**
	 * Status code of the server's response.
	 */
	private int mStatusCode;

	/**
	 * Server's response body.
	 */
	private String mResponseBody;

	/**
	 * Cookies returned by the server.
	 */
	private String[] mCookieStrings;

	/**
	 * Registered {@link ResultListener} to be notified of the request's results when they become
	 * available.
	 */
	private ResultListener mResultListener;

	/**
	 * Stores an exception triggered when launching the request, usually caused by network
	 * connectivity problem.
	 */
	private Exception mRequestException;

	/**
	 * 
	 * @param requestUrl
	 *            URL to send the backgorund request to.
	 * @param listener
	 *            {@link ResultListener} to be notified of the request's results when they become
	 *            available.
	 */
	public AsyncRequest(String requestUrl, ResultListener listener) {
		mRequestUrl = requestUrl;
		mResultListener = listener;
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

		String acceptLanguageHeaderValue = makeAcceptLanguageHeaderValue();
		if (shouldLogVerbosely)
			Log.i(getClass().getSimpleName(), "acceptLanguageHeaderValue: "
					+ acceptLanguageHeaderValue);
		request.addHeader(ACCEPT_LANGUAGE_HEADER_NAME, acceptLanguageHeaderValue);

		HttpClient client = new DefaultHttpClient();

		mRequestException = null;

		try {
			HttpResponse response = client.execute(request);
			mStatusCode = response.getStatusLine().getStatusCode();
			mResponseBody = HttpResponseParser.extractResponseString(response);

			Header[] cookieHeaders = response.getHeaders("Set-Cookie");

			// Populate result cookies with values of cookieHeaders
			if (cookieHeaders.length > 0) {
				
				if (shouldLogVerbosely)
					Log.v(LOG_TAG, String.format("Got following cookies from server (url: %s):",
							mRequestUrl));
				
				mCookieStrings = new String[cookieHeaders.length];
				for (int i = 0; i < cookieHeaders.length; i++) {
					mCookieStrings[i] = cookieHeaders[i].getValue();
					if (shouldLogVerbosely)
						Log.v(LOG_TAG, mCookieStrings[i]);
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception triggered when executing request: " + e);
			mRequestException = e;
		}
		return null;
	}

	/**
	 * Returns a value for the HTTP Accept-Language header based on the current locale set up for
	 * the device.
	 */
	private String makeAcceptLanguageHeaderValue() {
		String preferredLanguage = Locale.getDefault().getLanguage();

		String acceptLanguageLocaleValue = preferredLanguage;
		final String englishLanguageCode = Locale.ENGLISH.getLanguage();

		if (preferredLanguage == null || preferredLanguage.equals("")) {
			acceptLanguageLocaleValue = englishLanguageCode;
		} else if (!englishLanguageCode.equals(preferredLanguage)) {
			acceptLanguageLocaleValue += String.format(", %s;q=0.8", englishLanguageCode);
		}
		return acceptLanguageLocaleValue;
	}

	/**
	 * Called in the original thread when a response from the server is available. Notifies the
	 * request result listener.
	 * 
	 * @param result
	 */
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		mResultListener.onAsyncRequestComplete(this);
	}

	/**
	 * Gets the cookie strings returned by the server.
	 */
	public String[] getCookieStrings() {
		return mCookieStrings;
	}

	/**
	 * Gets the response body returned by the server.
	 */
	public String getResponseBody() {
		return mResponseBody;
	}

	/**
	 * Gets the returned HTTP status code.
	 */
	public int getHttpStatusCode() {
		return mStatusCode;
	}

	/**
	 * Returns whether a local exception was triggered when trying to send the request. An exception
	 * typically means that there was a problem connecting to the network, but checking the type of
	 * the exception returned by {@link #getRequestTriggeredException()} is recommended.
	 */
	public boolean didRequestTriggerException() {
		return (mRequestException != null);
	}

	/**
	 * Returns the local exception triggered when trying to send the request. An exception typically
	 * means that there was a problem connecting to the network, but checking the type of the
	 * returned exception can give a more accurate cause for the error.
	 */
	public Exception getRequestTriggeredException() {
		return mRequestException;
	}

	/**
	 * Returns whether a successful HTTP status code was returned.
	 */
	public boolean hasSucessfulStatusCode() {
		// "OK" and "Redirect" are considered successful
		return mStatusCode >= 200 && mStatusCode < 400;
	}
}
