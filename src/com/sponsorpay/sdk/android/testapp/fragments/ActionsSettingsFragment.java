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
import com.sponsorpay.sdk.android.utils.StringUtils;

public class ActionsSettingsFragment extends AbstractSettingsFragment {
	
	private static final String ACTION_ID_PREFS_KEY = "ACTION_ID";
	private String mActionId;
	private EditText mActionIdField;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_actions, container, false);
	}
	

	@Override
	protected String getFragmentTitle() {
		return getResources().getString(R.string.actions);
	}


	@Override
	protected void setValuesInFields() {
		mActionIdField.setText(mActionId);
	}


	@Override
	protected void bindViews() {
		mActionIdField = (EditText) findViewById(R.id.action_id_field);
	}


	@Override
	protected void fetchValuesFromFields() {
		mActionId = mActionIdField.getText().toString();
	}


	@Override
	protected void readPreferences(SharedPreferences prefs) {
		mActionId = prefs.getString(ACTION_ID_PREFS_KEY, StringUtils.EMPTY_STRING);
	}


	@Override
	protected void storePreferences(Editor prefsEditor) {
		prefsEditor.putString(ACTION_ID_PREFS_KEY, mActionId);
	}
	
	
	public void sendActionCompleted(){
		fetchValuesFromFields();
		try {
			SponsorPayAdvertiser.reportActionCompletion(mActionId);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
					ex);
		}
	}
	
	
}
