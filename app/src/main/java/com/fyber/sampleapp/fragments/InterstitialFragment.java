package com.fyber.sampleapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.fyber.sampleapp.FyberMainActivity;
import com.fyber.sampleapp.R;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.interstitial.SPInterstitialRequestListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InterstitialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InterstitialFragment extends Fragment implements SPInterstitialRequestListener {
	private Intent mIntent;

	private Button interstitialButton;
	private static final int INTERSTITIAL_REQUEST_CODE = 8792;


	public static InterstitialFragment newInstance() {
		InterstitialFragment interstitialFragment = new InterstitialFragment();
		return interstitialFragment;
	}


	public InterstitialFragment() {
		// Required empty public constructor
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Do not create a new Fragment when the Activity is re-created such as orientation changes.
		setRetainInstance(true);
	}


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		FrameLayout interstitialFrameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_interstitial, container, false);
		interstitialButton = (Button) interstitialFrameLayout.findViewById(R.id.intertstial_start_button);
		interstitialButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mIntent != null) {
					startActivityForResult(mIntent, INTERSTITIAL_REQUEST_CODE);
					FyberMainActivity.setButtonColorAndText(interstitialButton, "Request\r\nInterstitials", getResources().getColor(R.color.colorPrimary));
				} else {
					interstitialButton.startAnimation(FyberMainActivity.getAnimation());
					interstitialButton.setText("Getting\r\nOffers");
					SponsorPayPublisher.getIntentForInterstitialActivity(getActivity(), InterstitialFragment.this);
				}
			}


		});

		if (mIntent != null) {
			FyberMainActivity.setButtonColorAndText(interstitialButton, "Show\r\nVideo", getResources().getColor(R.color.buttonColorSuccess));
		}

		return interstitialFrameLayout;

	}


	public void onSPInterstitialAdAvailable(Intent intent) {
		interstitialButton.startAnimation(FyberMainActivity.getAnimation());
		FyberMainActivity.setButtonColorAndText(interstitialButton, "Show\r\nInterstitial", getResources().getColor(R.color.buttonColorSuccess));
		mIntent = intent;
	}


	public void onSPInterstitialAdNotAvailable() {
		mIntent = null;
		interstitialButton.startAnimation(FyberMainActivity.getReverseAnimation());
		interstitialButton.setText("Request\r\nInterstitial");
	}


	public void onSPInterstitialAdError(String s) {
		mIntent = null;
		interstitialButton.startAnimation(FyberMainActivity.getReverseAnimation());
		interstitialButton.setText("Request\r\nInterstitial");
	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case INTERSTITIAL_REQUEST_CODE:
					FyberMainActivity.setButtonColorAndText(interstitialButton, "Request\r\nInterstitial", getResources().getColor(R.color.colorPrimary));
					mIntent = null;
					break;
				default:
					break;
			}
		}
	}


}