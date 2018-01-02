package com.fyber.sampleapp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.requesters.InterstitialRequester;
import com.fyber.sampleapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InterstitialFragment extends FyberFragment {

	private static final String TAG = InterstitialFragment.class.getSimpleName();

	@BindView(R.id.interstitial_button) Button interstitialButton;

	public InterstitialFragment() {
	}

	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_interstitial, container, false);
		ButterKnife.bind(this, view);

		if (isIntentAvailable()) {
			setButtonToSuccessState();
		}
		return view;
	}

	// using butter knife to link Button click
	@OnClick(R.id.interstitial_button)
	public void onInterstitialButtonClicked(View view) {
		requestOrShowAd();
	}

	/*
	* ** Code to perform an Interstitial ad request **
	*/

	@Override
	protected void performRequest() {
		//request an interstitial ad.
		InterstitialRequester
				.create(this)
				.request(getActivity());
	}

	/*
	* ** FyberFragment methods **
	*/

	@Override
	public String getLogTag() {
		return TAG;
	}

	@Override
	public String getRequestText() {
		return getString(R.string.request_interstitial);
	}

	@Override
	public String getShowText() {
		return getString(R.string.show_interstitial);
	}

	@Override
	public Button getButton() {
		return interstitialButton;
	}

	@Override
	protected int getRequestCode() {
		return INTERSTITIAL_REQUEST_CODE;
	}

}