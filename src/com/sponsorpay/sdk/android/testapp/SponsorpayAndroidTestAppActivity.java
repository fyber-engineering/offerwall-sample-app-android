package com.sponsorpay.sdk.android.testapp;

import java.util.EnumMap;

import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.InterstitialLoader.InterstitialLoadingStatusListener;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerAbstractResponse;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerDeltaOfCoinsResponse;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.currency.VirtualCurrencyConnector;
import com.sponsorpay.sdk.android.testapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Example activity in order to show the usage of Sponsorpay Android SDK.
 */
public class SponsorpayAndroidTestAppActivity extends Activity {
	/**
	 * Used to enclose user provided parameters from the state of the UI fields and check boxes in order to configure
	 * the launch of the Offer Wall or Interstitial.
	 */
	private class LaunchOptions {
		public String overridenAppId;
		public String userId;
		public boolean shouldStayOpen;
		public boolean shouldUseStagingUrls;
		public String backgroundUrl;
		public String skinName;
		public String securityToken;
		public int callDelay;
	}

	private static final int DEFAULT_DELAY_MIN = 15;
	
	/**
	 * Called when the activity is first created. See {@link Activity}.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		((TextView) findViewById(R.id.sdk_version_string)).setText("Publisher SDK v. "
				+ SponsorPayPublisher.RELEASE_VERSION_STRING);

		setCustomErrorMessages();
	}

	/**
	 * Sets custom UI messages in the SDK to demonstrate the use of SponsorPayPublisher.setCustomUIStrings();
	 */
	private void setCustomErrorMessages() {
		EnumMap<UIStringIdentifier, Integer> customUIStrings = new EnumMap<UIStringIdentifier, Integer>(
				UIStringIdentifier.class);
		customUIStrings.put(UIStringIdentifier.ERROR_DIALOG_TITLE, R.string.custom_error_message);
		SponsorPayPublisher.setCustomUIStrings(customUIStrings, getApplicationContext());
	}

	/**
	 * Fetches user provided options from the state of the UI text fields and text boxes.
	 * 
	 * @return A {@link LaunchOptions} instance filled with the retrieved data.
	 */
	private LaunchOptions fetchLaunchOptions() {
		EditText appIdField = (EditText) findViewById(R.id.app_id_field);
		EditText userIdField = (EditText) findViewById(R.id.user_id_field);
		CheckBox keepOfferwallOpenCheckBox = (CheckBox) findViewById(R.id.keep_offerwall_open_checkbox);
		CheckBox useStagingUrlsCheckBox = (CheckBox) findViewById(R.id.use_staging_urls_checkbox);
		EditText skinNameField = (EditText) findViewById(R.id.skin_name_field);
		EditText backgroundUrlField = (EditText) findViewById(R.id.background_url_field);
		EditText securityTokenField = (EditText) findViewById(R.id.security_token_field);
		EditText delayField = (EditText) findViewById(R.id.delay_field);

		LaunchOptions lo = new LaunchOptions();
		try {
			lo.overridenAppId = appIdField.getText().toString();
		} catch (NumberFormatException e) {
			lo.overridenAppId = null;
		}

		lo.userId = userIdField.getText().toString();
		lo.shouldStayOpen = keepOfferwallOpenCheckBox.isChecked();
		lo.shouldUseStagingUrls = useStagingUrlsCheckBox.isChecked();

		String skinNameValue = skinNameField.getText().toString();
		if (!skinNameValue.equals("")) {
			lo.skinName = skinNameValue;
		}

		lo.backgroundUrl = backgroundUrlField.getText().toString();
		lo.securityToken = securityTokenField.getText().toString();

		String delay = delayField.getText().toString();
		Integer parsedInt = null;

		try {
			parsedInt = Integer.parseInt(delay);
			if (parsedInt <= 0) {
				parsedInt = DEFAULT_DELAY_MIN;
			}
		} catch (NumberFormatException e) {
			parsedInt = DEFAULT_DELAY_MIN;
		}

		delayField.setText(String.format("%d", parsedInt));
		
		lo.callDelay = parsedInt;
		
		return lo;
	}

