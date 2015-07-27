package com.fyber.sampleapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RewardedVideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RewardedVideoFragment extends FyberFragment implements RequestCallback, VirtualCurrencyCallback {

	private static final String TAG = "RewardedVideoFragment";

	@Bind(R.id.rewarded_video_button) Button rewardedVideoButton;

	//FIXME: since it is mandatory to have public constructor on a fragment is it worth it to have this new instance method

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment RewardedVideoFragment.
	 */
	public static RewardedVideoFragment newInstance() {
		return new RewardedVideoFragment();
	}

	public RewardedVideoFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Do not create a new Fragment when the Activity is re-created such as orientation changes.
		setRetainInstance(true);
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

	@OnClick(R.id.rewarded_video_button)
	public void onRewardedVideoButtonCLicked(View view) {
		if (!isRequestingState()) {
			//if we already have an intent, we start the video activity and reset the button
			if (isIntentAvailable()) {
				startActivityForResult(intent, REWARDED_VIDEO_REQUEST_CODE);
				setButtonToOriginalState();
				resetIntent();
			} else {
				setButtonToRequestingMode();

				//Requesting a rewarded video ad
				RewardedVideoRequester
						.create(this)
						.withVirtualCurrencyCallback(this) // you can add a vcs listener by chaining this extra method
						.request(getActivity());

				//FIXME: is it worth to add a link to the dev portal? (http://developer.fyber.com/content/android/basics/rewarding-the-user/vcs/) or something?
			/*
			* If you do not chain a vcs callback in the rewarded video request you can always make a separate call for virtual currency.
			* comment the 'withVirtualCurrencyCallback' line and uncomment the 'requestVirtualCurrency()' on 'onActivityResult'
			* Have a look at the commented method 'requestVirtualCurrency'
			*/
			}
		}
	}

	@Override
	public String getLogTag() {
		return TAG;
	}

	@Override
	public String getRequestText() {
		return getString(R.string.requestVideo);
	}

	@Override
	public String getShowText() {
		return getString(R.string.showVideo);
	}

	@Override
	public Button getButton() {
		return rewardedVideoButton;
	}

	// ** VCS listener **

	@Override
	public void onError(VirtualCurrencyErrorResponse response) {
		FyberLogger.d(TAG, "VCS error received - " + response.getErrorMessage());
	}

	@Override
	public void onSuccess(VirtualCurrencyResponse response) {
		FyberLogger.d(TAG, "VCS coins received - " + response.getDeltaOfCoins());
	}

	/*
	 * ** separate VCS request **
	 * Note that you will only have a successful response querying for virtual currency after watching a rewarded video.
	 * Uncomment this code and call this method from 'onActivityResult'
	 *
	  */

//	public static void requestVirtualCurrency() {
//
//		VirtualCurrencyRequester.create(new VirtualCurrencyCallback() {
//			@Override
//			public void onError(VirtualCurrencyErrorResponse virtualCurrencyErrorResponse) {
//				FyberLogger.d(TAG, "VCS error received - " + virtualCurrencyErrorResponse.getErrorMessage());
//			}
//
//			@Override
//			public void onSuccess(VirtualCurrencyResponse virtualCurrencyResponse) {
//				FyberLogger.d(TAG, "VCS coins received - " + virtualCurrencyResponse.getDeltaOfCoins());
//			}
//
//			@Override
//			public void onRequestError(RequestError requestError) {
//				FyberLogger.d(TAG, "error requesting vcs: " + requestError.getDescription());
//			}
//		});
//	}

}
