/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
	public static final String EXTRA_OFFERWALL_TYPE = "EXTRA_OFFERWALL_TEMPLATE_KEY";
	public static final String OFFERWALL_TYPE_MOBILE = "OFFERWALL_TYPE_MOBILE";
	public static final String OFFERWALL_TYPE_UNLOCK = "OFFERWALL_TYPE_UNLOCK";

	/**
	 * Key for extracting the current user ID from the extras bundle.
	 */
	public static final String EXTRA_USERID_KEY = "EXTRA_USERID_KEY";

	/**
	 * Key for extracting the value of {@link #mShouldStayOpen} from the extras bundle.
	 */
	public static final String EXTRA_SHOULD_STAY_OPEN_KEY = "EXTRA_SHOULD_REMAIN_OPEN_KEY";

	/**
	 * Key for extracting the App ID from the extras bundle. If no app id is provided it will be
	 * retrieved from the application manifest.
	 */
	public static final String EXTRA_OVERRIDING_APP_ID_KEY = "EXTRA_OVERRIDING_APP_ID";

	/**
	 * Key for extracting a map of custom key/values to add to the parameters on the OfferWall
	 * request URL from the extras bundle.
	 */
	public static final String EXTRA_KEYS_VALUES_MAP_KEY = "EXTRA_KEY_VALUES_MAP";

	public static final String EXTRA_OVERRIDING_URL_KEY = "EXTRA_OVERRIDING_URL_KEY";

	/**
	 * The result code that is returned when the Offer Wall's parsed exit scheme does not contain a
	 * status code.
	 */
	public static final int RESULT_CODE_NO_STATUS_CODE = -10;

	/**
	 * Full-size web view within the activity
	 */
	protected WebView mWebView;

	/**
	 * The user ID (after extracting it from the extra)
	 */
	protected UserId mUserId;

	/**
	 * Information about the hosting application and device.
	 */
	protected HostInfo mHostInfo;

	/**
	 * Whether this activity should stay open or close when the user is redirected outside the
	 * application by clicking on an offer.
	 */
	private boolean mShouldStayOpen;

	/**
	 * Map of custom key/values to add to the parameters on the OfferWall request URL.
	 */
	protected Map<String, String> mCustomKeysValues;

	/**
	 * Loading progress dialog.
	 */
	private ProgressDialog mProgressDialog;

	/**
	 * Error dialog.
	 */
	private AlertDialog mErrorDialog;

	private OfferWallTemplate mTemplate;

	private String mOverridingUrl;

	/**
	 * Overridden from {@link Activity}. Upon activity start, extract the user ID from the extra,
	 * create the web view and setup the interceptor for the web view exit-request.
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
		mProgressDialog.setMessage(SponsorPayPublisher
				.getUIString(UIStringIdentifier.LOADING_OFFERWALL));
		mProgressDialog.show();

		mHostInfo = new HostInfo(getApplicationContext());

		instantiateTemplate();

		fetchPassedExtras();

		mWebView = new WebView(getApplicationContext());
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		setContentView(mWebView);

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);

		mWebView.setWebViewClient(new ActivityOfferWebClient(OfferWallActivity.this,
				mShouldStayOpen) {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
					String failingUrl) {
				// super.onReceivedError(view, errorCode, description, failingUrl);
				Log.e(getClass().getSimpleName(), String.format(
						"OfferWall WebView triggered an error. "
								+ "Error code: %d, error description: %s. Failing URL: %s",
						errorCode, description, failingUrl));

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

	private void instantiateTemplate() {
		String templateName = getIntent().getStringExtra(EXTRA_OFFERWALL_TYPE);

		if (OFFERWALL_TYPE_UNLOCK.equals(templateName)) {
			mTemplate = new UnlockOfferWallTemplate();
		} else {
			mTemplate = new MobileOfferWallTemplate();
		}
	}

	@SuppressWarnings("unchecked")
	protected void fetchPassedExtras() {
		// Get data from extras
		String passedUserId = getIntent().getStringExtra(EXTRA_USERID_KEY);
		mUserId = UserId.make(getApplicationContext(), passedUserId);

		mShouldStayOpen = getIntent().getBooleanExtra(EXTRA_SHOULD_STAY_OPEN_KEY,
				mTemplate.shouldStayOpenByDefault());

		Serializable inflatedKvMap = getIntent().getSerializableExtra(EXTRA_KEYS_VALUES_MAP_KEY);
		if (inflatedKvMap instanceof HashMap<?, ?>) {
			mCustomKeysValues = (HashMap<String, String>) inflatedKvMap;
		}

		String overridingAppId = getIntent().getStringExtra(EXTRA_OVERRIDING_APP_ID_KEY);

		if (overridingAppId != null && !overridingAppId.equals("")) {
			mHostInfo.setOverriddenAppId(overridingAppId);
		}

		mOverridingUrl = getIntent().getStringExtra(EXTRA_OVERRIDING_URL_KEY);

		mTemplate.fetchAdditionalExtras();
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
	 * Overridden from {@link Activity}. Loads or reloads the contents of the offer wall webview.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		try {
			String offerwallUrl = determineUrl();

			Log.d(getClass().getSimpleName(), "Offerwall request url: " + offerwallUrl);
			mWebView.loadUrl(offerwallUrl);
		} catch (RuntimeException ex) {
			Log.e(getClass().getSimpleName(),
					"An exception occurred when launching the Offer Wall", ex);
			showErrorDialog(ex.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		mWebView.destroy();
		super.onDestroy();
	}

	private String determineUrl() {
		if (mOverridingUrl != null && !mOverridingUrl.equals(""))
			return mOverridingUrl;
		else
			return generateUrl();
	}

	private String generateUrl() {
		mCustomKeysValues = mTemplate.addAdditionalParameters(mCustomKeysValues);
		String baseUrl = mTemplate.getBaseUrl();
		return UrlBuilder.newBuilder(baseUrl, mHostInfo).setUserId(mUserId.toString())
				.addExtraKeysValues(mCustomKeysValues).addScreenMetrics().buildUrl();
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
		String errorDialogTitle = SponsorPayPublisher
				.getUIString(UIStringIdentifier.ERROR_DIALOG_TITLE);
		String dismissButtonCaption = SponsorPayPublisher
				.getUIString(UIStringIdentifier.DISMISS_ERROR_DIALOG);

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
			Log.e(getClass().getSimpleName(),
					"Couldn't show error dialog. Not displayed error message is: " + errorMessage,
					e);
		}
	}

	public abstract class OfferWallTemplate {
		public abstract void fetchAdditionalExtras();

		public abstract String getBaseUrl();

		public abstract Map<String, String> addAdditionalParameters(Map<String, String> params);

		public abstract boolean shouldStayOpenByDefault();
	}

	public class MobileOfferWallTemplate extends OfferWallTemplate {
		/**
		 * Sponsorpay's URL to contact within the web view
		 */
		private static final String OFFERWALL_PRODUCTION_BASE_URL = "http://iframe.sponsorpay.com/mobile?";
		private static final String OFFERWALL_STAGING_BASE_URL = "http://staging.iframe.sponsorpay.com/mobile?";

		@Override
		public void fetchAdditionalExtras() {

		}

		@Override
		public String getBaseUrl() {
			return SponsorPayPublisher.shouldUseStagingUrls() ? OFFERWALL_STAGING_BASE_URL
					: OFFERWALL_PRODUCTION_BASE_URL;
		}

		@Override
		public Map<String, String> addAdditionalParameters(Map<String, String> params) {
			return params;
		}

		@Override
		public boolean shouldStayOpenByDefault() {
			return true;
		}

	}

	public class UnlockOfferWallTemplate extends OfferWallTemplate {
		/**
		 * Sponsorpay's URL to contact within the web view
		 */
		private static final String UNLOCK_OFFERWALL_PRODUCTION_BASE_URL = "http://iframe.sponsorpay.com/unlock?";
		private static final String UNLOCK_OFFERWALL_STAGING_BASE_URL = "http://staging.iframe.sponsorpay.com/unlock?";

		/**
		 * Key for extracting the value of {@link #mUnlockItemId} from the extras bundle.
		 */
		public static final String EXTRA_UNLOCK_ITEM_ID_KEY = "EXTRA_UNLOCK_ITEM_ID_KEY";

		/**
		 * Key for extracting the value of {@link #mUnlockItemName} from the extras bundle.
		 */
		public static final String EXTRA_UNLOCK_ITEM_NAME_KEY = "EXTRA_UNLOCK_ITEM_NAME_KEY";

		public static final String PARAM_UNLOCK_ITEM_ID_KEY = "itemid";
		public static final String PARAM_UNLOCK_ITEM_NAME_KEY = "item_name";

		private String mUnlockItemId;
		private String mUnlockItemName;

		@Override
		public void fetchAdditionalExtras() {
			mUnlockItemId = getIntent().getStringExtra(EXTRA_UNLOCK_ITEM_ID_KEY);
			mUnlockItemName = getIntent().getStringExtra(EXTRA_UNLOCK_ITEM_NAME_KEY);
		}

		@Override
		public String getBaseUrl() {
			return SponsorPayPublisher.shouldUseStagingUrls() ? UNLOCK_OFFERWALL_STAGING_BASE_URL
					: UNLOCK_OFFERWALL_PRODUCTION_BASE_URL;
		}

		@Override
		public Map<String, String> addAdditionalParameters(Map<String, String> params) {
			if (params == null) {
				params = new HashMap<String, String>();
			}
			params.put(PARAM_UNLOCK_ITEM_ID_KEY, mUnlockItemId);

			if (null != mUnlockItemName && !"".equals(mUnlockItemName)) {
				params.put(PARAM_UNLOCK_ITEM_NAME_KEY, mUnlockItemName);
			}

			return params;
		}

		@Override
		public boolean shouldStayOpenByDefault() {
			return false;
		}

	}
}
