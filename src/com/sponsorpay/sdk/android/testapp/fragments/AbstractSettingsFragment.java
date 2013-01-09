package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;

public abstract class AbstractSettingsFragment extends Fragment {

	protected Button mBackButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final FragmentActivity activity = getActivity();
		View view = activity.findViewById(R.id.backButton);
		if (view != null && view instanceof Button) {
			mBackButton = (Button) view;
			mBackButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getFragmentManager().popBackStack();
				}
			});
		}
		view = activity.findViewById(R.id.fragmentTitle);
		if (view != null && view instanceof TextView) {
			((TextView) view).setText(getFragmentTitle());
		}
		bindViews();
	}

	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences prefs = ((SponsorpayAndroidTestAppActivity) getActivity())
				.getPrefsStore();
		readPreferences(prefs);

		setValuesInFields();
	}

	@Override
	public void onPause() {
		// Save the state of the UI fields into the app preferences.
		fetchValuesFromFields();

		SharedPreferences prefs = ((SponsorpayAndroidTestAppActivity) getActivity())
				.getPrefsStore();
		Editor prefsEditor = prefs.edit();

		storePreferences(prefsEditor);

		prefsEditor.commit();

		super.onPause();
	}

	protected View findViewById(int viewId) {
		return getActivity().findViewById(viewId);
	}

	protected void showCancellableAlertBox(String title, String text) {
		((SponsorpayAndroidTestAppActivity) getActivity())
				.showCancellableAlertBox(title, text);
	}

	protected Context getApplicationContext() {
		return getActivity().getApplicationContext();
	}

	protected abstract void setValuesInFields();

	protected abstract String getFragmentTitle();

	protected abstract void bindViews();

	protected abstract void fetchValuesFromFields();

	protected abstract void readPreferences(SharedPreferences prefs);

	protected abstract void storePreferences(Editor prefsEditor);

}
