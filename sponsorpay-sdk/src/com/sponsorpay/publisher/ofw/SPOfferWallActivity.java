/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.ofw;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;

/**
 * <p>
 * Retrieves the SponsorPay mobile Offer Wall and shows it embedded in a WebView.
 * </p>
 */
public class SPOfferWallActivity extends Activity {
	
	public static final String EXTRA_CREDENTIALS_TOKEN_KEY = "EXTRA_CREDENTIALS_TOKEN_KEY";
	
	/**
	 * Key for extracting the value of {@link #mShouldStayOpen} from the extras bundle.
	 */
	public static final String EXTRA_SHOULD_STAY_OPEN_KEY = "EXTRA_SHOULD_REMAIN_OPEN_KEY";

	/**
	 * Key for extracting a map of custom key/values to add to the parameters on the OfferWall
	 * request URL from the extras bundle.
	 */
	public static final String EXTRA_KEYS_VALUES_MAP_KEY = "EXTRA_KEY_VALUES_MAP";

	public static final String EXTRA_CURRENCY_NAME_KEY = "EXTRA_CURRENCY_NAME_KEY";

	/**
	 * The result code that is returned when the Offer Wall's parsed exit scheme does not contain a
	 * status code.
	 */
	public static final int RESULT_CODE_NO_STATUS_CODE = -10;
	
	/**
	 * Sponsorpay's URL to contact within the web view
	 */
	private static final String OFW_URL_KEY = "ofw";

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

	private String mCurrencyName;
	
	/**
	 * The {@link SPCredentials} used for showing the Offer Wall
	 */
	private SPCredentials mCredentials;
	
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

		fetchPassedExtras();

		mWebView = new WebView(getApplicationContext());
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		setContentView(mWebView);

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);

		mActivityOfferWebClient = new ActivityOfferWebClient(SPOfferWallActivity.this,
				mShouldStayOpen);

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

	@SuppressWarnings("unchecked")
	protected void fetchPassedExtras() {
		// Get data from extras
		String credentialsToken = getIntent().getStringExtra(EXTRA_CREDENTIALS_TOKEN_KEY);
		
		try {
			mCredentials = SponsorPay.getCredentials(credentialsToken);
		} catch (RuntimeException e) {
			// occurs in the unlikely event when the credentials we're wiped 
			// out of memory and the MOFW was left open
			restoreCredentialsValues();
			deleteCredentialsValues();
		}
		
		mShouldStayOpen = getIntent().getBooleanExtra(EXTRA_SHOULD_STAY_OPEN_KEY,
				shouldStayOpenByDefault());

		Serializable inflatedKvMap = getIntent().getSerializableExtra(EXTRA_KEYS_VALUES_MAP_KEY);
		if (inflatedKvMap instanceof HashMap<?, ?>) {
			mCustomKeysValues = (HashMap<String, String>) inflatedKvMap;
		}

		String currencyName = getIntent().getStringExtra(EXTRA_CURRENCY_NAME_KEY);
		
		if (StringUtils.notNullNorEmpty(currencyName)) {
			mCurrencyName = currencyName;
		}

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
		storeCrendentialsValues();
		super.onPause();
	}

	/**
	 * Overridden from {@link Activity}. Loads or reloads the contents of the offer wall webview.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		try {
			String offerwallUrl = buildUrl();

			SponsorPayLogger.d(getClass().getSimpleName(), "Offerwall request url: " + offerwallUrl);
			mWebView.loadUrl(offerwallUrl);
		} catch (RuntimeException ex) {
			SponsorPayLogger.e(getClass().getSimpleName(),
					"An exception occurred when launching the Offer Wall", ex);
			mActivityOfferWebClient.showDialog(ex.getMessage());
		}
	}

	private String buildUrl() {
		String baseUrl = SponsorPayBaseUrlProvider.getBaseUrl(OFW_URL_KEY);
		return UrlBuilder.newBuilder(baseUrl, mCredentials).setCurrency(mCurrencyName)
				.addExtraKeysValues(mCustomKeysValues).addScreenMetrics().buildUrl();
	}
	
	public boolean shouldStayOpenByDefault() {
		return true;
	}

	// Credentials helper methods
	private static final String UID_KEY = "user.id.key";
	private static final String APPID_KEY = "app.id.key";
	private static final String SECURITY_TOKEN_KEY = "security.token.key";
	
	private void storeCrendentialsValues() {
		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		Editor prefsEditor = preferences.edit();
		prefsEditor.putString(APPID_KEY, mCredentials.getAppId());
		prefsEditor.putString(UID_KEY, mCredentials.getUserId());
		prefsEditor.putString(SECURITY_TOKEN_KEY, mCredentials.getSecurityToken());
		prefsEditor.commit();
	}
	
	private void deleteCredentialsValues() {
		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		Editor prefsEditor = preferences.edit();
		prefsEditor.clear();
		prefsEditor.commit();
	}
	
	private void restoreCredentialsValues() {
		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		String appId = preferences.getString(APPID_KEY, StringUtils.EMPTY_STRING);
		String userId = preferences.getString(UID_KEY, StringUtils.EMPTY_STRING);
		String securityToken = preferences.getString(SECURITY_TOKEN_KEY, StringUtils.EMPTY_STRING);
		SponsorPay.start(appId, userId, securityToken, this);
		mCredentials = SponsorPay.getCurrentCredentials();
	}

}
