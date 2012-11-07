/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.session.SPSession;
import com.sponsorpay.sdk.android.session.SPSessionManager;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * <p>
 * Retrieves the SponsorPay mobile Offer Wall and shows it embedded in a WebView.
 * </p>
 */
public class OfferWallActivity extends Activity {
	public static final String EXTRA_OFFERWALL_TYPE = "EXTRA_OFFERWALL_TEMPLATE_KEY";
	public static final String OFFERWALL_TYPE_MOBILE = "OFFERWALL_TYPE_MOBILE";
	public static final String OFFERWALL_TYPE_UNLOCK = "OFFERWALL_TYPE_UNLOCK";

	
	public static final String EXTRA_SESSION_TOKEN_KEY = "EXTRA_SESSION_TOKEN_KEY";
	
	/**
	 * Key for extracting the value of {@link #mShouldStayOpen} from the extras bundle.
	 */
	public static final String EXTRA_SHOULD_STAY_OPEN_KEY = "EXTRA_SHOULD_REMAIN_OPEN_KEY";

	/**
	 * Key for extracting a map of custom key/values to add to the parameters on the OfferWall
	 * request URL from the extras bundle.
	 */
	public static final String EXTRA_KEYS_VALUES_MAP_KEY = "EXTRA_KEY_VALUES_MAP";

	public static final String EXTRA_OVERRIDING_URL_KEY = "EXTRA_OVERRIDING_URL_KEY";

	public static final String EXTRA_CURRENCY_NAME_KEY = "EXTRA_CURRENCY_NAME_KEY";

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

	private String mCurrencyName;
	
	/**
	 * The {@link SPSession} used for showing the Offer Wall
	 */
	private SPSession mSession;
	
	private ActivityOfferWebClient mActivityOfferWebClient;

	/**
	 * Overridden from {@link Activity}. Upon activity start, extract the user ID from the extra,
	 * create the web view and setup the interceptor for the web view exit-request.
	 * 
	 * @param savedInstanceState
	 *            Android's savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setOwnerActivity(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setMessage(SponsorPayPublisher
				.getUIString(UIStringIdentifier.LOADING_OFFERWALL));
		mProgressDialog.show();

		instantiateTemplate();

		fetchPassedExtras();

		mWebView = new WebView(getApplicationContext());
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		setContentView(mWebView);

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);

		mActivityOfferWebClient = new ActivityOfferWebClient(OfferWallActivity.this,
				mShouldStayOpen) {
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
					String failingUrl) {
				SponsorPayLogger.e(getClass().getSimpleName(), String.format(
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
		};
		mWebView.setWebViewClient(mActivityOfferWebClient);
		
		
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress > 50 && mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				super.onProgressChanged(view, newProgress);
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
		String sessionToken = getIntent().getStringExtra(EXTRA_SESSION_TOKEN_KEY);
		
		mSession = SPSessionManager.getSession(sessionToken);
		
		mShouldStayOpen = getIntent().getBooleanExtra(EXTRA_SHOULD_STAY_OPEN_KEY,
				mTemplate.shouldStayOpenByDefault());

		Serializable inflatedKvMap = getIntent().getSerializableExtra(EXTRA_KEYS_VALUES_MAP_KEY);
		if (inflatedKvMap instanceof HashMap<?, ?>) {
			mCustomKeysValues = (HashMap<String, String>) inflatedKvMap;
		}

		String currencyName = getIntent().getStringExtra(EXTRA_CURRENCY_NAME_KEY);
		
		if (StringUtils.notNullNorEmpty(currencyName)) {
			mCurrencyName = currencyName;
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

			SponsorPayLogger.d(getClass().getSimpleName(), "Offerwall request url: " + offerwallUrl);
			mWebView.loadUrl(offerwallUrl);
		} catch (RuntimeException ex) {
			SponsorPayLogger.e(getClass().getSimpleName(),
					"An exception occurred when launching the Offer Wall", ex);
			mActivityOfferWebClient.showDialog(ex.getMessage());
		}
	}

	private String determineUrl() {
		if (StringUtils.notNullNorEmpty(mOverridingUrl)) {
			return mOverridingUrl;
		} else {
			return generateUrl();
		}
	}

	private String generateUrl() {
		mCustomKeysValues = mTemplate.addAdditionalParameters(mCustomKeysValues);
		if (mCurrencyName != null && !mCurrencyName.trim().equals("")) {
			if (mCustomKeysValues == null) {
				mCustomKeysValues = new HashMap<String, String>();
			}
			mCustomKeysValues.put(UrlBuilder.URL_PARAM_CURRENCY_NAME_KEY, mCurrencyName);
		}
		String baseUrl = mTemplate.getBaseUrl();
		return UrlBuilder.newBuilder(baseUrl, mSession)
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
		mActivityOfferWebClient.showDialog(errorMessage);
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
		private static final String OFFERWALL_PRODUCTION_BASE_URL = "https://iframe.sponsorpay.com/mobile?";
		private static final String OFFERWALL_STAGING_BASE_URL = "https://staging-iframe.sponsorpay.com/mobile?";

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
		private static final String UNLOCK_OFFERWALL_PRODUCTION_BASE_URL = "https://iframe.sponsorpay.com/unlock?";
		private static final String UNLOCK_OFFERWALL_STAGING_BASE_URL = "https://staging-iframe.sponsorpay.com/unlock?";

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

			if (StringUtils.notNullNorEmpty(mUnlockItemName)) {
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
