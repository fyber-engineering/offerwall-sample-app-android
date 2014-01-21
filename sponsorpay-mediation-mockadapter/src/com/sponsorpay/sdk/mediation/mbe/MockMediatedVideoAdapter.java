/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.mbe;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.sdk.mediation.MockMediatedAdapter;

public class MockMediatedVideoAdapter extends SPBrandEngageMediationAdapter<MockMediatedAdapter>{


//	public static final String VIDEO_MOCK_BEHAVIOUR = "video.mock.behaviour";
//	public static final String VIDEO_VALIDATION_RESULT = "validation.event.result";
//	public static final String VIDEO_EVENT_RESULT = "video.event.result";
	public static final String VIDEO_MOCK_SETTING = "video.mock.setting";
	private static final String VIDEO_CLASS_NAME = "video.class.name";
	
	private static final int DELAY_FOR_START_PLAY_EVENT = 500;
	private static final int DELAY_FOR_VIDEO_EVENT = 4500;


	public MockMediatedVideoAdapter(MockMediatedAdapter adapter, HashMap<String, Object> configs) {
		super(adapter);
//		configs.put(VIDEO_VALIDATION_RESULT,
//				SPTPNVideoValidationResult.SPTPNValidationSuccess);
//		configs.put(VIDEO_MOCK_BEHAVIOUR,
//				MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult);
//		configs.put(VIDEO_EVENT_RESULT, SPTPNVideoEvent.SPTPNVideoEventFinished);
		configs.put(VIDEO_MOCK_SETTING, MockVideoSetting.PlayingStartedFinishedClosed);
		configs.put(VIDEO_CLASS_NAME, VideoMediationConfigurationActivity.class.getCanonicalName());
	}
	
	@Override
	public void videosAvailable(Context context) {
		// let timeout occur, otherwise fire the event
//		SPTPNVideoValidationResult validationResult = (SPTPNVideoValidationResult) getConfig(VIDEO_VALIDATION_RESULT);
		SPTPNVideoValidationResult validationResult = (SPTPNVideoValidationResult) getConfig().getValidationResult();
		if (validationResult != SPTPNVideoValidationResult.SPTPNValidationTimeout || 
//				getConfig(VIDEO_MOCK_BEHAVIOUR) == MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce) {
				getConfig().getBehaviour() == MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce) {
			sendValidationEvent(validationResult);
		}
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		MockVideoMediationPlayingBehaviour behaviour = (MockVideoMediationPlayingBehaviour) 
//				getConfig(VIDEO_MOCK_BEHAVIOUR);
				getConfig().getBehaviour();
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
//			sendVideoEvent((SPTPNVideoEvent) getConfig(VIDEO_EVENT_RESULT));
			sendVideoEvent((SPTPNVideoEvent) getConfig().getVideoEvent());
			clearVideoEvent();
			break;
		case MockMediationPlayingBehaviourTriggerStartAndFinalResult:
			// Start event
			runnable = new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(parentActivity,
							MockVideoMediationActivity.class);
					notifyVideoStarted();
					parentActivity.startActivityForResult(intent, 986547);
				}
			};
			handler.postDelayed(runnable, DELAY_FOR_START_PLAY_EVENT);
			// Result event
			runnable = new Runnable() {
				@Override
				public void run() {
//					SPTPNVideoEvent eventResultToFire = (SPTPNVideoEvent) getConfig(VIDEO_EVENT_RESULT);
					SPTPNVideoEvent eventResultToFire = (SPTPNVideoEvent) getConfig().getVideoEvent();
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

	private MockVideoSetting getConfig() {
		return (MockVideoSetting) SPMediationConfigurator.getConfiguration(
				getName(), VIDEO_MOCK_SETTING, Object.class);
	}
//	private Object getConfig(String key) {
//		return SPMediationConfigurator.getConfiguration(getName(), key, Object.class);
//	}

	
}
