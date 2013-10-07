/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

public abstract class SPMediationAdaptor {

	private SPMediationValidationEvent mValidationEvent;
	private Map<String, String> mValidationContextData;
	private SPMediationVideoEvent mVideoEvent;
	private Map<String, String> mVideoContextData;
	private boolean mVideoPlayed = false;

	public abstract boolean startAdaptor(Activity activity);

	public abstract String getName();

	public abstract String getVersion();

	public abstract void videosAvailable(Context context);
	
	public abstract void startVideo(Activity parentActivity);
	
	public void videosAvailable(Context context, SPMediationValidationEvent event,
			Map<String, String> contextData) {
		mValidationEvent = event;
		mValidationContextData = contextData;
		videosAvailable(context);
		//start timeout here! 4.5 s
	}

	public void startVideo(Activity parentActivity,
			SPMediationVideoEvent event, Map<String, String> contextData){ 
		mVideoPlayed = false;
		mVideoEvent = event;
		mVideoContextData = contextData;
		startVideo(parentActivity);
		// start timeout here 
	}
	
   protected void sendValidationEvent(SPTPNValidationResult result) {
	   if (mValidationEvent != null) {
		   // due to mbe bug, we need to send validation in lower case
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
