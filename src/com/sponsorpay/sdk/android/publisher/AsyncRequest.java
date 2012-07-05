/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.os.AsyncTask;
import android.util.Log;

import com.sponsorpay.sdk.android.HttpResponseParser;

/**
 * <p>
 * Requests and loads a resource using the HTTP GET method in the background. Will call the
 * {@link AsyncRequest.AsyncRequestResultListener} registered in the constructor in the same thread
 * which triggered the request / loading process. Uses the Android {@link AsyncTask} mechanism.
 * </p>
 */
public class AsyncRequest extends AsyncTask<Void, Void, Void> {

	public interface AsyncRequestResultListener {
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
	 * Custom SponsorPay HTTP header containing the signature of the response.
	 */
	private static final String SIGNATURE_HEADER = "X-Sponsorpay-Response-Signature";

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
	 * Server's response signature, extracted of the {@value #SIGNATURE_HEADER} header.
	 */
	private String mResponseSignature;

	/**
	 * Cookies returned by the server.
	 */
	private String[] mCookieStrings;

	/**
	 * Registered {@link AsyncRequestResultListener} to be notified of the request's results when
	 * they become available.
	 */
	private AsyncRequestResultListener mResultListener;

	/**
	 * Stores an error thrown when launching the request, usually caused by a network connectivity
	 * problem.
	 */
	private Throwable mThrownRequestError;

	/**
	 * 
	 * @param requestUrl
	 *            URL to send the backgorund request to.
	 * @param listener
	 *            {@link AsyncRequestResultListener} to be notified of the request's results when
	 *            they become available.
	 */
	public AsyncRequest(String requestUrl, AsyncRequestResultListener listener) {
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
		if (shouldLogVerbosely) {
			Log.i(getClass().getSimpleName(), "acceptLanguageHeaderValue: "
					+ acceptLanguageHeaderValue);
		}
		
		request.addHeader(ACCEPT_LANGUAGE_HEADER_NAME, acceptLanguageHeaderValue);

//		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
//
//		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
//		socketFactory
//				.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
//
//		// Set verifier
//		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//		
//		SchemeRegistry schemeRegistry = new SchemeRegistry();
//		schemeRegistry.register(new Scheme("https", 
//		            socketFactory, 443));
////		SSLSocketFactory.getSocketFactory(), 443));
//
//		HttpParams httpParams = new BasicHttpParams();
//
//		SingleClientConnManager mgr = new SingleClientConnManager(httpParams, schemeRegistry);
		

//		HttpClient client = new DefaultHttpClient(mgr, httpParams);
		
		
		
		
		HttpClient client = new MyHttpClient();

		
		
		mThrownRequestError = null;

		try {
			HttpResponse response = client.execute(request);
			mStatusCode = response.getStatusLine().getStatusCode();
			mResponseBody = HttpResponseParser.extractResponseString(response);
			Header[] responseSignatureHeaders = response.getHeaders(SIGNATURE_HEADER);
			mResponseSignature = responseSignatureHeaders.length > 0 ? responseSignatureHeaders[0]
					.getValue() : "";
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
		} catch (Throwable t) {
			Log.e(LOG_TAG, "Exception triggered when executing request: " + t);
			mThrownRequestError = t;
		}
		return null;
	}
	
	public class MyHttpClient extends DefaultHttpClient {
		 
	    @Override
	    protected ClientConnectionManager createClientConnectionManager() {
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        // Register for port 443 our SSLSocketFactory with our keystore
	        // to the ConnectionManager
	        registry.register(new Scheme("https", newSslSocketFactory(), 443));
	        return new SingleClientConnManager(getParams(), registry);
	    }
	 
	    private SSLSocketFactory newSslSocketFactory() {
	        try {
	            // Get an instance of the Bouncy Castle KeyStore format
	            KeyStore trusted = KeyStore.getInstance("BKS");
	            // Get the raw resource, which contains the keystore with
	            // your trusted certificates (root and any intermediate certs)
	            InputStream in = getClass().getResourceAsStream("/mystore.bks");
	            try {
	                // Initialize the keystore with the provided trusted certificates
	                // Also provide the password of the keystore
	                trusted.load(in, "SPVerySecretPasswd".toCharArray());
	            } finally {
	                in.close();
	            }
	            // Pass the keystore to the SSLSocketFactory. The factory is responsible
	            // for the verification of the server certificate.
	            SSLSocketFactory sf = new SSLSocketFactory(trusted);
	            // Hostname verification from certificate
	            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
	            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
	            return sf;
	        } catch (Exception e) {
	            throw new AssertionError(e);
	        }
	   
	    }
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

	public boolean hasCookies() {
		Boolean retval;
		
		if (mCookieStrings == null || mCookieStrings.length == 0) {
			retval = false;
		} else {
			String firstCookieString = mCookieStrings[0];
			if (firstCookieString == null || "".equals(firstCookieString)) {
				retval = false;
			} else {
				retval = true;
			}
		}
		
		return retval;
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

	public String getResponseSignature() {
		return mResponseSignature;
	}

	public String getRequestUrl() {
		return mRequestUrl;
	}
	
	/**
	 * Returns the local error thrown when trying to send the request. An exception typically means
	 * that there was a problem connecting to the network, but checking the type of the returned
	 * error can give a more accurate cause for the error.
	 */
	public boolean didRequestThrowError() {
		return (mThrownRequestError != null);
	}

	/**
	 * Returns the local error thrown when trying to send the request. An exception typically means
	 * that there was a problem connecting to the network, but checking the type of the returned
	 * error can give a more accurate cause for the error.
	 */
	public Throwable getRequestThrownError() {
		return mThrownRequestError;
	}

	/**
	 * Returns whether a successful HTTP status code was returned.
	 */
	public boolean hasSucessfulStatusCode() {
		// "OK" and "Redirect" are considered successful
		return mStatusCode >= 200 && mStatusCode < 400;
	}
}
