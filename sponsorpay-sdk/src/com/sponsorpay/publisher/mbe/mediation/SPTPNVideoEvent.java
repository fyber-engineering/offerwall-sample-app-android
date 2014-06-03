/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe.mediation;

public enum SPTPNVideoEvent {

    SPTPNVideoEventStarted("started"),
    SPTPNVideoEventAborted("aborted"),
    SPTPNVideoEventFinished("finished"),
    SPTPNVideoEventClosed("closed"),
    SPTPNVideoEventNoVideo("no_video"),
    SPTPNVideoEventTimeout("timeout"),
    SPTPNVideoEventError("error"),
    SPTPNVideoEventAdapterNotIntegrated("no_sdk");

    private final String text;
	
	private SPTPNVideoEvent(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
