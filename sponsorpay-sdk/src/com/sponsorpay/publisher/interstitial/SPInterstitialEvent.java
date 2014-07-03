/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

/**
 * Intersitial tracking events 
 */
public enum SPInterstitialEvent {
	/**
	 * The network is being requested
	 */
	ValidationRequest("request"),
	/**
	 * The network has an ad fill
	 */
	ValidationFill("fill"),
	/**
	 * The network has no fill
	 */
	ValidationNoFill("no_fill"),
	/**
	 * An error occurred while performing the request
	 */
	ValidationError("error"),
	/**
	 * The ad as been successfully shown
	 */
	ShowImpression("impression"),
	/**
	 * The ad as been clicked by the user
	 */
	ShowClick("click"),
	/**
	 * The ad as been closed by the user
	 */
	ShowClose("close"),
	/**
	 * An error has occurred while the ad is being shown
	 */
	ShowError("error"),
	/**
	 * The 3rd party network is not integrated
	 */
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
