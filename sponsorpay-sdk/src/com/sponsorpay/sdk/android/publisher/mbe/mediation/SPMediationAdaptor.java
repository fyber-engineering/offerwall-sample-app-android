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

public abstract class SPMediationAdaptor {

	private static final int VALIDATION_RESULT = 1;
	private static final int VIDEO_EVENT = 2;
	
	private SPMediationValidationEvent mValidationEvent;
	private Map<String, String> mValidationContextData;
	private SPMediationVideoEvent mVideoEvent;
	private Map<String, String> mVideoContextData;
	private boolean mVideoPlayed = false;

//	private int mValidationMatchingNumber;
//	private int mVideoMatchingNumber;
	private Handler mHandler;
//	private Handler mVideoHandler;

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
//		mVideoHandler = new Handler();
	}
	
	public void videosAvailable(Context context, SPMediationValidationEvent event,
			Map<String, String> contextData) {
		mValidationEvent = event;
		mValidationContextData = contextData;
		videosAvailable(context);
		//TODO extract delay
		mHandler.sendEmptyMessageDelayed(VALIDATION_RESULT, 4500);
//		mValidationMatchingNumber = SPTimeoutChecker.getTimeoutMatcher();
//		mValidationHandler.postDelayed(new SPTimeoutChecker(mValidationMatchingNumber) {
//			@Override
//			public int getMatchingNumber() {
//				return mValidationMatchingNumber;
//			}
//
//			@Override
//			public void doRun() {
//				sendValidationEvent(SPTPNValidationResult.SPTPNValidationTimeout);
//			}
//			
//		}, 4500);
//		Message m = new Message();
//		mValidationHandler.
		
//		mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				sendValidationEvent(SPTPNValidationResult.SPTPNValidationTimeout);
//			}
//			// TODO extract this
//		}, 4500);
	}

	public void startVideo(Activity parentActivity,
			SPMediationVideoEvent event, Map<String, String> contextData){ 
		mVideoPlayed = false;
		mVideoEvent = event;
		mVideoContextData = contextData;
		startVideo(parentActivity);
		// start timeout here
		//TODO extract delay
//		mHandler.sendEmptyMessageDelayed(VIDEO_EVENT, 4500);
		
//		mVideoMatchingNumber = SPTimeoutChecker.getTimeoutMatcher();
//		mHandler.postDelayed(new SPTimeoutChecker(mVideoMatchingNumber) {
//			@Override
//			public int getMatchingNumber() {
//				return mVideoMatchingNumber;
//			}
//
//			@Override
//			public void doRun() {
////				sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventTimeout);
//			}
//			
//		}, 4500);
	}
	
   protected void sendValidationEvent(SPTPNValidationResult result) {
	   if (mValidationEvent != null) {
		   // due to mbe bug, we need to send validation in lower case
//		   mValidationMatchingNumber = SPTimeoutChecker.getTimeoutMatcher();
		   mHandler.removeMessages(VALIDATION_RESULT);
		   mValidationEvent.validationEventResult(getName().toLowerCase(), result, mValidationContextData);
		   mValidationEvent = null;
		   mValidationContextData = null;
	   } else {
//		   SponsorPayLogger.d(tag, message)
	   }
   }
   
   protected void sendVideoEvent(SPTPNVideoEvent event) {
	   if (mVideoEvent != null) {
		   mVideoEvent.videoEventOccured(getName(), event, mVideoContextData);
		   if (event.equals(SPTPNVideoEvent.SPTPNVideoEventStarted)) {
//				mVideoMatchingNumber = SPTimeoutChecker.getTimeoutMatcher();
			   mHandler.removeMessages(VIDEO_EVENT);
		   }
	   } else {
//		   SponsorPayLogger.d(tag, message)
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
