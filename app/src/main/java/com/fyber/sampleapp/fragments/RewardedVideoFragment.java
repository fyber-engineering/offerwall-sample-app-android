package com.fyber.sampleapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.currency.VirtualCurrencyErrorResponse;
import com.fyber.currency.VirtualCurrencyResponse;
import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.RewardedVideoRequester;
import com.fyber.requesters.VirtualCurrencyCallback;
import com.fyber.requesters.VirtualCurrencyRequester;
import com.fyber.sampleapp.R;
import com.fyber.utils.FyberLogger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RewardedVideoFragment extends FyberFragment implements RequestCallback {

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REWARDED_VIDEO_REQUEST_CODE:
					// If you did not chain a vcs callback in the rewarded video request, you can uncomment the line below and the respective method to make a separate virtual currency request.
//					requestVirtualCurrencyWithDelay();
				default:
					break;
			}
		}
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
				.withVirtualCurrencyRequester(getVcsRequester()) // you can add a virtual currency listener listener by chaining this extra method
				.request(getActivity());

			/*
			* If you do not chain a vcs callback in the rewarded video request you can always make a separate call for virtual currency.
			* Comment the 'withVirtualCurrencyCallback' line above and uncomment the 'requestVirtualCurrency()' on 'onActivityResult'
			* Have a look at the commented method 'requestVirtualCurrency' at the end of this class.
			*/
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

	private VirtualCurrencyRequester getVcsRequester() {
		VirtualCurrencyRequester vcsRequester = VirtualCurrencyRequester.create(new VirtualCurrencyCallback() {
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
		});
		return vcsRequester;
	}

	/*
	 * ** separate VCS request **
	 *
	 * Note that you will only have a successful response querying for virtual currency after watching a rewarded video.
	 * Also it is recommended to provide a 3 second delay before requesting virtual currency if you are calling the method from 'onActivityResult'
	 *
	  */


	//we run the virtual currency request with a delay of 3 seconds to make sure that the reward has been paid after watching the video.
	private void requestVirtualCurrencyWithDelay() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				requestVirtualCurrency();
			}
		}, 3000);
	}

	public void requestVirtualCurrency() {
		getVcsRequester().request(getActivity());
	}

}
