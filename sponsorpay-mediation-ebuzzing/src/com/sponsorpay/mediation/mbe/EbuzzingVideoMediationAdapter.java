/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.mbe;

import android.app.Activity;
import android.content.Context;

import com.ebuzzing.sdk.controller.util.EbzVideoListener;
import com.ebuzzing.sdk.interfaces.EbzError;
import com.ebuzzing.sdk.interfaces.EbzInterstitial;
import com.sponsorpay.mediation.EbuzzingMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;

public class EbuzzingVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<EbuzzingMediationAdapter> implements
		EbzVideoListener,  EbzInterstitial.EbzInterstitialListener {

//	private static final float MIN_PLAY_REQUIRED = 0.1f;
//
//	private static final String SHOW_CLOSE_BUTTON = "show.close.button";
//	private static final String VIDEO_WATCHED_AT = "video.considered.watched.at";
//
//	private float mVideoWatchedAt;
	
	private EbzInterstitial mEbzInterstitialRewarded;

	public EbuzzingVideoMediationAdapter(EbuzzingMediationAdapter adapter) {
		super(adapter);
	}

	@Override
	public void videosAvailable(Context context) {
		mEbzInterstitialRewarded = new EbzInterstitial(context, "TAG_DEMO_ANDROID", false);
		mEbzInterstitialRewarded.load();
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		mEbzInterstitialRewarded.show();
		notifyVideoStarted();
	}

//	private void setVideoWatchedAt() {
//		try {
//			mVideoWatchedAt = Float.parseFloat(SPMediationConfigurator
//					.getConfiguration(getName(), VIDEO_WATCHED_AT, "0.9",
//							String.class));
//			if (mVideoWatchedAt < MIN_PLAY_REQUIRED) {
//				mVideoWatchedAt = MIN_PLAY_REQUIRED;
//			} else if (mVideoWatchedAt > 1) {
//				mVideoWatchedAt = 1;
//			}
//		} catch (NumberFormatException e) {
//			mVideoWatchedAt = 0.9f;
//		}
//	}

//	// Vungle EventListener interface
//	@Override
//	public void onVungleAdEnd() {
//		// this is fired before onVungleView method
//		// notifyCloseEngagement();
//	}
//
//	@Override
//	public void onVungleAdStart() {
//		notifyVideoStarted();
//	}
	
	@Override
	public boolean onCloseFullscreen() {
		notifyCloseEngagement();
		return false;
	}

	@Override
	public boolean onDisplayFullscreen() {
		return false;
	}

	@Override
	public boolean onLoadFail(EbzError errorCode) {
//		errorCode.EbzAdFailsToLoad;
//		errorCode.EbzAdServerBadResponse;
//		errorCode.EbzAdServerError;
//		errorCode.EbzNetworkError;
//		EbzError r = errorCode.EbzNoAdsAvailable;
		if(errorCode.equals(EbzError.EbzNoAdsAvailable)){
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
			
		}else if(errorCode.equals(EbzError.EbzNetworkError)){
			
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNetworkError);
			
		}else {
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationError);
		}
		
		return false;
	}

	@Override
	public boolean onLoadSuccess() {
		sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationSuccess);
		return true;
	}

	@Override
	public boolean onRewardUnlocked() {
		setVideoPlayed();
		return false;
	}


	@Override
	public void onComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrepared() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgress(int watchedPercent) {
//		if (watchedPercent >= mVideoWatchedAt) {
//			// we notify only about the finished event tdue to the lack of event
//			// separation
//			// the mediation offer will do the conversion and the close
//			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventFinished);
//		} else {
//			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventAborted);
//		}
//		clearVideoEvent();
		
	}

}