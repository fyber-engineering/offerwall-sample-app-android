/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

/**
 * The Interstitial client internal state 
 */
public enum SPInterstitialClientState {
	
	// No offers are ready to show, must do a request before starting an engagement
	READY_TO_CHECK_OFFERS(false, true, true),
	// Querying the server for offers
    REQUESTING_OFFERS(false, false, false),
    // Validating offers received from the backend
    VALIDATING_OFFERS(false, false, false),
    // An ad is preloaded and ready to show
    READY_TO_SHOW_OFFERS(true, true, true),
    // An interstitial is currently being shown
    SHOWING_OFFERS(false, false, false);
    
    private final boolean canShowAdw;
    private final boolean canChangeParameters;
	private final boolean canRequestAds;
    
	SPInterstitialClientState(boolean canShowAds,
			boolean canRequestAds, boolean canChangeParameters) {
		this.canShowAdw = canShowAds;
		this.canRequestAds = canRequestAds;
		this.canChangeParameters = canChangeParameters;
	}
    
	/**
	 * @return true if this state allows the start of an ad
	 */
    boolean canShowAds() {
    	return this.canShowAdw;
    }
    
    /**
     * @return true if this state allows to change parameters
     */
    boolean canChangeParameters() {
    	return this.canChangeParameters;
    }
    
    /**
     * @return true if this state allows to request for offers
     */
    boolean canRequestAds() {
    	return this.canRequestAds;
    }
} 