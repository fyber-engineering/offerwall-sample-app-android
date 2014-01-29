/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

public enum SPInterstitialEvent {
	
	ValidationRequest("request"),
	ValidationFill("fill"),
	ValidationNoFill("no_fill"),
	ValidationError("error"),
	ShowImpression("impression"),
	ShowClick("click"),
	ShowClose("close"),
	ShowError("error"),
	NotIntegrated("no_sdk");
	
    private final String text;
	
	private SPInterstitialEvent(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
    
}