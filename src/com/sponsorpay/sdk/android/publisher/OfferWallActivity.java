/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.BadTokenException;
import android.webkit.WebView;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;

/**
 * <p>
 * Retrieves the SponsorPay mobile Offer Wall and shows it embedded in a WebView.
 * </p>
 */
public class OfferWallActivity extends Activity {
	private boolean SHOULD_STAY_OPEN_DEFAULT = true;

	/**
	 * Key for extracting the current user ID from the extras bundle.
	 */
	public static final String EXTRA_USERID_KEY = "EXTRA_USERID_KEY";

	/**
	 * Key for extracting the value of {@link #mShouldStayOpen} from the extras bundle.
	 */
	public static final String EXTRA_SHOULD_STAY_OPEN_KEY = "EXTRA_SHOULD_REMAIN_OPEN_KEY";

	/**
	 * Key for extracting the App ID from the extras bundle. If no app id is provided it will be retrieved from the
	 * application manifest.
	 */
	public static final String EXTRA_OVERRIDEN_APP_ID = "EXTRA_OVERRIDEN_APP_ID";

	/**
	 * The result code that is returned when the Offer Wall's parsed exit scheme does not contain a status code.
	 */
	public static final int RESULT_CODE_NO_STATUS_CODE = -10;

	/**
	 * Sponsorpay's URL to contact within the web view
	 */
	private static final String OFFERWALL_PRODUCTION_BASE_URL = "http://iframe.sponsorpay.com/mobile?";
	private static final String OFFERWALL_STAGING_BASE_URL = "http://staging.iframe.sponsorpay.com/mobile?";

	/**
	 * Full-size web view within the activity
	 */
	private WebView mWebView;

	/**
	 * The user ID (after extracting it from the extra)
	 */
	private String mUserId;

	/**
	 * Information about the hosting application and device.
	 */
	private HostInfo mHostInfo;

	/**
	 * Whether this activity should stay open or close when the user is redirected outside the application by clicking
	 * on an offer.
	 */
	private boolean mShouldStayOpen;

	/**
	 * Loading progress dialog.
	 */
	private ProgressDialog mProgressDialog;

	/**
	 * Error dialog.
	 */
	private AlertDialog mErrorDialog;
	
	/**
	 * Overriden from {@link Activity}. Upon activity start, extract the user ID from the extra, create the web view and
	 * setup the interceptor for the web view exit-request.
	 * 
	 * @param savedInstanceState
	 *            Android's savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setOwnerActivity(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setMessage(SponsorPayPublisher.getUIString(UIStringIdentifier.LOADING_OFFERWALL));
		mProgressDialog.show();

		mHostInfo = new HostInfo(getApplicationContext());

		// Get data from extras
		mUserId = getIntent().getStringExtra(EXTRA_USERID_KEY);

		mShouldStayOpen = getIntent().getBooleanExtra(EXTRA_SHOULD_STAY_OPEN_KEY, SHOULD_STAY_OPEN_DEFAULT);

		String overridenAppId = getIntent().getStringExtra(EXTRA_OVERRIDEN_APP_ID);

		if (overridenAppId != null && !overridenAppId.equals("")) {
			mHostInfo.setOverriddenAppId(overridenAppId);
		}

		mWebView = new WebView(OfferWallActivity.this);
		setContentView(mWebView);

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);

		mWebView.setWebViewClient(new ActivityOfferWebClient(OfferWallActivity.this, mShouldStayOpen) {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				// super.onReceivedError(view, errorCode, description, failingUrl);
				UIStringIdentifier error;

				switch (errorCode) {
				case ERROR_HOST_LOOKUP:
				case ERROR_IO:
					error = UIStringIdentifier.ERROR_LOADING_OFFERWALL_NO_INTERNET_CONNECTION;
					break;
				default:
					error = UIStringIdentifier.ERROR_LOADING_OFFERWALL;
					break;
				}
				showErrorDialog(error);
			}
		});
	}

	@Override
	protected void onPause() {
		if (mErrorDialog != null) {
			mErrorDialog.dismiss();
			mErrorDialog = null;
		}
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		super.onPause();
	}
	
	/**
	 * Overriden from {@link Activity}. Loads or reloads the contents of the offer wall webview.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		String offerWallBaseUrl = SponsorPayPublisher.shouldUseStagingUrls() ? OFFERWALL_STAGING_BASE_URL
				: OFFERWALL_PRODUCTION_BASE_URL;
		try {
			String offerwallUrl = UrlBuilder.buildUrl(offerWallBaseUrl, mUserId, mHostInfo, null, null);
			Log.d(getClass().getSimpleName(), "Offerwall request url: " + offerwallUrl);
			mWebView.loadUrl(offerwallUrl);
		} catch (RuntimeException ex) {
			showErrorDialog(ex.getMessage());
		}
	}

	/**
	 * Displays an error dialog with the passed error message on top of the activity.
	 * 
	 * @param error
	 *            Error message to show.
	 */
	protected void showErrorDialog(UIStringIdentifier error) {
		String errorMessage = SponsorPayPublisher.getUIString(error);
		showErrorDialog(errorMessage);
	}

	/**
	 * Displays an error dialog with the passed error message on top of the activity.
	 * 
	 * @param error
	 *            Error message to show.
	 */
	protected void showErrorDialog(String error) {
		String errorMessage = error;
		String errorDialogTitle = SponsorPayPublisher.getUIString(UIStringIdentifier.ERROR_DIALOG_TITLE);
		String dismissButtonCaption = SponsorPayPublisher.getUIString(UIStringIdentifier.DISMISS_ERROR_DIALOG);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(errorDialogTitle);
		dialogBuilder.setMessage(errorMessage);
		dialogBuilder.setNegativeButton(dismissButtonCaption, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				mErrorDialog = null;
				finish();
			}
		});
		mErrorDialog = dialogBuilder.create();
		mErrorDialog.setOwnerActivity(this);
		try {
			mErrorDialog.show();
		} catch (BadTokenException e) {
			Log.e(getClass().getSimpleName(), "Couldn't show error dialog. Not displayed error message is: "
					+ errorMessage, e);
		}
	}
}
