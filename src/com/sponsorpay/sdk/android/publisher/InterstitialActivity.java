/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * <p>
 * Shows the SponsorPay mobile interstitial embedded in a WebView.
 * </p>
 * <p>
 * Will retrieve the interstitial's initial html content from the {@link #EXTRA_INITIAL_CONTENT_KEY} encoded into the
 * calling intent, and will load dependent content referenced with relative links using the base URL encoded as
 * {@link #EXTRA_BASE_DOMAIN_KEY} into the calling intent. The http cookie(s) to use will be retrieved from the String
 * array encoded as extra {@link #EXTRA_COOKIESTRINGS_KEY}
 * </p>
 * <p>
 * The boolean value encoded as {@link #EXTRA_SHOULD_STAY_OPEN_KEY} into the calling intent will determine the
 * activity's behavior when the user is redirected outside the application. The default behavior is to close the
 * interstitial.
 * </p>
 */
public class InterstitialActivity extends Activity {
	// private static final String LOG_TAG = "InterstitialActivity";
	public static final String EXTRA_SHOULD_STAY_OPEN_KEY = "EXTRA_SHOULD_REMAIN_OPEN_KEY";
	public static final String EXTRA_INITIAL_CONTENT_KEY = "EXTRA_INITIAL_CONTENT_KEY";

	public static final String EXTRA_BASE_DOMAIN_KEY = "EXTRA_INITIAL_BASE_URL";
	public static final String EXTRA_COOKIESTRINGS_KEY = "EXTRA_COOKIESTRINGS";

	/**
	 * The result code that is returned when the Offer Wall's parsed exit scheme does not contain a status code.
	 */
	public static final int RESULT_CODE_NO_STATUS_CODE = -10;

	private WebView mWebView;
	private boolean mShouldStayOpen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		String initialInterstitialContent = getIntent().getStringExtra(EXTRA_INITIAL_CONTENT_KEY);
		String baseDomain = getIntent().getStringExtra(EXTRA_BASE_DOMAIN_KEY);
		String[] cookieStrings = getIntent().getStringArrayExtra(EXTRA_COOKIESTRINGS_KEY);
		mShouldStayOpen = getIntent().getBooleanExtra(EXTRA_SHOULD_STAY_OPEN_KEY, mShouldStayOpen);
		mWebView = new WebView(InterstitialActivity.this);

		// Add into the CookieManager used by the web view the cookies received as encoded extras.
		if (cookieStrings.length > 0) {
			for (String cookieString : cookieStrings) {
				getCookieManagerInstance().setCookie(baseDomain, cookieString);
			}
		}

		setContentView(mWebView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d(InterstitialActivity.class.getSimpleName(), "shouldOverrideUrlLoading: " + url);
				
				if (url.startsWith("sponsorpay://exit")) { // ?status=<statusCode>&url=<url>)url is optional)

					int resultCode = parseURLForStatusCodeViaUri(url);
					setResult(resultCode);

					/*
					 * Checking scheme if URL has been provided! If yes, try calling an app that will respond to the
					 * given Uri (most likely, the Market App).
					 */
					String marketUrl = parseURLForProvidedURL(url);
					Log.d(InterstitialActivity.class.getSimpleName(), "Provided (market) url is: " + marketUrl);
					if (marketUrl != null) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(marketUrl));
						startActivity(intent);
						// TODO: handle activity not found case (throws an ActivityNotFoundException)
						if (!mShouldStayOpen) {
							finish();
						}
					} else {
						finish();
					}

					return true;
				} else {
					return false;
				}
			}
		});

		mWebView.loadDataWithBaseURL(baseDomain, initialInterstitialContent, "text/html", "utf-8", null);
	}

	private CookieManager getCookieManagerInstance() {
		CookieManager instance;
		
		// CookieSyncManager.createInstance() has to be called before we get CookieManager's instance.
		try {
			CookieSyncManager.getInstance();
		} catch (IllegalStateException e) {
			CookieSyncManager.createInstance(InterstitialActivity.this);
		}

		instance = CookieManager.getInstance();

		return instance;
	}

	/**
	 * Extract the provided URL from the exit scheme
	 * 
	 * @param url
	 *            the exit scheme url to parse
	 * @return the extracted, provided & decoded URL
	 */
	private String parseURLForProvidedURL(String url) {
		Uri uri = Uri.parse(url);
		if (uri.getQueryParameter("url") != null) {
			return Uri.decode(uri.getQueryParameter("url"));
		}
		return null;
	}

	/**
	 * Extract the status code from the scheme
	 * 
	 * @param url
	 *            the url to parsed for the status code
	 * @return the status code
	 */
	private int parseURLForStatusCodeViaUri(String url) {
		Uri uri = Uri.parse(url);

		if (uri.getQueryParameter("status") != null) {
			return (Integer.parseInt(uri.getQueryParameter("status")));
		}
		return RESULT_CODE_NO_STATUS_CODE;
	}
}
