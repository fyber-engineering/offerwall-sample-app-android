package com.sponsorpay.mediation.mbe;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.hyprmx.android.sdk.HyprMXHelper;
import com.hyprmx.android.sdk.HyprMXPresentation;
import com.hyprmx.android.sdk.api.data.Offer;
import com.hyprmx.android.sdk.api.data.OffersAvailableResponse;
import com.hyprmx.android.sdk.utility.OfferHolder.OnOffersAvailableResponseReceivedListener;
import com.sponsorpay.mediation.HyprMXMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;

public class HyprMXVideoMediationAdapter extends SPBrandEngageMediationAdapter<HyprMXMediationAdapter>
		implements HyprMXHelper.HyprMXListener, OnOffersAvailableResponseReceivedListener {

	private static final String TAG = HyprMXVideoMediationAdapter.class.getSimpleName();

	HyprMXPresentation _presentation = null;
	
	/*
	 * methods of SPBrandEngageMediationAdapter
	 */

	public HyprMXVideoMediationAdapter(HyprMXMediationAdapter adapter) {
		super(adapter);
		
		Log.d(TAG, "creating hypr video adapter");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void videosAvailable(Context context) {
		// TODO Auto-generated method stub
		Log.d(TAG, "videos available method");
		_presentation = new HyprMXPresentation();
		_presentation.prepare(this);
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		Log.d(TAG, "start video method");
		// TODO Auto-generated method stub
		Log.d(TAG, parentActivity.getClass().getCanonicalName());
		parentActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				_presentation.show(parentActivity);
			}
		});
	}

	/*
	 * methods of HyprMXHelper.HyprMXListener
	 */

	@Override
	public void onNoContentAvailable() {
		Log.d(TAG, "onNoContentAvailable method");
		// TODO Auto-generated method stub

	}

	@Override
	public void onOfferCancelled(Offer arg0) {
		Log.d(TAG, "onOfferCancelled method");
		// TODO Auto-generated method stub

	}

	@Override
	public void onOfferCompleted(Offer arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onOfferCompleted method");

	}

	@Override
	public void onUserOptedOut() {
		Log.d(TAG, "onUserOptedOut method");
		// TODO Auto-generated method stub

	}

	/*
	 * OnOffersAvailableResponseReceivedListener
	 */
	@Override
	public void onError(int arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onError method");
		
	}

	@Override
	public void onNoOffersAvailable(OffersAvailableResponse arg0) {
		Log.d(TAG, "onNoOffersAvailable method");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOffersAvailable(OffersAvailableResponse arg0) {
		Log.d(TAG, "onOffersAvailable method");
		// TODO Auto-generated method stub
		//_presentation.show(arg0);
		sendValidationEvent(arg0.getOffersAvailable().size() > 0 ?SPTPNVideoValidationResult.SPTPNValidationSuccess : SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
	}

}
