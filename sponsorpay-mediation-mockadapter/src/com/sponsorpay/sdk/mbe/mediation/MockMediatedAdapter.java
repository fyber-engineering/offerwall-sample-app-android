/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mbe.mediation;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class MockMediatedAdapter extends SPMediationAdapter {

	private static final String TAG = "MockMediatedAdapter";

	public static final String ADAPTER_NAME = "MockMediatedNetwork";

	private static final String VERSION_STRING = "1.0.0";

	public static final String MOCK_PLAYING_BEHAVIOUR = "mock.playing.behaviour";
	public static final String VALIDATION_RESULT = "validation.event.result";
	public static final String VIDEO_EVENT_RESULT = "video.event.result";
	private static final String CLASS_NAME = "class.name";
	
	private static final int DELAY_FOR_START_PLAY_EVENT = 500;
	private static final int DELAY_FOR_VIDEO_EVENT = 4500;

	private HashMap<String, Object> configs;

	public MockMediatedAdapter() {
		configs = new HashMap<String, Object>(3);
		configs.put(VALIDATION_RESULT,
				SPTPNValidationResult.SPTPNValidationSuccess);
		configs.put(MOCK_PLAYING_BEHAVIOUR,
				MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult);
		configs.put(VIDEO_EVENT_RESULT, SPTPNVideoEvent.SPTPNVideoEventFinished);
		configs.put(CLASS_NAME, MediationConfigurationActivity.class.getCanonicalName());
	}
	
	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting mocking mediation adapter");
		SPMediationConfigurator.INSTANCE.setConfigurationForAdapter(getName(), configs);
		return true;
	}
	
	@Override
	public String getName() {
		return ADAPTER_NAME;
	}
	
	@Override
	public String getVersion() {
		return VERSION_STRING;
	}

	@Override
	public void videosAvailable(Context context) {
		// let timeout occur, otherwise fire the event
		SPTPNValidationResult validationResult = (SPTPNValidationResult) configs.get(VALIDATION_RESULT);
		if (validationResult != SPTPNValidationResult.SPTPNValidationTimeout || 
				configs.get(MOCK_PLAYING_BEHAVIOUR) == MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce) {
			sendValidationEvent(validationResult);
		}
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		MockMediationPlayingBehaviour behaviour = (MockMediationPlayingBehaviour) configs.get(MOCK_PLAYING_BEHAVIOUR);
		Handler handler = new Handler();
		switch (behaviour) {
		case MockMediationPlayingBehaviourTimeOut:
			// Ignore call and let it time out
			break;

		case MockMediationPlayingBehaviourTriggerStartAndTimeOut:
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					notifyVideoStarted();
				}
			};
			handler.postDelayed(runnable, DELAY_FOR_START_PLAY_EVENT);
			break;
		case MockMediationPlayingBehaviourTriggerResultOnce:
			sendVideoEvent((SPTPNVideoEvent) configs.get(VIDEO_EVENT_RESULT));
			clearVideoEvent();
			break;
		case MockMediationPlayingBehaviourTriggerStartAndFinalResult:
			// Start event
			runnable = new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(parentActivity,
							MockMediationActivity.class);
					notifyVideoStarted();
					parentActivity.startActivityForResult(intent, 986547);
				}
			};
			handler.postDelayed(runnable, DELAY_FOR_START_PLAY_EVENT);
			// Result event
			runnable = new Runnable() {
				@Override
				public void run() {
					SPTPNVideoEvent eventResultToFire = (SPTPNVideoEvent) configs
							.get(VIDEO_EVENT_RESULT);
					sendVideoEvent(eventResultToFire);
					if (eventResultToFire == SPTPNVideoEvent.SPTPNVideoEventFinished) {
						try {
							// Sleep is here to allow mBE client to receive the
							// first event
							Thread.sleep(600);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventClosed);
						clearVideoEvent();
					}
					parentActivity.finishActivity(986547);
				}
			};
			handler.postDelayed(runnable, DELAY_FOR_VIDEO_EVENT);
			break;
		}	
	}

}
