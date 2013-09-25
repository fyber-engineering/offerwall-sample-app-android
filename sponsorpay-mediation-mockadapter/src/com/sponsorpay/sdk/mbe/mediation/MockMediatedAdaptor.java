package com.sponsorpay.sdk.mbe.mediation;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationAdaptor;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationValidationEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class MockMediatedAdaptor implements SPMediationAdaptor {

	public static final String ADAPTOR_NAME = "MockMediatedNetwork";

	private static final String TAG = "MockMediatedAdaptor";
	private static final String VERSION_STRING = "1.0.0";

	public static final String MOCK_PLAYING_BEHAVIOUR = "mock.playing.behaviour";
	public static final String VALIDATION_RESULT = "validation.event.result";
	public static final String VIDEO_EVENT_RESULT = "video.event.result";
	
	private static final int DELAY_FOR_START_PLAY_EVENT = 500;
	private static final int DELAY_FOR_VIDEO_EVENT = 4500;

	private HashMap<String, Object> configs;

	public MockMediatedAdaptor() {
		configs = new HashMap<String, Object>(3);
		configs.put(VALIDATION_RESULT,
				SPTPNValidationResult.SPTPNValidationSuccess);
		configs.put(MOCK_PLAYING_BEHAVIOUR,
				MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult);
		configs.put(VIDEO_EVENT_RESULT, SPTPNVideoEvent.SPTPNVideoEventFinished);
	}
	
	@Override
	public boolean startAdaptor() {
		SponsorPayLogger.d(TAG, "Starting mocking mediation adaptor");
		SPMediationConfigurator.INSTANCE.setConfigurationForAdaptor(getName(), configs);
		return true;
	}
	
	@Override
	public String getName() {
		return ADAPTOR_NAME;
	}
	
	@Override
	public String getVersion() {
		return VERSION_STRING;
	}

	@Override
	public void videosAvailable(SPMediationValidationEvent event, Map<String, String> contextData) {
		// let timeout occur, otherwise fire the event
		SPTPNValidationResult validationResult = (SPTPNValidationResult) configs.get(VALIDATION_RESULT);
		if (validationResult != SPTPNValidationResult.SPTPNValidationTimeout || 
				configs.get(MOCK_PLAYING_BEHAVIOUR) == MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce) {
			event.validationEventResult(getName().toLowerCase(), validationResult, contextData);
		}
	}

	@Override
	public void startVideo(final Activity parentActivity, final SPMediationVideoEvent event, final Map<String, String> contextData) {
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
						event.videoEventOccured(getName(), SPTPNVideoEvent.SPTPNVideoEventStarted, contextData);
					}
				};
			handler.postDelayed(runnable, DELAY_FOR_START_PLAY_EVENT);
            break;
        case MockMediationPlayingBehaviourTriggerResultOnce:
        	event.videoEventOccured(getName(), (SPTPNVideoEvent) configs.get(VIDEO_EVENT_RESULT), contextData);
            break;
        case MockMediationPlayingBehaviourTriggerStartAndFinalResult:
            // Start event
			runnable = new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(parentActivity, MockMediationActivity.class);
					event.videoEventOccured(getName(), SPTPNVideoEvent.SPTPNVideoEventStarted, contextData);
					parentActivity.startActivityForResult(intent, 986547);
				}
			};
			handler.postDelayed(runnable, DELAY_FOR_START_PLAY_EVENT);
            // Result event
			runnable = new Runnable() {
				@Override
				public void run() {
					SPTPNVideoEvent eventResultToFire = (SPTPNVideoEvent) configs.get(VIDEO_EVENT_RESULT);
					event.videoEventOccured(getName(), eventResultToFire, contextData);
					if (eventResultToFire == SPTPNVideoEvent.SPTPNVideoEventFinished) {
						try {
							//Sleep is here to allow mBE client to receive the first event
							Thread.sleep(600);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						event.videoEventOccured(getName(), SPTPNVideoEvent.SPTPNVideoEventClosed, contextData);
					}
					parentActivity.finishActivity(986547);
				}
			};
			handler.postDelayed(runnable, DELAY_FOR_VIDEO_EVENT);
            break;
		}	
	}

}
