package com.sponsorpay.sdk.android.testapp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;
import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiserState;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.session.SPSessionManager;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class MainSettingsActivity extends Activity {
	
	/**
	 * Shared preferences file name. Stores the values entered into the UI fields.
	 */
	public static final String PREFERENCES_EXTRA = "prefs.extra";
	public static final String KEEP_OFFERWALL_OPEN_EXTRA = "keep.offerwall.extra.extra";
	
	private static final String OVERRIDING_URL_PREFS_KEY = "OVERRIDING_URL";
	// this is public in order to be shared with the main activity 
	public static final String KEEP_OFFERWALL_OPEN_PREFS_KEY = "KEEP_OFFERWALL_OPEN";
	
	private Button mBackButton;
	
	private CheckBox mKeepOfferwallOpenCheckBox;
	private CheckBox mSimulateNoPhoneStatePermissionCheckBox;
	private CheckBox mSimulateNoWifiStatePermissionCheckBox;
	private CheckBox mSimulateInvalidAndroidIdCheckBox;
	private CheckBox mSimulateNoSerialNumberCheckBox;

	private TextView mKeyValuesList;

	private EditText mOverridingUrlField;
	private EditText mCustomKeyField, mCustomValueField;
	
	private boolean mShouldStayOpen; 
	
	private String mOverridingUrl;

	private String mPreferencesFileName;

	private Map<String, String> mCustomKeyValuesForRequest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_settings);
		
		mPreferencesFileName = getIntent().getStringExtra(PREFERENCES_EXTRA);
		
		bindViews();
	}

	protected void bindViews() {
		mKeepOfferwallOpenCheckBox = (CheckBox) findViewById(R.id.keep_offerwall_open_checkbox);
		mSimulateNoPhoneStatePermissionCheckBox = (CheckBox) findViewById(R.id.simulate_no_phone_state_permission);
		mSimulateNoWifiStatePermissionCheckBox = (CheckBox) findViewById(R.id.simulate_no_wifi_state_permission);
		mSimulateInvalidAndroidIdCheckBox = (CheckBox) findViewById(R.id.simulate_invalid_android_id);
		mSimulateNoSerialNumberCheckBox = (CheckBox) findViewById(R.id.simulate_no_hw_serial_number);
		
		mCustomKeyValuesForRequest = new HashMap<String, String>();
		mCustomKeyField = (EditText) findViewById(R.id.custom_key_field);
		mCustomValueField = (EditText) findViewById(R.id.custom_value_field);
		mOverridingUrlField = (EditText) findViewById(R.id.overriding_url_field);

		mKeyValuesList = (TextView) findViewById(R.id.key_values_list);
		
		mBackButton = (Button) findViewById(R.id.back_button);
		
		setListenersInViews();
	}
	
	protected void setListenersInViews() {
		mCustomKeyField.setKeyListener(new KeyListener() {
			@Override
			public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					mCustomValueField.requestFocus();
					return true;
				}
				return false;
			}

			@Override
			public boolean onKeyOther(View view, Editable text, KeyEvent event) {
				return false;
			}

			@Override
			public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
				return keyCode == KeyEvent.KEYCODE_ENTER;
			}

			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_TEXT;
			}

			@Override
			public void clearMetaKeyState(View view, Editable content, int states) {
			}
		});

		mCustomValueField.setKeyListener(new KeyListener() {
			@Override
			public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					MainSettingsActivity.this.onAddCustomParameterClick(null);
					mCustomKeyField.requestFocus();
					return true;
				}
				return false;
			}

			@Override
			public boolean onKeyOther(View view, Editable text, KeyEvent event) {
				return false;
			}

			@Override
			public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
				return keyCode == KeyEvent.KEYCODE_ENTER;
			}

			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_TEXT;
			}

			@Override
			public void clearMetaKeyState(View view, Editable content, int states) {
			}
		});
		OnCheckedChangeListener simCheckboxesChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView == mSimulateNoPhoneStatePermissionCheckBox) {
					HostInfo.setSimulateNoReadPhoneStatePermission(isChecked);
				} else if (buttonView == mSimulateNoWifiStatePermissionCheckBox) {
					HostInfo.setSimulateNoAccessWifiStatePermission(isChecked);
				} else if (buttonView == mSimulateInvalidAndroidIdCheckBox) {
					HostInfo.setSimulateInvalidAndroidId(isChecked);
				} else if (buttonView == mSimulateNoSerialNumberCheckBox) {
					HostInfo.setSimulateNoHardwareSerialNumber(isChecked);
				}
			}
		};

		mSimulateNoPhoneStatePermissionCheckBox
				.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mSimulateNoWifiStatePermissionCheckBox
				.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mSimulateInvalidAndroidIdCheckBox
				.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mSimulateNoSerialNumberCheckBox
				.setOnCheckedChangeListener(simCheckboxesChangeListener);
		
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * Invoked when the user clicks on the "Add" button on the custom key/values area.
	 * 
	 * @param v
	 */
	public void onAddCustomParameterClick(View v) {

		if (StringUtils.nullOrEmpty(mCustomKeyField.getText().toString())) {
			Toast.makeText(getApplicationContext(),
					"Key field must contain a valid value", Toast.LENGTH_SHORT).show();
			return;
		}
		
		mCustomKeyValuesForRequest.put(mCustomKeyField.getText().toString(), mCustomValueField
				.getText().toString());

		// is this necessary? the object instance is the same
		SponsorPayAdvertiser.setCustomParameters(mCustomKeyValuesForRequest);
		SponsorPayPublisher.setCustomParameters(mCustomKeyValuesForRequest);

		mCustomKeyField.setText(StringUtils.EMPTY_STRING);
		mCustomValueField.setText(StringUtils.EMPTY_STRING);

		updateCustomParametersList();
	}

	/**
	 * Invoked when the user clicks on the "Clear" button on the custom key/values area.
	 * 
	 * @param v
	 */
	public void onClearCustomParametersClick(View v) {
		mCustomKeyValuesForRequest.clear();
		
//		mCustomKeyField.setText(StringUtils.EMPTY_STRING);
//		mCustomValueField.setText(StringUtils.EMPTY_STRING);
		
		SponsorPayAdvertiser.setCustomParameters(mCustomKeyValuesForRequest);
		SponsorPayPublisher.setCustomParameters(mCustomKeyValuesForRequest);

		updateCustomParametersList();
	}
	
	public void onClearApplicationDataClick(View view) {
		SharedPreferences prefs = getSharedPreferences(
				SponsorPayPublisher.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();
		prefsEditor.clear();
		prefsEditor.commit();

		prefs = getSharedPreferences(
				SponsorPayAdvertiserState.PREFERENCES_FILE_NAME,
				Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
		prefsEditor.clear();
		prefsEditor.commit();
	}
 
	private void updateCustomParametersList() {
		String text = StringUtils.EMPTY_STRING;

		Iterator<String> customKvIterator = mCustomKeyValuesForRequest.keySet().iterator();

		while (customKvIterator.hasNext()) {
			String key = customKvIterator.next();
			String value = mCustomKeyValuesForRequest.get(key);

			text += String.format("%s = %s\n", key, value);
		}

		mKeyValuesList.setText(text);
	}
	
	@Override
	protected void onDestroy() {
		Intent intent = new Intent();
		intent.putExtra(KEEP_OFFERWALL_OPEN_EXTRA, mShouldStayOpen);
		setIntent(intent);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// Save the state of the UI fields into the app preferences.
		fetchValuesFromFields();

		SharedPreferences prefs = getSharedPreferences(mPreferencesFileName, Context.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();
		
		prefsEditor.putString(OVERRIDING_URL_PREFS_KEY, mOverridingUrl);
		prefsEditor.putBoolean(KEEP_OFFERWALL_OPEN_PREFS_KEY, mShouldStayOpen);

		prefsEditor.commit();
		
		super.onPause();
	}

	private void fetchValuesFromFields() {
		mShouldStayOpen = mKeepOfferwallOpenCheckBox.isChecked();
		
		mOverridingUrl = mOverridingUrlField.getText().toString();
		SponsorPayPublisher.setOverridingWebViewUrl(mOverridingUrl);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Recover the state of the UI fields from the app preferences.
		SharedPreferences prefs = getSharedPreferences(mPreferencesFileName,
				Context.MODE_PRIVATE);

		mOverridingUrl = prefs.getString(OVERRIDING_URL_PREFS_KEY, StringUtils.EMPTY_STRING);
		mShouldStayOpen = prefs.getBoolean(KEEP_OFFERWALL_OPEN_PREFS_KEY, true);
		setValuesInFields();
	}
	
	/**
	 * Sets values in the state of the UI text fields and text boxes.
	 */
	private void setValuesInFields() {
		mOverridingUrlField.setText(mOverridingUrl);
		SponsorPayPublisher.setOverridingWebViewUrl(mOverridingUrl);
		
		mKeepOfferwallOpenCheckBox.setChecked(mShouldStayOpen);

		mSimulateNoPhoneStatePermissionCheckBox.setChecked(ExtendedHostInfo
				.getSimulateNoReadPhoneStatePermission());
		mSimulateNoWifiStatePermissionCheckBox.setChecked(ExtendedHostInfo
				.getSimulateNoAccessWifiStatePermission());
		mSimulateInvalidAndroidIdCheckBox.setChecked(ExtendedHostInfo
				.getSimulateInvalidAndroidId());
		mSimulateNoSerialNumberCheckBox.setChecked(ExtendedHostInfo
				.getSimulateNoHardwareSerialNumber());
	}

	public void appendDefaultParamsToUrlField(View v) {
		fetchValuesFromFields();
		try {
			mOverridingUrl = UrlBuilder.newBuilder(mOverridingUrl, SPSessionManager.getCurrentSession())
					.addExtraKeysValues(mCustomKeyValuesForRequest)
					.buildUrl();
		} catch (RuntimeException e) {
			showCancellableAlertBox("Exception from SDK", e.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(),
					"SponsorPay SDK Exception: ", e);
		}
		setValuesInFields();
	}
	
	//refactor this, copied from SponsorpayAndroidTestAppActivity
	public void showCancellableAlertBox(String title, String text) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(title).setMessage(text).setCancelable(true);
		dialogBuilder.show();
	}
	
	private static class ExtendedHostInfo extends HostInfo {

		public ExtendedHostInfo(Context context) {
			super(context);
		}

		public static boolean getSimulateNoAccessWifiStatePermission(){
			return sSimulateNoAccessWifiStatePermission;
		}
		
		public static boolean getSimulateNoReadPhoneStatePermission(){
			return sSimulateNoReadPhoneStatePermission;
		}
		
		public static boolean getSimulateNoHardwareSerialNumber(){
			return sSimulateNoHardwareSerialNumber;
		}
		
		public static boolean getSimulateInvalidAndroidId(){
			return sSimulateInvalidAndroidId;
		}
	}
	
}
