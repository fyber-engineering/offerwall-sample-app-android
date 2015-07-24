package com.fyber.sampleapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.requesters.InterstitialRequester;
import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.sampleapp.MainActivity;
import com.fyber.sampleapp.R;
import com.fyber.utils.FyberLogger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InterstitialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InterstitialFragment extends Fragment implements RequestCallback {

	public static final String SHOW_INTERSTITIAL = "Show\r\nInterstitial";
	public static final String REQUEST_INTERSTITIALS = "Request\r\nInterstitials";
	public static final String GETTING_OFFERS = "Getting\r\nOffers";
	private static final String TAG = InterstitialFragment.class.getSimpleName();
	private Intent interstitialIntent;
	private static final int INTERSTITIAL_REQUEST_CODE = 8792;

	@Bind(R.id.interstitial_button) Button interstitialButton;

	//FIXME: since it is mandatory to have public constructor on a fragment is it worth it to have this new instance method
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment InterstitialFragment.
	 */
	public static InterstitialFragment newInstance() {
		return new InterstitialFragment();
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
		View view = inflater.inflate(R.layout.fragment_interstitial, container, false);
		ButterKnife.bind(this, view);

		if (interstitialIntent != null) {
			MainActivity.setButtonColorAndText(interstitialButton, SHOW_INTERSTITIAL, getResources().getColor(R.color.buttonColorSuccess));
		}

		return view;

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case INTERSTITIAL_REQUEST_CODE:
					interstitialIntent = null;
					MainActivity.setButtonColorAndText(interstitialButton, SHOW_INTERSTITIAL, getResources().getColor(R.color.colorPrimary));
					break;
				default:
					break;
			}
		}
	}


	@OnClick(R.id.interstitial_button)
	public void onInterterstitialButtonCLicked(View view) {
		if (interstitialIntent != null) {
			startActivityForResult(interstitialIntent, INTERSTITIAL_REQUEST_CODE);
			interstitialIntent = null;
			MainActivity.setButtonColorAndText(interstitialButton, REQUEST_INTERSTITIALS, getResources().getColor(R.color.colorPrimary));
		} else {
			interstitialButton.startAnimation(MainActivity.getClockwiseAnimation());
			interstitialButton.setText(GETTING_OFFERS);

			//request an interstitial ad.
			InterstitialRequester
					.create(this)
					.request(getActivity());
		}
	}

	// ** RequestCallback methods **

	@Override
	public void onAdAvailable(Intent intent) {
		interstitialButton.startAnimation(MainActivity.getClockwiseAnimation());
		MainActivity.setButtonColorAndText(interstitialButton, SHOW_INTERSTITIAL, getResources().getColor(R.color.buttonColorSuccess));
		this.interstitialIntent = intent;
	}

	@Override
	public void onAdNotAvailable() {
		FyberLogger.d(TAG, "no ad available");
		resetInterstitialState();
	}

	@Override
	public void onRequestError(RequestError requestError) {
		FyberLogger.d(TAG, "error requesting ad: " + requestError.getDescription());
		resetInterstitialState();
	}

	private void resetInterstitialState() {
		interstitialIntent = null;
		interstitialButton.startAnimation(MainActivity.getReverseClockwiseAnimation());
		interstitialButton.setText(REQUEST_INTERSTITIALS);
	}


}