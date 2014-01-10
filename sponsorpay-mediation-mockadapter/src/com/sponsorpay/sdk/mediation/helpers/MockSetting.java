/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.helpers;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.sdk.mediation.MockMediationPlayingBehaviour;


public enum MockSetting {

	SPValidationTimeout("Timeout (SP SDK side)", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationTimeout),
	TPNValidationTimeout("Timeout (3rd party SDK side)", 
			// overusing the playing behaviour 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationTimeout),
	NoVideoAvailable("No video available", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable),
	NetowrokError("Network error", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationNetworkError),
	DiskError("Disk error", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationDiskError),
	OtherError("Other error", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationError),
			
	PlayingTimeout("Timeout (3rd party SDK side)",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventTimeout,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartAndTimeout("Started + Timeout (3rd party SDK)",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventTimeout,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartedAborted("Started + Aborted",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventAborted,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartedFinishedClosed("Started + Finished + Closed",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventFinished,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingNoVideo("No video on play",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingOtherError("Other error",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventError,
			SPTPNVideoValidationResult.SPTPNValidationSuccess),
	PlayingStartedOtherError("Started + other error",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventError,
			SPTPNVideoValidationResult.SPTPNValidationSuccess);
	
    private final String text;
    private final MockMediationPlayingBehaviour behaviour;
    private final SPTPNVideoEvent videoEvent;
    private final SPTPNVideoValidationResult validationResult;
	
	private MockSetting(final String text,
			MockMediationPlayingBehaviour behaviour,
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
    
    public MockMediationPlayingBehaviour getBehaviour() {
    	return behaviour;
    }

	public SPTPNVideoEvent getVideoEvent() {
		return videoEvent;
	}
	
	public SPTPNVideoValidationResult getValidationResult() {
		return validationResult;
	}
	
}