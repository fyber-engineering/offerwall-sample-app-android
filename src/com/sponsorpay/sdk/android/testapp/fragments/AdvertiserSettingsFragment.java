package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;

public class AdvertiserSettingsFragment extends AbstractSettingsFragment {
	
	private static final String DELAY_PREFS_KEY = "DELAY";
	private static final int DEFAULT_DELAY_MIN = 15;
	private int mCallDelay;
	private EditText mDelayField;
	private String mAuxOverridingId;

	@Override
	public void onResume() {
		super.onResume();
		if (mAuxOverridingId != null) {
			sendCallback(mAuxOverridingId);
			mAuxOverridingId = null;
			getFragmentManager().popBackStack();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_advertiser, container, false);
	}

	@Override
	protected String getFragmentTitle() {
		return getResources().getString(R.string.advertiser);
	}

	@Override
	protected void setValuesInFields() {
		mDelayField.setText(String.format("%d", mCallDelay));
	}

	@Override
	protected void bindViews() {
		mDelayField = (EditText) findViewById(R.id.delay_field);
	}

	@Override
	protected void fetchValuesFromFields() {
		String delay = mDelayField.getText().toString();

		Integer parsedInt;

		try {
			parsedInt = Integer.parseInt(delay);
			if (parsedInt <= 0) {
				parsedInt = DEFAULT_DELAY_MIN;
			}
		} catch (NumberFormatException e) {
			parsedInt = DEFAULT_DELAY_MIN;
		}

		mDelayField.setText(String.format("%d", parsedInt));

		mCallDelay = parsedInt;
	}

	@Override
	protected void readPreferences(SharedPreferences prefs) {
		mCallDelay = prefs.getInt(DELAY_PREFS_KEY, DEFAULT_DELAY_MIN);
	}

	@Override
	protected void storePreferences(Editor prefsEditor) {
		prefsEditor.putInt(DELAY_PREFS_KEY, mCallDelay);
	}
	
	
	public void sendCallbackWithDelay(String overridingAppId){
		fetchValuesFromFields();
		try {
			SponsorPayAdvertiser.registerWithDelay(getApplicationContext(), mCallDelay,
					overridingAppId);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
					ex);
		}
	}
	
	
	public void sendCallback(String overridingAppId){
		try {
			SponsorPayAdvertiser.register(getApplicationContext(), overridingAppId);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
					ex);
		}
	}

	public void sendCallbackOnStart(String overridingAppId) {
		mAuxOverridingId = overridingAppId;
	}
}
