/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.view.WindowManager.BadTokenException;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.SponsorPayPublisher.UIStringIdentifier;

/**
 * {@link WebViewClient} implementing common functionality for {@link WebView} instances displaying
 * SponsorPay offers.  
 *
 */
public abstract class SPWebClient extends WebViewClient {
	public static final String LOG_TAG = "SPWebClient";
	
	private static final String SPONSORPAY_SCHEMA = "sponsorpay://";
	private static final String SPONSORPAY_EXIT_SCHEMA = "exit";
	private static final String EXIT_URL_TARGET_URL_PARAM_KEY = "url";
	private static final String EXIT_URL_RESULT_CODE_PARAM_KEY = "status";
	
	/**
	 * The result code that is returned when the parsed exit scheme does not contain a status code.
	 */
	public static final int RESULT_CODE_NO_STATUS_CODE = -10;

	private WeakReference<Activity>  mHostActivityRef;
	
	public SPWebClient(Activity hostActivity) {
		mHostActivityRef = new WeakReference<Activity>(hostActivity);
	}
	
	protected Activity getHostActivity() {
		return mHostActivityRef.get();
	}
	
	/**
	 * Extracts the provided URL from the exit scheme
	 * 
	 * @param url
	 *            the exit scheme url to parse
	 * @return the extracted, provided & decoded URL
	 */
	protected String parseSponsorPayExitUrlForTargetUrl(String url) {
		Uri uri = Uri.parse(url);

		return uri.getQueryParameter(EXIT_URL_TARGET_URL_PARAM_KEY);
	}

	/**
	 * Extracts the status code from the scheme
	 * 
	 * @param url
	 *            the url to parsed for the status code
	 * @return the status code
	 */
	protected int parseSponsorPayExitUrlForResultCode(String url) {
		Uri uri = Uri.parse(url);

		String resultCodeAsString = uri.getQueryParameter(EXIT_URL_RESULT_CODE_PARAM_KEY);

		if (resultCodeAsString != null) {
			return (Integer.parseInt(resultCodeAsString));
		}
		return RESULT_CODE_NO_STATUS_CODE;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		SponsorPayLogger.i(LOG_TAG, "shouldOverrideUrlLoading called with url: " + url);

		if (url.startsWith(SPONSORPAY_SCHEMA)) {
			Uri uri = Uri.parse(url);
			String host = uri.getHost();
			if (host.equals(SPONSORPAY_EXIT_SCHEMA)) {
				// ?status=<statusCode>&url=<url>)url is optional)
				int resultCode = parseSponsorPayExitUrlForResultCode(url);
				String targetUrl = parseSponsorPayExitUrlForTargetUrl(url);
	
				SponsorPayLogger.i(LOG_TAG, "Overriding. Target Url: " + targetUrl);
				
				onSponsorPayExitScheme(resultCode, targetUrl);
			} else {
				processSponsorPayScheme(host, uri);
			}

			return true;
		} else {
			SponsorPayLogger.i(LOG_TAG, "Not overriding");
			return false;
		}
	}

	protected boolean launchActivityWithUrl(String url) {
		Activity hostActivity = getHostActivity();
		if (null == hostActivity) {
			return false;
		}
		if (url == null) {
			return false;
		}
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(url);
		intent.setData(uri);
		try {
			hostActivity.startActivity(intent);
			onTargetActivityStart(url);
		} catch (ActivityNotFoundException e) {
			if (uri.getScheme().equalsIgnoreCase("market") && !IntentHelper.isIntentAvailable(getHostActivity(),
					Intent.ACTION_VIEW, 
					// dummy search to validate Play Store application
					Uri.parse("market://search?q=pname:com.google"))) {
				SponsorPayLogger.e(LOG_TAG, "Play Store is not installed on this device...");
				onPlayStoreNotFound();
			}
			// else - fail silently
			return false;
		}
		return true;
	}

	protected abstract void processSponsorPayScheme(String host, Uri uri);
	
	protected abstract void onSponsorPayExitScheme(int resultCode, String targetUrl);
	
	protected abstract void onTargetActivityStart(String targetUrl);
	
	protected void onPlayStoreNotFound() {
		showDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.ERROR_PLAY_STORE_UNAVAILABLE));
	}
	
	public void showDialog(String errorMessage) {
		String errorDialogTitle = SponsorPayPublisher
				.getUIString(UIStringIdentifier.ERROR_DIALOG_TITLE);
		String dismissButtonCaption = SponsorPayPublisher
				.getUIString(UIStringIdentifier.DISMISS_ERROR_DIALOG);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getHostActivity());
		dialogBuilder.setTitle(errorDialogTitle);
		dialogBuilder.setMessage(errorMessage);
		dialogBuilder.setNegativeButton(dismissButtonCaption, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = dialogBuilder.create();
		dialog.setOwnerActivity(getHostActivity());
		try {
			dialog.show();
		} catch (BadTokenException e) {
			SponsorPayLogger.e(getClass().getSimpleName(),
					"Couldn't show error dialog. Not displayed error message is: " + errorMessage,
					e);
		}
	}
	
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
            SslError error) {
    	//URL is null, relying on the certificate issues
		if (error.getCertificate().getIssuedBy().getOName()
				.matches(".*StartCom Ltd.*")) {
			handler.proceed();
		}
    }
	
}
