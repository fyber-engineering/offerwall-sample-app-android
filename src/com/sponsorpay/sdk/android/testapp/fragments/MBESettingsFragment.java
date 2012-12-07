package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClient;
import com.sponsorpay.sdk.android.testapp.R;

public class MBESettingsFragment extends AbstractSettingsFragment {


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
	}

	@Override
	protected void bindViews() {
	}

	@Override
	protected void fetchValuesFromFields() {
	}

	@Override
	protected void readPreferences(SharedPreferences prefs) {
	}

	@Override
	protected void storePreferences(Editor prefsEditor) {
	}

	public void requestOffers() {
		SPBrandEngageClient.INSTANCE.requestOffers(SponsorPay.getCurrentCredentials(),
				getActivity(), (ViewGroup)findViewById(R.id.mbe_linear_layout));
	}
	
	public void startEngament() {
		if (SPBrandEngageClient.INSTANCE.canStartEngagement()) {
			Intent intent = new Intent(getActivity().getApplicationContext(), SPBrandEngageClient.class);
			startActivity(intent);
		}
	}

	
}
