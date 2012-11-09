package com.sponsorpay.sdk.android.testapp;

import java.util.EnumMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerAbstractResponse;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerDeltaOfCoinsResponse;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.currency.VirtualCurrencyConnector;
import com.sponsorpay.sdk.android.session.SPSession;
import com.sponsorpay.sdk.android.session.SPSessionManager;
import com.sponsorpay.sdk.android.testapp.fragments.ActionsSettingsFragment;
import com.sponsorpay.sdk.android.testapp.fragments.BannersSettingsFragment;
import com.sponsorpay.sdk.android.testapp.fragments.InterstitialSettingsFragment;
import com.sponsorpay.sdk.android.testapp.fragments.ItemsSettingsFragment;
import com.sponsorpay.sdk.android.testapp.fragments.LauncherFragment;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * Example activity in order to show the usage of Sponsorpay Android SDK.
 */
public class SponsorpayAndroidTestAppActivity extends FragmentActivity {
	
	private static final String TAG = SponsorpayAndroidTestAppActivity.class.getSimpleName();

	/**
	 * Shared preferences file name. Stores the values entered into the UI fields.
	 */
	private static final String PREFERENCES_FILE_NAME = "SponsorPayTestAppState";

	private static final String APP_ID_PREFS_KEY = "APP_ID";
	private static final String USER_ID_PREFS_KEY = "USER_ID";
	private static final String SECURITY_TOKEN_PREFS_KEY = "SECURITY_TOKEN";
	private static final String CURRENCY_NAME_PREFS_KEY = "CURRENCY_NAME";
	private static final String USE_STAGING_URLS_PREFS_KEY = "USE_STAGING_URLS";
	
	private static final int MAIN_SETTINGS_ACTIVITY_CODE = 3962;

	private String mCurrencyName;
	
	private EditText mAppIdField;
	private EditText mUserIdField;
	private EditText mSecurityTokenField;
	private EditText mCurrencyNameField;
	private TextView mSessionInfo;

	private CheckBox mUseStagingUrlsCheckBox;
	
	private boolean mShouldStayOpen;
	
	/**
	 * Called when the activity is first created. See {@link Activity}.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		bindViews();
		setCustomErrorMessages();
		
		createLauncherFragment();

		((TextView) findViewById(R.id.sdk_version_string)).setText("SP Android SDK v. "
				+ SponsorPay.RELEASE_VERSION_STRING);

		SponsorPayLogger.enableLogging(true);
	}

	private void createLauncherFragment() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
		if (fragment == null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.add(R.id.fragment_placeholder, new LauncherFragment());
			transaction.commit();
		}
	}

	protected void bindViews() {
		mAppIdField = (EditText) findViewById(R.id.app_id_field);
		mUserIdField = (EditText) findViewById(R.id.user_id_field);
		mSecurityTokenField = (EditText) findViewById(R.id.security_token_field);
		mCurrencyNameField = (EditText) findViewById(R.id.currency_name_field);
		
		mUseStagingUrlsCheckBox = (CheckBox) findViewById(R.id.use_staging_urls_checkbox);
		
		mSessionInfo = (TextView) findViewById(R.id.session_info);

	}


	public void onSettingsButtonClick (View v) {
		fetchValuesFromFields();
		Intent intent = new Intent(getApplicationContext(),
				MainSettingsActivity.class);
		intent.putExtra(MainSettingsActivity.PREFERENCES_EXTRA, PREFERENCES_FILE_NAME);
		startActivityForResult(intent,
				MAIN_SETTINGS_ACTIVITY_CODE);
	}
	
	
	@Override
	protected void onPause() {
		// Save the state of the UI fields into the app preferences.
		fetchValuesFromFields();

		SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();

		try {
			SPSession session = SPSessionManager.getCurrentSession();
			prefsEditor.putString(APP_ID_PREFS_KEY, session.getAppId());
			prefsEditor.putString(USER_ID_PREFS_KEY, session.getUserId());
			prefsEditor.putString(SECURITY_TOKEN_PREFS_KEY, session.getSecurityToken());
		} catch (RuntimeException e) {
			SponsorPayLogger.d(TAG, "There's no current session.");
		}
		
		prefsEditor.putString(CURRENCY_NAME_PREFS_KEY, mCurrencyName);
		prefsEditor.putBoolean(USE_STAGING_URLS_PREFS_KEY, mUseStagingUrlsCheckBox.isChecked());

		prefsEditor.commit();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Recover the state of the UI fields from the app preferences.
		SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

		String overridingAppId = prefs.getString(APP_ID_PREFS_KEY, StringUtils.EMPTY_STRING);
		String userId = prefs.getString(USER_ID_PREFS_KEY, StringUtils.EMPTY_STRING);
		String securityToken = prefs.getString(SECURITY_TOKEN_PREFS_KEY, StringUtils.EMPTY_STRING);
		mCurrencyName = prefs.getString(CURRENCY_NAME_PREFS_KEY, StringUtils.EMPTY_STRING);
		
		try {
			SPSessionManager.initialize(overridingAppId, userId, securityToken, getApplicationContext());
		} catch (RuntimeException e){
			SponsorPayLogger.d(TAG,
						e.getLocalizedMessage());
		}
		
		mShouldStayOpen = prefs.getBoolean(MainSettingsActivity.KEEP_OFFERWALL_OPEN_PREFS_KEY, true);

		setValuesInFields();

		mUseStagingUrlsCheckBox.setChecked(prefs.getBoolean(USE_STAGING_URLS_PREFS_KEY, false));

	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case MAIN_SETTINGS_ACTIVITY_CODE:
				mShouldStayOpen = data.getBooleanExtra(
						MainSettingsActivity.KEEP_OFFERWALL_OPEN_EXTRA, true);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Sets one custom UI message in the SDK to demonstrate the use of
	 * SponsorPayPublisher.setCustomUIStrings();
	 */
	private void setCustomErrorMessages() {
		EnumMap<UIStringIdentifier, Integer> customUIStrings = new EnumMap<UIStringIdentifier, Integer>(
				UIStringIdentifier.class);
		customUIStrings.put(UIStringIdentifier.ERROR_DIALOG_TITLE, R.string.custom_error_message);
		SponsorPayPublisher.setCustomUIStrings(customUIStrings, getApplicationContext());
	}

