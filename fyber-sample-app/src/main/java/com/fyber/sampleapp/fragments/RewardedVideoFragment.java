package com.fyber.sampleapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.currency.VirtualCurrencyErrorResponse;
import com.fyber.currency.VirtualCurrencyResponse;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.RewardedVideoRequester;
import com.fyber.requesters.VirtualCurrencyCallback;
import com.fyber.requesters.VirtualCurrencyRequester;
import com.fyber.sampleapp.R;
import com.fyber.utils.FyberLogger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RewardedVideoFragment extends FyberFragment implements VirtualCurrencyCallback {

	private static final String TAG = "RewardedVideoFragment";

	@Bind(R.id.rewarded_video_button) Button rewardedVideoButton;

	public RewardedVideoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_rewarded_video,
				container, false);
		ButterKnife.bind(this, view);

		if (isIntentAvailable()) {
			setButtonToSuccessState();
		}

		return view;
	}

	// using butter knife to link Button click
	@OnClick(R.id.rewarded_video_button)
	public void onRewardedVideoButtonCLicked(View view) {

		requestOrShowAd();
	}

	/*
	* ** Code to perform a Rewarded Video request **
	*/

	@Override
	protected void performRequest() {
		//Requesting a rewarded video ad
		RewardedVideoRequester
				.create(this)
				// you can add a virtual Currency Requester by chaining this extra method
				.withVirtualCurrencyRequester(getVirtualCurrencyRequester())
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
		return getString(R.string.request_video);
	}

	@Override
	public String getShowText() {
		return getString(R.string.show_video);
	}

	@Override
	public Button getButton() {
		return rewardedVideoButton;
	}

	@Override
	protected int getRequestCode() {
		return REWARDED_VIDEO_REQUEST_CODE;
	}

	// creates a new virtual currency requester to be used in a rewarded video request or on a separate virtual currency request.

	private VirtualCurrencyRequester getVirtualCurrencyRequester() {
		return VirtualCurrencyRequester.create(this)
				//
//				.notifyUserOnReward(true)
				/* this is the currency id for RV ad format
				 you can refer to this -- link to doc*/
//				.forCurrencyId("coins")
				;
	}

	@Override
	public void onError(VirtualCurrencyErrorResponse virtualCurrencyErrorResponse) {
		FyberLogger.d(TAG, "VCS error received - " + virtualCurrencyErrorResponse.getErrorMessage());
	}

	@Override
	public void onSuccess(VirtualCurrencyResponse virtualCurrencyResponse) {
		FyberLogger.d(TAG, "VCS coins received - " + virtualCurrencyResponse.getDeltaOfCoins());
	}

	@Override
	public void onRequestError(RequestError requestError) {
		FyberLogger.d(TAG, "error requesting vcs: " + requestError.getDescription());
	}


}
