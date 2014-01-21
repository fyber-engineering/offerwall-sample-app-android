/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.mbe;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoValidationResult;


public enum MockVideoSetting {

	SPValidationTimeout("Timeout (SP SDK side)", 
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationTimeout),
	TPNValidationTimeout("Timeout (3rd party SDK side)", 
			// overusing the playing behaviour 
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationTimeout),
	NoVideoAvailable("No video available", 
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable),
	NetowrokError("Network error", 
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationNetworkError),
	DiskError("Disk error", 
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationDiskError),
	OtherError("Other error", 
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationError),
			
	PlayingTimeout("Timeout (3rd party SDK side)",
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventTimeout,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartAndTimeout("Started + Timeout (3rd party SDK)",
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventTimeout,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartedAborted("Started + Aborted",
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventAborted,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartedFinishedClosed("Started + Finished + Closed",
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventFinished,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingNoVideo("No video on play",
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingOtherError("Other error",
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventError,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartedOtherError("Started + other error",
			MockVideoMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventError,
			SPTPNVideoValidationResult.SPTPNValidationSuccess);
	
    private final String text;
    private final MockVideoMediationPlayingBehaviour behaviour;
    private final SPTPNVideoEvent videoEvent;
    private final SPTPNVideoValidationResult validationResult;
	
	private MockVideoSetting(final String text,
			MockVideoMediationPlayingBehaviour behaviour,
			SPTPNVideoEvent videoEvent,
			SPTPNVideoValidationResult validationResult) {
		this.text = text;
		this.behaviour = behaviour;
		this.videoEvent = videoEvent;
		this.validationResult = validationResult;
	}

    @Override
    public String toString() {
        return text;
    }
    
    public MockVideoMediationPlayingBehaviour getBehaviour() {
    	return behaviour;
    }

	public SPTPNVideoEvent getVideoEvent() {
		return videoEvent;
	}
	
	public SPTPNVideoValidationResult getValidationResult() {
		return validationResult;
	}
	
}