/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public abstract class SPMediationAdaptor {

	private static final String TAG = "SPMediationAdaptor";

	private static final short VALIDATION_RESULT = 1;
	private static final short VIDEO_EVENT = 2;
	private static final int TIMEOUT_DELAY = 4500;
	
	private SPMediationValidationEvent mValidationEvent;
	private Map<String, String> mValidationContextData;
	private SPMediationVideoEvent mVideoEvent;
	private Map<String, String> mVideoContextData;
	private boolean mVideoPlayed = false;

	private Handler mHandler;

	public abstract boolean startAdaptor(Activity activity);

	public abstract String getName();

	public abstract String getVersion();

	public abstract void videosAvailable(Context context);
	
	public abstract void startVideo(Activity parentActivity);
	
	public SPMediationAdaptor() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case VALIDATION_RESULT:
					sendValidationEvent(SPTPNValidationResult.SPTPNValidationTimeout);
					break;
				case VIDEO_EVENT:
					sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventTimeout);
					break;
				}
			}
		};
	}
	
	public void videosAvailable(Context context, SPMediationValidationEvent event,
			Map<String, String> contextData) {
		mValidationEvent = event;
		mValidationContextData = contextData;
		videosAvailable(context);
		mHandler.sendEmptyMessageDelayed(VALIDATION_RESULT, TIMEOUT_DELAY);
	}

	public void startVideo(Activity parentActivity,
			SPMediationVideoEvent event, Map<String, String> contextData){ 
		mVideoPlayed = false;
		mVideoEvent = event;
		mVideoContextData = contextData;
		startVideo(parentActivity);
		mHandler.sendEmptyMessageDelayed(VIDEO_EVENT, TIMEOUT_DELAY);
	}
	
   protected void sendValidationEvent(SPTPNValidationResult result) {
	   if (mValidationEvent != null) {
		   mHandler.removeMessages(VALIDATION_RESULT);
		   // due to mbe bug, we need to send validation in lower case
		   mValidationEvent.validationEventResult(getName().toLowerCase(), result, mValidationContextData);
		   mValidationEvent = null;
		   mValidationContextData = null;
	   } else {
		   SponsorPayLogger.i(TAG, "No validation event listener");
	   }
   }
   
   protected void sendVideoEvent(SPTPNVideoEvent event) {
	   if (mVideoEvent != null) {
		   if (event.equals(SPTPNVideoEvent.SPTPNVideoEventStarted)) {
			   mHandler.removeMessages(VIDEO_EVENT);
		   }
		   mVideoEvent.videoEventOccured(getName(), event, mVideoContextData);
	   } else {
		   SponsorPayLogger.i(TAG, "No video event listener");
	   }
   }
   
   protected void clearVideoEvent() {
	   mVideoEvent = null;
	   mVideoContextData = null;
   }

   protected void setVideoPlayed() {
	   mVideoPlayed = true;
   }
   
   protected void notifyVideoStarted() {
	   sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventStarted);
   }
   
   protected void notifyCloseEngagement() {
		sendVideoEvent(mVideoPlayed ? SPTPNVideoEvent.SPTPNVideoEventFinished
				: SPTPNVideoEvent.SPTPNVideoEventAborted);
		clearVideoEvent();
   }
   
   protected void notifyVideoError() {
	   sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventError);
	   clearVideoEvent();
   }
   
}
