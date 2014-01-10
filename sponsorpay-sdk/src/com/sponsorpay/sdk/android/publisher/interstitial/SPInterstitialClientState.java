/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial;

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
    // An offer is preloaded and ready to show
    READY_TO_SHOW_OFFERS(true, true, true),
    // An interstitial is currently being shown
    SHOWING_OFFERS(false, false, false);
    
    private final boolean canShowOffers;
    private final boolean canChangeParameters;
	private final boolean canRequestOffers;
    
	SPInterstitialClientState(boolean canShowOffers,
			boolean canRequestOffers, boolean canChangeParameters) {
		this.canShowOffers = canShowOffers;
		this.canRequestOffers = canRequestOffers;
		this.canChangeParameters = canChangeParameters;
	}
    
	/**
	 * 
	 * @return true if this state allows the start of an engagement
	 */
    boolean canShowOffers() {
    	return this.canShowOffers;
    }
    
    /**
     * 
     * @return true if this state allows to change RewardedVideo parameters
     */
    boolean canChangeParameters() {
    	return this.canChangeParameters;
    }
    
    /**
     * 
     * @return true if this state allows to request for offers
     */
    boolean canRequestOffers() {
    	return this.canRequestOffers;
    }
} 