	/**
	 * Fetches user provided values from the state of the UI text fields and text boxes.
	 */
	private void fetchValuesFromFields() {

		mCurrencyName = mCurrencyNameField.getText().toString();

		SponsorPayAdvertiser.setShouldUseStagingUrls(mUseStagingUrlsCheckBox
				.isChecked());
		SponsorPayPublisher.setShouldUseStagingUrls(mUseStagingUrlsCheckBox
				.isChecked());
	}

	/**
	 * Sets values in the state of the UI text fields and text boxes.
	 */
	private void setValuesInFields() {
		try {
			SPSession session = SPSessionManager.getCurrentSession();
			mAppIdField.setText(session.getAppId());
			mUserIdField.setText(session.getUserId());
			mSecurityTokenField.setText(session.getSecurityToken());
		} catch (RuntimeException e) {
			SponsorPayLogger.d(TAG, "There's no current session.");
		}
		mCurrencyNameField.setText(mCurrencyName);
		setSessionInfo();
	}
	
	private void setSessionInfo() {
		try {
			mSessionInfo.setText(SPSessionManager.getCurrentSession().toString());
		} catch (RuntimeException e) {
			SponsorPayLogger.d(TAG,
					"There's no session yet, unable to send the callback.");
		}
	}
	
	/**
	 * Triggered when the user clicks on the create new session button.
	 * 
	 * @param v
	 */
	public void onCreateNewSessionClick(View v) {
		try {
			String overridingAppId = mAppIdField.getText().toString();
			String userId = mUserIdField.getText().toString();
			String securityToken = mSecurityTokenField.getText().toString();
			SPSessionManager.initialize(overridingAppId, userId, securityToken, getApplicationContext());
		} catch (RuntimeException e){
			showCancellableAlertBox("Exception from SDK", e.getMessage());
			SponsorPayLogger.e(TAG,
					"SponsorPay SDK Exception: ", e);
		}
		setSessionInfo();
	}

	/**
	 * Triggered when the user clicks on the launch offer wall button.
	 * 
	 * @param v
	 */
	public void onLaunchOfferwallClick(View v) {
		fetchValuesFromFields();
		try {
			startActivityForResult(
			/* Pass in a User ID */
			SponsorPayPublisher.getIntentForOfferWallActivity(
					getApplicationContext(), mShouldStayOpen, mCurrencyName, null),
					SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			SponsorPayLogger.e(TAG,
					"SponsorPay SDK Exception: ", ex);
		}
	}

	/**
	 * Triggered when the user clicks on the launch unlock offer wall button.
	 * 
	 * @param v
	 */
	public void onLaunchUnlockOfferwallClick(View v) {
		fetchValuesFromFields();

		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.fragment_placeholder);

		if (fragment instanceof ItemsSettingsFragment) {
			((ItemsSettingsFragment) fragment).launchUnlockOfferWall();
		}
	}
	
