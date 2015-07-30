package com.fyber.sampleapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fyber.requesters.OfferWallRequester;
import com.fyber.sampleapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OfferwallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OfferwallFragment extends FyberFragment {
	private static final String TAG = OfferwallFragment.class.getSimpleName();

	@Bind(R.id.offer_wall_button) Button offerwallButton;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_offerwall,
				container, false);
		ButterKnife.bind(this, view);

		setButtonToSuccessState();

		return view;
	}

	@OnClick(R.id.offer_wall_button)
	public void onOfferWallButtonCLicked(View view) {

		requestOrShowAd();
	}

	@Override
	protected void performRequest() {
		//FIXME: should the comment be different here?
		//Unless the device is not supported, OfferWallRequester will always return an intent.
		//However, for consistency reason Fyber sdk uses the same callback as other ad formats

		//Requesting the offer wall
		OfferWallRequester
				.create(this)
				.request(getActivity());
	}

	@Override
	public String getLogTag() {
		return TAG;
	}

	@Override
	public String getRequestText() {
		return getString(R.string.showOfferWall);
	}

	@Override
	public String getShowText() {
		return getString(R.string.showOfferWall);
	}

	@Override
	public Button getButton() {
		return offerwallButton;
	}

	@Override
	protected int getRequestCode() {
		return OFFERWALL_REQUEST_CODE;
	}

	/*
	* ** Offer wall specific state methods **
	 */

	@Override
	protected boolean isRequestingState() {
		//offer wall is never in the requesting sate. It is always ready to show.
		return false;
	}

	@Override
	protected void setButtonToRequestingMode() {
		//do nothing: there is only one state
	}

	@Override
	protected void setButtonToOriginalState() {
		//do nothing: there is only one state
	}
}
