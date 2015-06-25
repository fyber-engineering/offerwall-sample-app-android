package com.fyber.sampleapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.fyber.sampleapp.FyberMainActivity;
import com.fyber.sampleapp.R;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.currency.SPCurrencyServerErrorResponse;
import com.sponsorpay.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.publisher.currency.SPCurrencyServerSuccessfulResponse;
import com.sponsorpay.publisher.mbe.SPBrandEngageRequestListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RewardedVideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RewardedVideoFragment extends Fragment implements SPBrandEngageRequestListener, SPCurrencyServerListener {

	private static final String TAG = "RewardedVideoFragment";

	private Intent mIntent;
	private Button rewardedVideoButton;
	private static final int REWARDED_VIDEO_REQUEST_CODE = 8796;

	private FrameLayout rewardedVideoFrameLayout;


	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment RewardedVideoFragment.
	 */
	public static RewardedVideoFragment newInstance() {
		RewardedVideoFragment fragment = new RewardedVideoFragment();
		return fragment;
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

		rewardedVideoFrameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_rewarded_video,
				container, false);

		// note that we're looking for a button with id="@+id/myButton" in your inflated layout
		// Naturally, this can be any View; it doesn't have to be a button
		rewardedVideoButton = (Button) rewardedVideoFrameLayout.findViewById(R.id.play_button);

		rewardedVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// We assume this code is running inside an android.app.Activity subclass
				if (mIntent != null) {
					startActivityForResult(mIntent, REWARDED_VIDEO_REQUEST_CODE);
					mIntent = null;
					FyberMainActivity.setButtonColorAndText(rewardedVideoButton, "Request\r\nVideo", getResources().getColor(R.color.colorPrimary));
				} else {
					rewardedVideoButton.startAnimation(FyberMainActivity.getAnimation());
					rewardedVideoButton.setText("Getting\r\nOffers");
					SponsorPayPublisher.getIntentForMBEActivity(getActivity(), RewardedVideoFragment.this, RewardedVideoFragment.this);
				}
			}
		});

		if (mIntent != null) {
			FyberMainActivity.setButtonColorAndText(rewardedVideoButton, "Show\r\nVideo", getResources().getColor(R.color.buttonColorSuccess));
		}


		// Inflate the layout for this fragment
		return rewardedVideoFrameLayout;
	}

	@Override
	public void onSPBrandEngageOffersAvailable(Intent spBrandEngageActivity) {
		Log.d(TAG, "SPBrandEngage - intent available");

		rewardedVideoButton.startAnimation(FyberMainActivity.getAnimation());
		FyberMainActivity.setButtonColorAndText(rewardedVideoButton, "Show\r\nVideo", getResources().getColor(R.color.buttonColorSuccess));
		mIntent = spBrandEngageActivity;
	}

	@Override
	public void onSPBrandEngageOffersNotAvailable() {
		Log.d(TAG, "SPBrandEngage - no offers for the moment");
		mIntent = null;
		rewardedVideoButton.startAnimation(FyberMainActivity.getReverseAnimation());
		rewardedVideoButton.setText("Request\r\nVideo");
	}

	@Override
	public void onSPBrandEngageError(String errorMessage) {
		Log.d(TAG, "SPBrandEngage - an error occurred:\n" + errorMessage);
		mIntent = null;
		rewardedVideoButton.startAnimation(FyberMainActivity.getReverseAnimation());
		rewardedVideoButton.setText("Request\r\nVideo");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REWARDED_VIDEO_REQUEST_CODE:
					FyberMainActivity.setButtonColorAndText(rewardedVideoButton, "Request\r\nVideo", getResources().getColor(R.color.colorPrimary));
					mIntent = null;
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onSPCurrencyServerError(SPCurrencyServerErrorResponse response) {
		Log.e(TAG, "VCS error received - " + response.getErrorMessage());
	}

	@Override
	public void onSPCurrencyDeltaReceived(SPCurrencyServerSuccessfulResponse response) {
		Log.d(TAG, "VCS coins received - " + response.getDeltaOfCoins());
	}

}
