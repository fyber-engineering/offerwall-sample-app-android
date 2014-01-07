/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.helpers;

import com.sponsorpay.sdk.android.mediation.SPTPNValidationResult;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.sdk.mediation.MockMediationPlayingBehaviour;


public enum MockSetting {

	SPValidationTimeout("Timeout (SP SDK side)", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNValidationResult.SPTPNValidationTimeout),
	TPNValidationTimeout("Timeout (3rd party SDK side)", 
			// overusing the playing behaviour 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNValidationResult.SPTPNValidationTimeout),
	NoVideoAvailable("No video available", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNValidationResult.SPTPNValidationNoVideoAvailable),
	NetowrokError("Network error", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNValidationResult.SPTPNValidationNetworkError),
	DiskError("Disk error", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNValidationResult.SPTPNValidationDiskError),
	OtherError("Other error", 
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTimeOut,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNValidationResult.SPTPNValidationError),
			
	PlayingTimeout("Timeout (3rd party SDK side)",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventTimeout,
			SPTPNValidationResult.SPTPNValidationSuccess),
	PlayingStartAndTimeout("Started + Timeout (3rd party SDK)",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventTimeout,
			SPTPNValidationResult.SPTPNValidationSuccess),
	PlayingStartedAborted("Started + Aborted",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventAborted,
			SPTPNValidationResult.SPTPNValidationSuccess),
	PlayingStartedFinishedClosed("Started + Finished + Closed",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventFinished,
			SPTPNValidationResult.SPTPNValidationSuccess),
	PlayingNoVideo("No video on play",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventNoVideo,
			SPTPNValidationResult.SPTPNValidationSuccess),
	PlayingOtherError("Other error",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerResultOnce,
			SPTPNVideoEvent.SPTPNVideoEventError,
			SPTPNValidationResult.SPTPNValidationSuccess),
	PlayingStartedOtherError("Started + other error",
			MockMediationPlayingBehaviour.MockMediationPlayingBehaviourTriggerStartAndFinalResult,
			SPTPNVideoEvent.SPTPNVideoEventError,
			SPTPNValidationResult.SPTPNValidationSuccess);
	
    private final String text;
    private final MockMediationPlayingBehaviour behaviour;
    private final SPTPNVideoEvent videoEvent;
    private final SPTPNValidationResult validationResult;
	
	private MockSetting(final String text,
			MockMediationPlayingBehaviour behaviour,
			SPTPNVideoEvent videoEvent,
			SPTPNValidationResult validationResult) {
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
	
	public SPTPNValidationResult getValidationResult() {
		return validationResult;
	}
	
}