	/**
	 * Triggered when the user clicks on the send action button.
	 * 
	 * @param v
	 */
	public void onSendActionClick(View v) {
		fetchValuesFromFields();
		
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.fragment_placeholder);
		
		if (fragment instanceof ActionsSettingsFragment) {
			((ActionsSettingsFragment) fragment).sendActionCompleted();
		}
		
	}
	

	/**
	 * Triggered when the user clicks on the launch interstitial button.
	 * 
	 * @param v
	 */
	public void onLaunchInsterstitialClick(View v) {
		fetchValuesFromFields();

		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.fragment_placeholder);

		if (fragment instanceof InterstitialSettingsFragment) {
			((InterstitialSettingsFragment) fragment).launchInsterstitial(
					mShouldStayOpen, mCurrencyName);
		}
	}

	/**
	 * Triggered when the user clicks on the Request New Coins button. Will send a request for delta
	 * of coins to the currency server and register a callback object to show the result in a dialog
	 * box. Uses the values entered for User ID, App ID and Security Token.
	 * 
	 * @param v
	 */
	public void onRequestNewCoinsClick(View v) {
		fetchValuesFromFields();

		try {
			SPSession session = SPSessionManager.getCurrentSession();
			
			final String usedTransactionId = VirtualCurrencyConnector.fetchLatestTransactionId(
					getApplicationContext(), session.getAppId(), session.getUserId());
			
			SPCurrencyServerListener requestListener = new SPCurrencyServerListener() {
				
				@Override
				public void onSPCurrencyServerError(CurrencyServerAbstractResponse response) {
					showCancellableAlertBox("Response or Request Error", String.format("%s\n%s\n%s\n",
							response.getErrorType(), response.getErrorCode(), response
							.getErrorMessage()));
				}
				
				@Override
				public void onSPCurrencyDeltaReceived(CurrencyServerDeltaOfCoinsResponse response) {
					showCancellableAlertBox("Response From Currency Server", String.format(
							"Delta of Coins: %s\n\n" + "Used Latest Transaction ID: %s\n\n"
									+ "Returned Latest Transaction ID: %s\n\n", response
									.getDeltaOfCoins(), usedTransactionId, response
									.getLatestTransactionId()));
				}
			};
			SponsorPayPublisher.requestNewCoins(getApplicationContext(), requestListener);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			SponsorPayLogger.e(TAG, "SponsorPay SDK Exception: ",
					ex);
		}
	}

	/**
	 * Triggered when the user clicks on the Request SP Unlock Items button. Will send a request for
	 * the status of the SP Unlock items to the server and register a callback object to show the
	 * result in a dialog box. Uses the values entered for User ID, App ID and Security Token.
	 * 
	 * @param v
	 */
	public void onRequestSPUnlockItemsClick(View v) {
		fetchValuesFromFields();

		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.fragment_placeholder);

		if (fragment instanceof ItemsSettingsFragment) {
			((ItemsSettingsFragment) fragment).launchUnlockItems();
		}
	}

	public void onRequestBannerClick(View v) {
		fetchValuesFromFields();
		SponsorPayLogger.i(TAG, "Requesting banner");
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.fragment_placeholder);
		if (fragment instanceof BannersSettingsFragment) {
			((BannersSettingsFragment) fragment).requestBanner(mCurrencyName);
		}
	}

	/**
	 * Shows an alert box with the provided title and message and a unique button to cancel it.
	 * 
	 * @param title
	 *            The title for the alert box.
	 * @param text
	 *            The text message to show inside the alert box.
	 */
	public void showCancellableAlertBox(String title, String text) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(title).setMessage(text).setCancelable(true);
		dialogBuilder.show();
	}

	public SharedPreferences getPrefsStore(){
		return getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
	}
	
	// FRAGMENTS stuff
	
	protected void replaceFragment(Fragment newFragment) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace whatever is in the fragment_container view with this
		// fragment, and add the transaction to the back stack
		
//		transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		newFragment.setRetainInstance(true);
		transaction.replace(R.id.fragment_placeholder, newFragment);
		
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}
	
	public void onInterstitialClick(View view){
		replaceFragment( new InterstitialSettingsFragment());
	}
	
	public void onBannersClick(View view){
		replaceFragment( new BannersSettingsFragment());
	}
	
	public void onItemsClick(View view){
		replaceFragment( new ItemsSettingsFragment());
	}
	
	public void onActionsClick(View view) {
		replaceFragment(new ActionsSettingsFragment());
	}
	
	
}