	/**
	 * Triggered when the user clicks on the launch offer wall button.
	 * 
	 * @param v
	 */
	public void onLaunchOfferwallClick(View v) {
		LaunchOptions lo = fetchLaunchOptions();

		SponsorPayPublisher.setShouldUseStagingUrls(lo.shouldUseStagingUrls);

		startActivityForResult(
		/* Pass in a User ID */
		SponsorPayPublisher.getIntentForOfferWallActivity(getApplicationContext(), lo.userId, lo.shouldStayOpen,
				lo.overridenAppId), SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
	}

	public void onSendCallbackNowButtonClick(View v) {
		LaunchOptions lo = fetchLaunchOptions();

		SponsorPayAdvertiser.register(getApplicationContext(), lo.overridenAppId);
	}

	public void onSendCallbackWithDelayButtonClick(View v) {
		LaunchOptions lo = fetchLaunchOptions();

		SponsorPayAdvertiser.registerWithDelay(getApplicationContext(), lo.callDelay, lo.overridenAppId);
	}
	
	/**
	 * Triggered when the user clicks on the launch interstitial button.
	 * 
	 * @param v
	 */
	public void onLaunchInsterstitialClick(View v) {
		Log.d("SP", "Launch Interstitial Clicked");
		LaunchOptions lo = fetchLaunchOptions();

		SponsorPayPublisher.setShouldUseStagingUrls(lo.shouldUseStagingUrls);

		SponsorPayPublisher.loadShowInterstitial(this, lo.userId, new InterstitialLoadingStatusListener() {

			@Override
			public void onInterstitialLoadingTimeOut() {
				Log.d(SponsorpayAndroidTestAppActivity.class.toString(), "onInterstitialLoadingTimeOut");
			}

			@Override
			public void onInterstitialRequestError() {
				Log.d(SponsorpayAndroidTestAppActivity.class.toString(), "onInterstitialRequestError");
			}

			@Override
			public void onNoInterstitialAvailable() {
				Log.d(SponsorpayAndroidTestAppActivity.class.toString(), "onNoInterstitialAvailable");
			}

			@Override
			public void onWillShowInterstitial() {
				Log.d(SponsorpayAndroidTestAppActivity.class.toString(), "onWillShowInterstitial");
			}

		}, lo.shouldStayOpen, lo.backgroundUrl, lo.skinName, 0, lo.overridenAppId);
	}

	/**
	 * Triggered when the user clicks on the Request New Coins button. Will send a request for delta of coins to the
	 * currency server and register a callback object to show the result in a dialog box. Uses the values entered for
	 * User ID, App ID and Security Token.
	 * 
	 * @param v
	 */
	public void onRequestNewCoinsClick(View v) {
		Log.d("SP", "Request New Coins Clicked");

		LaunchOptions lo = fetchLaunchOptions();

		final String usedTransactionId = VirtualCurrencyConnector.fetchLatestTransactionIdForUser(getApplicationContext(), lo.userId);

		SPCurrencyServerListener requestListener = new SPCurrencyServerListener() {

			@Override
			public void onSPCurrencyServerError(CurrencyServerAbstractResponse response) {
				showCancellableAlertBox(
						"Response or Request Error",
						String.format("%s\n%s\n%s\n", response.getErrorType(), response.getErrorCode(),
								response.getErrorMessage()));
			}

			@Override
			public void onSPCurrencyDeltaReceived(CurrencyServerDeltaOfCoinsResponse response) {
				showCancellableAlertBox("Response From Currency Server", String.format("Delta of Coins: %s\n\n"
						+ "Used Latest Transaction ID: %s\n\n" + "Returned Latest Transaction ID: %s\n\n",
						response.getDeltaOfCoins(), usedTransactionId, response.getLatestTransactionId()));

			}
		};

		SponsorPayPublisher.requestNewCoins(getApplicationContext(), lo.userId, requestListener, null,
				lo.securityToken, lo.overridenAppId);
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
}