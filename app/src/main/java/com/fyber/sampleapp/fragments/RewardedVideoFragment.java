package com.fyber.sampleapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.fyber.sampleapp.MainActivity;
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
public class RewardedVideoFragment extends Fragment implements RequestCallback, VirtualCurrencyCallback {

	private static final String TAG = "RewardedVideoFragment";
	public static final String SHOW_VIDEO = "Show\r\nVideo";
	public static final String REQUEST_VIDEO = "Request\r\nVideo";
	public static final String GETTING_OFFERS = "Getting\r\nOffers";

	private Intent rewardedVideoIntent;
	@Bind(R.id.rewarded_video_button) Button rewardedVideoButton;
	private static final int REWARDED_VIDEO_REQUEST_CODE = 8796;

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

		if (rewardedVideoIntent != null) {
			MainActivity.setButtonColorAndText(rewardedVideoButton, SHOW_VIDEO, getResources().getColor(R.color.buttonColorSuccess));
		}

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REWARDED_VIDEO_REQUEST_CODE:
					rewardedVideoIntent = null;
					MainActivity.setButtonColorAndText(rewardedVideoButton, REQUEST_VIDEO,
							getResources().getColor(R.color.colorPrimary));
					// If you did not chain a vcs callback in the rewarded video request, you can uncomment the line below and the respective method to make a separate vcs request.
//					requestVirtualCurrency();
					break;
				default:
					break;
			}
		}
	}

	@OnClick(R.id.rewarded_video_button)
	public void onRewardedVideoButtonCLicked(View view) {
		// We assume this code is running inside an android.app.Activity subclass
		if (rewardedVideoIntent != null) {
			startActivityForResult(rewardedVideoIntent, REWARDED_VIDEO_REQUEST_CODE);
			rewardedVideoIntent = null;
			MainActivity.setButtonColorAndText(rewardedVideoButton, REQUEST_VIDEO,
					getResources().getColor(R.color.colorPrimary));
		} else {
			rewardedVideoButton.startAnimation(MainActivity.getClockwiseAnimation());
			rewardedVideoButton.setText(GETTING_OFFERS);
			//Requesting a rewarded video ad
			RewardedVideoRequester
					.create(this)
					.withVirtualCurrencyCallback(this) // you can add a vcs listener by chaining an extra method
					.request(getActivity());

			//FIXME: is it worth to add a link to the dev portal? (http://developer.fyber.com/content/android/basics/rewarding-the-user/vcs/) or something?
			/*
			* If you do not chain a vcs callback in the rewarded video request you can always make a separate call for virtual currency.
			* comment the 'withVirtualCurrencyCallback' line and uncomment the 'requestVirtualCurrency()' on 'onActivityResult'
			* Have a look at the commented method 'requestVirtualCurrency'
			*/
		}
	}

	// ** RequestCallback methods **

	@Override
	public void onAdAvailable(Intent intent) {
		rewardedVideoButton.startAnimation(MainActivity.getClockwiseAnimation());
		MainActivity.setButtonColorAndText(rewardedVideoButton, SHOW_VIDEO, getResources().getColor(R.color.buttonColorSuccess));
		rewardedVideoIntent = intent;
	}

	@Override
	public void onAdNotAvailable() {
		FyberLogger.d(TAG, "no ad available");
		resetOfferWallState();
	}

	@Override
	public void onRequestError(RequestError requestError) {
		//FIXME: this is ambiguous. It can either be a video request error or a vcs request error. Should we move to anonymous callbacks?
		FyberLogger.d(TAG, "error requesting ad: " + requestError.getDescription());
		resetOfferWallState();
	}

	private void resetOfferWallState() {
		rewardedVideoIntent = null;
		rewardedVideoButton.startAnimation(MainActivity.getReverseClockwiseAnimation());
		rewardedVideoButton.setText(REQUEST_VIDEO);
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
	 * Note that you will only have a successful response when querying for virtual currency after watching a rewarded video.
	 * Uncomment this code and call this method after watching the video
	  */

//	public void requestVirtualCurrency() {
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
