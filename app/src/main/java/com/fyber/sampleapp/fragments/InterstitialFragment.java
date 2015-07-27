package com.fyber.sampleapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.requesters.InterstitialRequester;
import com.fyber.sampleapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InterstitialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InterstitialFragment extends FyberFragment {

	private static final String TAG = InterstitialFragment.class.getSimpleName();

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

		if (isIntentAvailable()) {
			setButtonToSuccessState();
		}

		return view;

	}

	@OnClick(R.id.interstitial_button)
	public void onInterterstitialButtonCLicked(View view) {
		if ( !isRequestingState()) {
			if (isIntentAvailable()) {
				startActivityForResult(intent, INTERSTITIAL_REQUEST_CODE);
				setButtonToOriginalState();
				resetIntent();
			} else {
				setButtonToRequestingMode();

				//request an interstitial ad.
				InterstitialRequester
						.create(this)
						.request(getActivity());
			}
		}
	}

	@Override
	public String getLogTag() {
		return TAG;
	}

	@Override
	public String getRequestText() {
		return getString(R.string.requestInterstitial);
	}

	@Override
	public String getShowText() {
		return getString(R.string.showInterstitial);
	}

	@Override
	public Button getButton() {
		return interstitialButton;
	}

}