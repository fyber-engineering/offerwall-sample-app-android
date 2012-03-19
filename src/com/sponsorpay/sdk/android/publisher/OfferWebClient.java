/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * {@link WebViewClient} implementing common functionality for {@link WebView} instances displaying
 * SponsorPay offers.  
 *
 */
public abstract class OfferWebClient extends WebViewClient {
	public static final String LOG_TAG = "OfferWebClient";
	
	private static final String SPONSORPAY_EXIT_SCHEMA = "sponsorpay://exit";
	private static final String EXIT_URL_TARGET_URL_PARAM_KEY = "url";
	private static final String EXIT_URL_RESULT_CODE_PARAM_KEY = "status";
	
	/**
	 * The result code that is returned when the parsed exit scheme does not contain a status code.
	 */
	public static final int RESULT_CODE_NO_STATUS_CODE = -10;

	/**
	 * Extracts the provided URL from the exit scheme
	 * 
	 * @param url
	 *            the exit scheme url to parse
	 * @return the extracted, provided & decoded URL
	 */
	protected static String parseSponsorPayExitUrlForTargetUrl(String url) {
		Uri uri = Uri.parse(url);

		String targetUrl = uri.getQueryParameter(EXIT_URL_TARGET_URL_PARAM_KEY);

		if (targetUrl != null) {
			return Uri.decode(targetUrl);
		}
		return null;
	}

	/**
	 * Extracts the status code from the scheme
	 * 
	 * @param url
	 *            the url to parsed for the status code
	 * @return the status code
	 */
	protected static int parseSponsorPayExitUrlForResultCode(String url) {
		Uri uri = Uri.parse(url);

		String resultCodeAsString = uri.getQueryParameter(EXIT_URL_RESULT_CODE_PARAM_KEY);

		if (resultCodeAsString != null) {
			return (Integer.parseInt(resultCodeAsString));
		}
		return RESULT_CODE_NO_STATUS_CODE;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.i(LOG_TAG, "shouldOverrideUrlLoading called with url: " + url);

		if (url.startsWith(SPONSORPAY_EXIT_SCHEMA)) {
			
			// ?status=<statusCode>&url=<url>)url is optional)
			int resultCode = parseSponsorPayExitUrlForResultCode(url);
			String targetUrl = parseSponsorPayExitUrlForTargetUrl(url);

			Log.i(LOG_TAG, "Overriding. Target Url: " + targetUrl);
			
			onSponsorPayExitScheme(resultCode, targetUrl);

			return true;
		} else {
			Log.i(LOG_TAG, "Not overriding");
			return false;
		}
	}

	protected boolean launchActivityWithUrl(Activity launcherActivity, String url) {
		if (url == null)
			return false;
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		launcherActivity.startActivity(intent);
		// TODO: handle activity not found case (throws an ActivityNotFoundException)
		return true;
	}

	protected abstract void onSponsorPayExitScheme(int resultCode, String targetUrl);
}
