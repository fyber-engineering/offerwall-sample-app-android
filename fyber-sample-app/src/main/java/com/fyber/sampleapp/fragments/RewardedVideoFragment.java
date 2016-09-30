package com.fyber.sampleapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.ads.videos.RewardedVideoActivity;
import com.fyber.currency.VirtualCurrencyErrorResponse;
import com.fyber.currency.VirtualCurrencyResponse;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.RewardedVideoRequester;
import com.fyber.requesters.VirtualCurrencyCallback;
import com.fyber.requesters.VirtualCurrencyRequester;
import com.fyber.sampleapp.R;
import com.fyber.utils.FyberLogger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RewardedVideoFragment extends FyberFragment implements VirtualCurrencyCallback {

	private static final String TAG = "RewardedVideoFragment";

	@BindView(R.id.rewarded_video_button) Button rewardedVideoButton;

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
	* Checking activity result for rewarded video engagement status
	 */

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == REWARDED_VIDEO_REQUEST_CODE) {
			String engagementStatus = data.getStringExtra(RewardedVideoActivity.ENGAGEMENT_STATUS);
				switch (engagementStatus) {
				case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_FINISHED_VALUE:
					FyberLogger.i(TAG, "The video has finished after completing. The user will be rewarded.");
					break;
				case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_ABORTED_VALUE:
					FyberLogger.i(TAG, "The video has finished before completing. The user might have aborted it, either explicitly (by tapping the close button) or implicitly (by switching to another app) or it was interrupted by an asynchronous event like an incoming phone call.");
					break;
				case RewardedVideoActivity.REQUEST_STATUS_PARAMETER_ERROR:
					FyberLogger.i(TAG, "The video was interrupted or failed to play due to an error.");
					break;
				default:
					break;
			}
		}
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
				// user will get a toast notification upon reward
//				.notifyUserOnReward(true)
//				this is the currency id for RV ad format
//				 you can refer to this -- http://developer.fyber.com/content/android/basics/rewarding-the-user/vcs/
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
