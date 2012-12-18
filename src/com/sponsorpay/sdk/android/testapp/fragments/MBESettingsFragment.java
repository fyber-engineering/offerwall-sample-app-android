package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerAbstractResponse;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerDeltaOfCoinsResponse;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClient;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageRequestListener;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class MBESettingsFragment extends AbstractSettingsFragment implements SPBrandEngageRequestListener {

	private static final String TAG = "MBESettingsFragment";
	
	private static final String MBE_CHECK_VCS = "MBE_CHECK_VCS";
	private static final String MBE_SHOW_REWARD_NOTIFICATION = "MBE_SHOW_REWARD_NOTIFICATION";
	
	private Intent mIntent;
	private CheckBox mVCSCheckbox;
	private CheckBox mNotificationfCheckbox;

	private boolean mAddVCSListener;
	private boolean mShowNotification;

	private SPCurrencyServerListener mVCSListener = new SPCurrencyServerListener() {
	
			@Override
			public void onSPCurrencyServerError(
					CurrencyServerAbstractResponse response) {
				Log.e(TAG, "VCS error received - " + response.getErrorMessage());
			}
	
			@Override
			public void onSPCurrencyDeltaReceived(
					CurrencyServerDeltaOfCoinsResponse response) {
				Log.d(TAG, "VCS coins received - " + response.getDeltaOfCoins());
			}
		};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_settings_mbe, container, false);
	}

	@Override
	protected String getFragmentTitle() {
		return getResources().getString(R.string.mbe);
	}

	@Override
	protected void setValuesInFields() {
		mVCSCheckbox.setChecked(mAddVCSListener);
		mNotificationfCheckbox.setChecked(mShowNotification);
	}

	@Override
	protected void bindViews() {
		mVCSCheckbox = (CheckBox) findViewById(R.id.mbe_add_vcs_listener_checkbox);
		mNotificationfCheckbox = (CheckBox) findViewById(R.id.mbe_show_notification_checkbox);
	}

	@Override
	protected void fetchValuesFromFields() {
		mAddVCSListener = mVCSCheckbox.isChecked();
		mShowNotification = mNotificationfCheckbox.isChecked();
	}

	@Override
	protected void readPreferences(SharedPreferences prefs) {
		mAddVCSListener = prefs.getBoolean(MBE_CHECK_VCS, true);
		mShowNotification = prefs.getBoolean(MBE_SHOW_REWARD_NOTIFICATION, true);
	}

	@Override
	protected void storePreferences(Editor prefsEditor) {
		prefsEditor.putBoolean(MBE_CHECK_VCS, mAddVCSListener);
		prefsEditor.putBoolean(MBE_SHOW_REWARD_NOTIFICATION, mShowNotification);
	}

	public void requestOffers(String currencyName) {
		fetchValuesFromFields();
		try {
			String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
			
			SPBrandEngageClient.INSTANCE.setShowRewardsNotification(mShowNotification);
			
			SPCurrencyServerListener vcsListener = mAddVCSListener ? mVCSListener : null;
			SponsorPayPublisher.getIntentForMBEActivity(credentialsToken, getActivity(), this,
					currencyName, null, vcsListener);
			
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
					ex);
		}
	}
	
	public void startEngament() {
		if (mIntent != null && SPBrandEngageClient.INSTANCE.canStartEngagement()) {
			SponsorPayLogger.d(TAG, "Starting MBE engagement...");
			startActivity(mIntent);
		}
		
	}

	@Override
	public void onSPBrandEngageOffersAvailable(Intent spBrandEngageActivity) {
		SponsorPayLogger.d(TAG, "SPBrandEngage - intent available");
		mIntent = spBrandEngageActivity;
	}

	@Override
	public void onSPBrandEngageOffersNotAvailable() {
		SponsorPayLogger.d(TAG, "SPBrandEngage - no offers for the moment");
	}

	@Override
	public void onSPBrandEngageError(String errorMessage) {
		SponsorPayLogger.e(TAG, "SPBrandEngage - an error occured:\n" + errorMessage);
	}
	
}
