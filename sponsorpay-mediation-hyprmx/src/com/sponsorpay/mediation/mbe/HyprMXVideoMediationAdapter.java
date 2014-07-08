package com.sponsorpay.mediation.mbe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hyprmx.android.sdk.HyprMXHelper;
import com.hyprmx.android.sdk.HyprMXPresentation;
import com.hyprmx.android.sdk.api.data.Offer;
import com.hyprmx.android.sdk.api.data.OffersAvailableResponse;
import com.hyprmx.android.sdk.utility.OfferHolder.OnOffersAvailableResponseReceivedListener;
import com.sponsorpay.mediation.HyprMXMediationAdapter;
import com.sponsorpay.mediation.helper.HyprMXVideoAdapterHelper;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.utils.SponsorPayLogger;

public class HyprMXVideoMediationAdapter extends SPBrandEngageMediationAdapter<HyprMXMediationAdapter>
		implements HyprMXHelper.HyprMXListener, OnOffersAvailableResponseReceivedListener {

	private static final String TAG = "HyprMXVideoMediationAdapter";

	private HyprMXPresentation mPresentation = null;

	/*
	 * methods of SPBrandEngageMediationAdapter
	 */

	public HyprMXVideoMediationAdapter(HyprMXMediationAdapter adapter) {
		super(adapter);
		HyprMXVideoAdapterHelper.setHyprMXVideoMediationAdapter(this);
		SponsorPayLogger.d(TAG, "creating hypr video adapter");
	}

	@Override
	public void videosAvailable(Context context) {
		SponsorPayLogger.d(TAG, "videos available method");
		mPresentation = new HyprMXPresentation();
		mPresentation.prepare(this);
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		SponsorPayLogger.d(TAG, "start video method");
		SponsorPayLogger.d(TAG, parentActivity.getClass().getCanonicalName());
		Intent intent = new Intent(parentActivity, HyprMXVideoActivity.class);
		parentActivity.startActivity(intent);
		notifyVideoStarted();
	}

	/*
	 * methods of HyprMXHelper.HyprMXListener
	 */

	@Override
	public void onNoContentAvailable() {
		SponsorPayLogger.d(TAG, "onNoContentAvailable method");
		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventNoVideo);
	}

	@Override
	public void onOfferCancelled(Offer arg0) {
		SponsorPayLogger.d(TAG, "onOfferCancelled method");
		mPresentation = null;
		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventAborted);
	}

	@Override
	public void onOfferCompleted(Offer arg0) {
		SponsorPayLogger.d(TAG, "onOfferCompleted method");
		mPresentation = null;
		setVideoPlayed();
		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventFinished);
	}

	@Override
	public void onUserOptedOut() {
		SponsorPayLogger.d(TAG, "onUserOptedOut method");
		mPresentation = null;
		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventAborted);
	}

	/*
	 * OnOffersAvailableResponseReceivedListener
	 */
	@Override
	public void onError(int arg0) {
		SponsorPayLogger.d(TAG, "onError method");
		notifyVideoError();
	}

	@Override
	public void onNoOffersAvailable(OffersAvailableResponse arg0) {
		SponsorPayLogger.d(TAG, "onNoOffersAvailable method");
		sendValidationEvent(arg0.getOffersAvailable().size() > 0 ? SPTPNVideoValidationResult.SPTPNValidationSuccess
				: SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
	}

	@Override
	public void onOffersAvailable(OffersAvailableResponse arg0) {
		SponsorPayLogger.d(TAG, "onOffersAvailable method");
		sendValidationEvent(arg0.getOffersAvailable().size() > 0 ? SPTPNVideoValidationResult.SPTPNValidationSuccess
				: SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
	}

	/*
	 * provide advertisement to helper HyprMXVideoActivity
	 */
	
	public HyprMXPresentation getPresentation(){
		return mPresentation;
	}
	
}
