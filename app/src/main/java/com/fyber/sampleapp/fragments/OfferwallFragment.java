package com.fyber.sampleapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.requesters.OfferWallRequester;
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
 * Use the {@link OfferwallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OfferwallFragment extends Fragment implements RequestCallback {
	private static final String TAG = OfferwallFragment.class.getSimpleName();

	public static final String SHOW_OFFER_WALL = "Show\r\nOffer Wall";

	@Bind(R.id.offer_wall_button) Button offerwallButton;

	private static final int OFFERWALL_REQUEST_CODE = 8795;
	private Intent offerWallIntent;


	//FIXME: since it is mandatory to have public constructor on a fragment is it worth it to have this new instance method
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment OfferwallFragment.
	 */
	public static OfferwallFragment newInstance() {
		return new OfferwallFragment();
	}

	public OfferwallFragment() {
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
		View view = inflater.inflate(R.layout.fragment_offerwall,
				container, false);
		ButterKnife.bind(this, view);

		MainActivity.setButtonColorAndText(offerwallButton, SHOW_OFFER_WALL, getResources().getColor(R.color.buttonColorSuccess));

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		int fragmentIndex = (requestCode >> 16);
		if (fragmentIndex != 0) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

		@OnClick(R.id.offer_wall_button)
		public void onOfferWallButtonCLicked(View view) {
		if (offerWallIntent != null) {
			startActivityForResult(offerWallIntent, OFFERWALL_REQUEST_CODE);
		} else {
			//FIXME: should the comment be different here?
			//unless the device is not supported OfferWallRequester will always return an intent. However, for consistency reason we use the same callback as for other ad formats
			//Requesting an offer ad
			OfferWallRequester
					.create(this)
					.request(getActivity());
		}
	}

	// ** RequestCallback methods **

	@Override
	public void onAdAvailable(Intent intent) {
		offerwallButton.startAnimation(MainActivity.getClockwiseAnimation());
		MainActivity.setButtonColorAndText(offerwallButton, SHOW_OFFER_WALL, getResources().getColor(R.color.buttonColorSuccess));
		this.offerWallIntent = intent;
		startActivityForResult(offerWallIntent, OFFERWALL_REQUEST_CODE);

	}

	//FIXME: validate button sate for offer wall
	@Override
	public void onAdNotAvailable() {
		FyberLogger.d(TAG, "no ad available");
		resetOfferWallState();
	}

	@Override
	public void onRequestError(RequestError requestError) {
		FyberLogger.d(TAG, "error requesting ad: " + requestError.getDescription());
		resetOfferWallState();
	}

	private void resetOfferWallState() {
		offerWallIntent = null;
		offerwallButton.startAnimation(MainActivity.getReverseClockwiseAnimation());
		offerwallButton.setText(SHOW_OFFER_WALL);
	}

}
