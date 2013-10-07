package com.sponsorpay.sdk.mbe.mediation;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationAdaptor;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationValidationEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class FlurryMediationAdaptor implements SPMediationAdaptor, FlurryAdListener{
			
	private static final String TAG = "FlurryAdaptor";

	private static final String ADAPTOR_VERSION = "1.0.0";
	public static final String ADAPTOR_NAME = "MockMediatedNetwork";
//	private static final String ADATPOR_NAME = "flurryappcircleclips";
	
	private static final String API_KEY = "api.key";
	private static final String AD_NAME_SPACE = "ad.name.space";
	private static final String AD_NAME_TYPE = "ad.name.type";

	private FrameLayout mLayout;
	private boolean mVideoPlayed = false;

	private SPMediationValidationEvent mValidationEvent;

	private SPMediationVideoEvent mVideoEvent;

	private Map<String, String> mValidationContextData;

	private Map<String, String> mVideoContextData;


	@Override
	public boolean startAdaptor(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Flurry adaptor - SDK version " + FlurryAgent.getReleaseVersion());
		String apiKey = SPMediationConfigurator.INSTANCE.getConfiguration(ADAPTOR_NAME, API_KEY, String.class);
		if (StringUtils.notNullNorEmpty(apiKey)) {
			FlurryAgent.onStartSession(activity, apiKey);
			FlurryAds.setAdListener(this);
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return ADAPTOR_NAME;
	}

	@Override
	public String getVersion() {
		return ADAPTOR_VERSION;
	}

	@Override
	public void videosAvailable(Context context, SPMediationValidationEvent event,
			Map<String, String> contextData) {
		
		mValidationEvent = event;
		mValidationContextData = contextData;
		mLayout = new FrameLayout(context);
		mVideoPlayed = false;
		FlurryAds.fetchAd(context, SPMediationConfigurator.INSTANCE
				.getConfiguration(ADAPTOR_NAME, AD_NAME_SPACE, String.class),
				mLayout, FlurryAdSize.valueOf(SPMediationConfigurator.INSTANCE
				.getConfiguration(ADAPTOR_NAME, AD_NAME_TYPE, String.class)));
	}

	@Override
	public void startVideo(final Activity parentActivity,
			SPMediationVideoEvent event, Map<String, String> contextData) {
		mVideoEvent = event;
		mVideoContextData = contextData;
		mLayout = new FrameLayout(parentActivity);

		if (Build.VERSION.SDK_INT > 10) {
			// REALLY BAD WORKAROUND
			FlurryAds.fetchAd(parentActivity, SPMediationConfigurator.INSTANCE
					.getConfiguration(ADAPTOR_NAME, AD_NAME_SPACE, String.class), 
					mLayout, FlurryAdSize.FULLSCREEN);
		}
		parentActivity.addContentView(mLayout, new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		
//		Handler h = new Handler(parentActivity.getMainLooper());
//		h.post(new Runnable() {
//			
//			@Override
//			public void run() {
//		if (parentActivity instanceof SPBrandEngageActivity) {
//			((SPBrandEngageActivity) parentActivity).run(new Runnable() {
//				
//				@Override
//				public void run() {
		FlurryAds.displayAd(parentActivity, SPMediationConfigurator.INSTANCE
				.getConfiguration(ADAPTOR_NAME, AD_NAME_SPACE, String.class),
				mLayout);
		//				}
//			});
//		}
//			}
//		});
	}

	// FlurryAdListener
	@Override
	public void onAdClicked(String adSpaceName) {
	}

	@Override
	public void onAdClosed(String adSpaceName) {
		// send video event finished or aborted
		sendVideoEvent(mVideoPlayed ? SPTPNVideoEvent.SPTPNVideoEventFinished : SPTPNVideoEvent.SPTPNVideoEventAborted);
		mVideoEvent = null;
		mVideoContextData = null;
		mLayout = null;
	}

	@Override
	public void onAdOpened(String adSpaceName) {
		// send video event started
		sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventStarted);
	}

	@Override
	public void onApplicationExit(String adSpaceName) {
	}

	@Override
	public void onRenderFailed(String adSpaceName) {
		// send video event error
	}

	@Override
	public void onVideoCompleted(String adSpaceName) {
		// store video played = true
		mVideoPlayed = true;
	}

	@Override
	public boolean shouldDisplayAd(String adSpaceName, FlurryAdType type) {
//		return type.equals(FlurryAdType.VIDEO_TAKEOVER);
		return true;
	}

	@Override
	public void spaceDidFailToReceiveAd(String adSpaceName) {
		// send validate event no video
//		mLayout = null;
		sendValidationEvent(SPTPNValidationResult.SPTPNValidationNoVideoAvailable);
	}

	@Override
	public void spaceDidReceiveAd(String adSpaceName) {
		// send validate event success
//		mLayout = null;
		sendValidationEvent(SPTPNValidationResult.SPTPNValidationSuccess);
	}

	//HELPER methods
		
	private void sendValidationEvent(SPTPNValidationResult validationResult) {
		if (mValidationEvent != null) {
			// lower case bug
			mValidationEvent.validationEventResult(ADAPTOR_NAME.toLowerCase(), validationResult, mValidationContextData);
		} else {
			SponsorPayLogger.d(TAG, "No validation result listener to notify");
		}
		mValidationEvent = null;
		mValidationContextData = null;
	}
	private void sendVideoEvent(SPTPNVideoEvent videoEvent) {
		if (mVideoEvent != null) {
			mVideoEvent.videoEventOccured(ADAPTOR_NAME, videoEvent, mVideoContextData);
		} else {
			SponsorPayLogger.d(TAG, "No video event listener to notify");
		}
	}
	
	
	
}
