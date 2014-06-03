/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe;

/**
 * The BrandEngage client internal status 
 */
public enum SPBrandEngageOffersStatus {
	// No offers are ready to show, must do a request before starting an engagement
	MUST_QUERY_SERVER_FOR_OFFERS(false, true, true),
	// Querying the server for offers
    QUERYING_SERVER_FOR_OFFERS(false, false, false),
    // An offer is preloaded and ready to show
    READY_TO_SHOW_OFFERS(true, true, true),
    // An engagement is currently on is way
    SHOWING_OFFERS(true, false, false),
    // the engagement has been successful
    USER_ENGAGED(true, true, false);
    
    private final boolean canShowOffers;
    private final boolean canChangeParameters;
	private final boolean canRequestOffers;
    
	SPBrandEngageOffersStatus(boolean canShowOffers,
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
     * @return true if this state allows to change BrandEngage parameters
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