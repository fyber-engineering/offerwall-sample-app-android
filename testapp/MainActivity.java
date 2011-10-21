/**
 * SponsorPay Android Advertiser Test App
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiser;

/**
 * Main activity of the SponsorPay Advertiser SDK Test App
 * 
 */
public class MainActivity extends Activity {
	private static final String DEFAULT_PROGRAM_ID = "3089";
	private static final int DEFAULT_DELAY_MIN = 15;

	/**
	 * Called when the activity is first created. See {@link Activity}.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		((TextView)findViewById(R.id.sdk_version)).setText("Advertiser SDK v. " + SponsorPayAdvertiser.RELEASE_VERSION_STRING);
		
		startListeningToUseStagingUrlsCheckBox();
	}

	private void startListeningToUseStagingUrlsCheckBox() {
		CheckBox useStagingUrlsCheckBox = (CheckBox) findViewById(R.id.use_staging_urls_checkbox);
		useStagingUrlsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SponsorPayAdvertiser.setShouldUseStagingUrls(isChecked);
			}
		});
	}
	
	private String fetchEnteredProgramId() {
		// Retrieve program ID from EditText on screen
		EditText programIdField = (EditText) findViewById(R.id.program_id_field);
		String programId = programIdField.getText().toString();
		if (programId == null || programId.trim().equals("")) {
			programId = DEFAULT_PROGRAM_ID;
			programIdField.setText(programId);
		}
		return programId;
	}

	private int fetchEnteredDelay() {
		// Retrieve delay from EditText on screen
		EditText delayField = (EditText) findViewById(R.id.delay_field);

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

		return parsedInt;
	}

	public void onSendCallbackNowButtonClick(View v) {
		SponsorPayAdvertiser.register(getApplicationContext(), fetchEnteredProgramId());
	}

	public void onSendCallbackWithDelayButtonClick(View v) {
		SponsorPayAdvertiser.registerWithDelay(getApplicationContext(), fetchEnteredDelay(), fetchEnteredProgramId());
	}
}