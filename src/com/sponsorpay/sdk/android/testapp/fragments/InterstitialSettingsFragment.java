package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.sponsorpay.sdk.android.publisher.InterstitialLoader.InterstitialLoadingStatusListener;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class InterstitialSettingsFragment extends AbstractSettingsFragment {

	// insterstitial
	private static final String SKIN_NAME_PREFS_KEY = "SKIN_NAME";
	private static final String BACKGROUND_URL_PREFS_KEY = "BACKGROUND_URL";

	private EditText mSkinNameField;
	private EditText mBackgroundUrlField;

	private String mBackgroundUrl;
	private String mSkinName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_settings_interstitial, container,
				false);
	}

	@Override
	protected String getFragmentTitle() {
		return getResources().getString(R.string.interstitial);
	}

	@Override
	protected void bindViews() {
		mSkinNameField = (EditText) findViewById(R.id.skin_name_field);
		mBackgroundUrlField = (EditText) findViewById(R.id.background_url_field);
	}

	@Override
	protected void setValuesInFields() {
		mSkinNameField.setText(mSkinName);
		mBackgroundUrlField.setText(mBackgroundUrl);
	}

	@Override
	protected void fetchValuesFromFields() {

		String skinNameValue = mSkinNameField.getText().toString();
		if (!skinNameValue.equals(StringUtils.EMPTY_STRING)) {
			mSkinName = skinNameValue;
		}

		mBackgroundUrl = mBackgroundUrlField.getText().toString();

	}

	@Override
	protected void readPreferences(SharedPreferences prefs) {
		mBackgroundUrl = prefs.getString(BACKGROUND_URL_PREFS_KEY,
				StringUtils.EMPTY_STRING);
		mSkinName = prefs.getString(SKIN_NAME_PREFS_KEY,
				StringUtils.EMPTY_STRING);
	}

	@Override
	protected void storePreferences(Editor prefsEditor) {
		prefsEditor.putString(BACKGROUND_URL_PREFS_KEY, mBackgroundUrl);
		prefsEditor.putString(SKIN_NAME_PREFS_KEY, mSkinName);
	}

	public void launchInsterstitial(String userId, Boolean shouldStayOpen,
			String currencyName, String overridingAppId) {
		fetchValuesFromFields();
		try {

			SponsorPayPublisher.loadShowInterstitial(
					getActivity(),
					userId,
					new InterstitialLoadingStatusListener() {

						@Override
						public void onInterstitialLoadingTimeOut() {
							Log.d(SponsorpayAndroidTestAppActivity.class
									.toString(), "onInterstitialLoadingTimeOut");
						}

						@Override
						public void onInterstitialRequestError() {
							Log.d(SponsorpayAndroidTestAppActivity.class
									.toString(), "onInterstitialRequestError");
						}

						@Override
						public void onNoInterstitialAvailable() {
							Log.d(SponsorpayAndroidTestAppActivity.class
									.toString(), "onNoInterstitialAvailable");
						}

						@Override
						public void onWillShowInterstitial() {
							Log.d(SponsorpayAndroidTestAppActivity.class
									.toString(), "onWillShowInterstitial");
						}

					}, shouldStayOpen, mBackgroundUrl, mSkinName, 0,
					currencyName, overridingAppId);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(),
					"SponsorPay SDK Exception: ", ex);
		}
	}

}
