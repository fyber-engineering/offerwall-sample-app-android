/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe.mediation;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.sponsorpay.mediation.SPMediationAdapter;
import com.sponsorpay.mediation.SPMediationCoordinator;
import com.sponsorpay.publisher.mbe.SPBrandEngageClient;
import com.sponsorpay.utils.SponsorPayLogger;

/**
 * <p>
 * Base class for mobile BrandEngage Mediation adapter
 * </p>
 * 
 * This class defines the required specific methods to every adapter and provides convenience methods
 * handling timeouts, validation and events notifications. The {@link SPMediationCoordinator} will
 * communicate the results back to the {@link SPBrandEngageClient}.
 * 
 */
public abstract class SPBrandEngageMediationAdapter<V extends SPMediationAdapter> {

	private static final String TAG = "SPBrandEngageMediationAdapter";

	/*
	 * Validation event message.what field
	 */
	private static final short VALIDATION_RESULT = 1;
	/*
	 * BrandEngage event message.what field
	 */
	private static final short VIDEO_EVENT = 2;
	
	/*
	 * Timeout delay, in nanoseconds
	 */
	public static final int START_TIMEOUT_DELAY = 4500;
	
	public static final int VALIDATION_TIMEOUT_DELAY = 4500;
	
	/*
	 * The mediation validation event listener 
	 */
	private SPMediationValidationEvent mValidationEvent;
	/*
	 * The mediation validation context data for this request 
	 */
	private Map<String, String> mValidationContextData;
	
	/*
	 * The mediation video event listener 
	 */
	private SPMediationVideoEvent mVideoEvent;
	/*
	 * The mediation video context data for this request 
	 */
	private Map<String, String> mVideoContextData;
	
	/*
	 * Boolean storing the 'play' status of the video
	 */
	private boolean mVideoPlayed = false;

	/*
	 * Handler queue for validation and video timeouts 
	 */
	private Handler mHandler;

	/**
	 * The base {@link SPMediationAdapter} for this network
	 */
	protected V mAdapter;
	
	public SPBrandEngageMediationAdapter(V adapter) {
		mAdapter = adapter;

		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case VALIDATION_RESULT:
					sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationTimeout);
					break;
				case VIDEO_EVENT:
					sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventTimeout);
					break;
				}
			}
		};
	}
	
	public V getMediationAdapter() {
		return mAdapter;
	}

	/* ======================================================
	 *              Adapter specific methods
	 * ======================================================
	 */
	
	/**
	 * Checks whether there are videos available to start playing. This is expected
	 * to be asynchronous, and the answer should be delivered through the
	 * {@link #sendValidationEvent(SPTPNVideoValidationResult)}.
	 */
	public abstract void videosAvailable(Context context);

	/**
	 * Instructs the wrapped video network SDK to start playing a video.
	 * @param parentActivity 
	 * 		If the wrapped SDK needs a parent activity, it can use the provided one.
	 */
	public abstract void startVideo(Activity parentActivity);

	/**
	 * Method called from the {@link SPMediationCoordinator} to check for videos
	 * availability for this network. The result of the method is returned asynchronously
	 * to the provided {@link SPMediationValidationEvent}.
	 * This method also stores the listener and the context data information to send it back
	 * when needed and starts the validation timeout.
	 *   
	 * @param context
	 * 			The activity context
	 * @param event
	 * 			The {@link SPMediationValidationEvent} to be notified.
	 * @param contextData
	 * 			The context data used in this request
	 */
	public void videosAvailable(Context context, SPMediationValidationEvent event,
			Map<String, String> contextData) {
		mValidationEvent = event;
		mValidationContextData = contextData;
		mHandler.sendEmptyMessageDelayed(VALIDATION_RESULT, VALIDATION_TIMEOUT_DELAY);
		videosAvailable(context);
	}

	/**
	 * Method called from the {@link SPMediationCoordinator} to start videos
	 * engagement for this network. The video status is returned asynchronously
	 * to the provided {@link SPMediationVideoEvent}.
	 * This method also stores the listener and the context data information to send it back
	 * when needed and starts the video validation timeout.
	 * 
	 * @param parentActivity
	 * 			The parent activity 
	 * @param event
	 * 			The {@link SPMediationVideoEvent} to be notified.
	 * @param contextData
	 * 			The context data used in this request
	 */
	public void startVideo(Activity parentActivity,
			SPMediationVideoEvent event, Map<String, String> contextData){ 
		mVideoPlayed = false;
		mVideoEvent = event;
		mVideoContextData = contextData;
		mHandler.sendEmptyMessageDelayed(VIDEO_EVENT, START_TIMEOUT_DELAY);
		startVideo(parentActivity);
	}
	
	/* ======================================================
	 *               Convenience methods
	 * ======================================================
	 */

	/**
    * Convenience method that sends a validation event if there is a listener 
    * to be notified, remove the timeout message from the queue and clears
    * the validation events related data, preventing more events from this
    * adapter to be sent.
    * 
    * @param result
    * 		The {@link SPTPNVideoEvent} to be sent.
    */
   protected void sendValidationEvent(SPTPNVideoValidationResult result) {
	   if (mValidationEvent != null) {
		   mHandler.removeMessages(VALIDATION_RESULT);
		   mValidationEvent.validationEventResult(getName(), result, mValidationContextData);
		   mValidationEvent = null;
		   mValidationContextData = null;
	   } else {
		   SponsorPayLogger.i(TAG, "No validation event listener");
	   }
   }
   
   /**
    * Convenience method that sends a video event if there is a listener 
    * to be notified and remove the timeout message from the queue if it is a 
    * {@link SPTPNVideoEvent#SPTPNVideoEventStarted}
    * 
    * @param event
    * 		The {@link SPTPNVideoEvent} to be sent.
    */
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
   
   /**
    * Convenience method that clears all video events related data.
    * This method is called automatically after an engagement close or video 
    * error event, preventing more events from this adapter to be sent.
    */
   protected void clearVideoEvent() {
	   mVideoEvent = null;
	   mVideoContextData = null;
   }

   /**
    * Convenience method to mark a video as fully watched, thus
    * considering that the engagement was successful
    */
   protected void setVideoPlayed() {
	   sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventFinished);
	   mVideoPlayed = true;
   }
   
   /**
    * Convenience method to notify that a video has been started 
    */
   protected void notifyVideoStarted() {
	   sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventStarted);
   }
   
   /**
    * Convenience method to notify that an engagement has been closed 
    */
   protected void notifyCloseEngagement() {
		sendVideoEvent(mVideoPlayed ? SPTPNVideoEvent.SPTPNVideoEventClosed
				: SPTPNVideoEvent.SPTPNVideoEventAborted);
		clearVideoEvent();
   }
   
   /**
    * Convenience method to notify that an error occurred while the video was playing
    */
   protected void notifyVideoError() {
	   sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventError);
	   clearVideoEvent();
   }
   
   protected String getName() {
	   return mAdapter.getName();
   }
   
}
