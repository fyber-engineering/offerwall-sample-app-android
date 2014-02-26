/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.mbe;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.sponsorpay.mediation.MockMediatedAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.utils.SponsorPayLogger;

public class MockMediatedVideoAdapter extends SPBrandEngageMediationAdapter<MockMediatedAdapter>{

	public static final String VIDEO_MOCK_SETTING = "video.mock.setting";
	private static final String VIDEO_CLASS_NAME = "video.class.name";
	
	private static final int DELAY_FOR_START_PLAY_EVENT = 500;
	private static final int DELAY_FOR_VIDEO_EVENT = 4500;


	public MockMediatedVideoAdapter(MockMediatedAdapter adapter, HashMap<String, Object> configs) {
		super(adapter);
		configs.put(VIDEO_MOCK_SETTING, MockVideoSetting.PlayingStartedFinishedClosed);
		configs.put(VIDEO_CLASS_NAME, VideoMediationConfigurationActivity.class.getCanonicalName());
	}
	
	@Override
	public void videosAvailable(Context context) {
		// let timeout occur, otherwise fire the event
		SPTPNVideoValidationResult validationResult = (SPTPNVideoValidationResult) getConfig().getValidationResult();
		if (validationResult != SPTPNVideoValidationResult.SPTPNValidationTimeout || 
				getConfig().getBehaviour() == MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce) {
			sendValidationEvent(validationResult);
		}
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		MockVideoMediationPlayingBehaviour behaviour = (MockVideoMediationPlayingBehaviour) 
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
					SPTPNVideoEvent eventResultToFire = (SPTPNVideoEvent) getConfig().getVideoEvent();
					sendVideoEvent(eventResultToFire);
					if (eventResultToFire == SPTPNVideoEvent.SPTPNVideoEventFinished) {
						try {
							// Sleep is here to allow mBE client to receive the
							// first event
							Thread.sleep(600);
						} catch (InterruptedException e) {
							SponsorPayLogger.e(getName(), e.getMessage(), e);
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
	
}
