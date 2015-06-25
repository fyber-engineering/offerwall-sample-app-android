package com.fyber.sampleapp.fragments;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OfferwallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OfferwallFragment extends Fragment {
	private Button offerwallButton;

	private static final int OFFERWALL_REQUEST_CODE = 8795;


	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment OfferwallFragment.
	 */
	public static OfferwallFragment newInstance() {
		OfferwallFragment fragment = new OfferwallFragment();
		return fragment;
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

		FrameLayout offerwallFrameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_offerwall,
				container, false);

		// note that we're looking for a button with id="@+id/myButton" in your inflated layout
		// Naturally, this can be any View; it doesn't have to be a button
		offerwallButton = (Button) offerwallFrameLayout.findViewById(R.id.display_button);
		FyberMainActivity.setButtonColorAndText(offerwallButton, "Show\r\nOffer Wall", getResources().getColor(R.color.buttonColorSuccess));

		offerwallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                // We assume this code is running inside an android.app.Activity subclass
				Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(getActivity(), true);
				startActivityForResult(offerWallIntent, OFFERWALL_REQUEST_CODE);
			}
		});
		// Inflate the layout for this fragment
		return offerwallFrameLayout;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		int fragmentIndex = (requestCode >> 16);
		if (fragmentIndex != 0) {
			// Yes. Pass it on...
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

}
