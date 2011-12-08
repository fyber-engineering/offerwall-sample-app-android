package com.sponsorpay.sdk.android.publisher;

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
 * {@link AsyncRequest.ResultListener} registered in the constructor in the
 * same thread which triggered the request / loading process. Uses the Android {@link AsyncTask}
 * mechanism.
 * </p>
 */
public class AsyncRequest extends AsyncTask<Void, Void, Void> {
	public interface ResultListener {
		void onAsyncRequestComplete(AsyncRequest request);
	}

	public static String LOG_TAG = "AsyncRequest";
	
	private static String USER_AGENT_HEADER_NAME = "User-Agent";
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

	private ResultListener mResultListener;

	/**
	 * Stores an exception triggered when launching the request, usually caused by network
	 * connectivity problem.
	 */
	private Exception mRequestException;

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
		HttpClient client = new DefaultHttpClient();

		mRequestException = null;

		try {
			HttpResponse response = client.execute(request);
			mStatusCode = response.getStatusLine().getStatusCode();
			mResponseBody = HttpResponseParser.extractResponseString(response);

			Header[] cookieHeaders = response.getHeaders("Set-Cookie");

			// Populate result cookies with values of cookieHeaders
			if (cookieHeaders.length > 0) {
				Log.v(LOG_TAG, "Got the following cookies from server (url " + mRequestUrl + "):");
				mCookieStrings = new String[cookieHeaders.length];
				for (int i = 0; i < cookieHeaders.length; i++) {
					mCookieStrings[i] = cookieHeaders[i].getValue();
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

	public String[] getCookieStrings() {
		return mCookieStrings;
	}

	public String getResponseBody() {
		return mResponseBody;
	}

	public int getHttpStatusCode() {
		return mStatusCode;
	}

	public boolean didRequestTriggerException() {
		return (mRequestException != null);
	}

	public Exception getRequestTriggeredException() {
		return mRequestException;
	}

	public boolean hasSucessfulStatusCode() {
		// "OK" and "Redirect" are considered successful
		return mStatusCode >= 200 && mStatusCode < 400;
	}
}
