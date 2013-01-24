/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe;

public enum SPBrandEngageOffersStatus {
    MUST_QUERY_SERVER_FOR_OFFERS(false, true, true),
    QUERYING_SERVER_FOR_OFFERS(false, false, false),
    READY_TO_SHOW_OFFERS(true, true, true),
    SHOWING_OFFERS(true, false, false),
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
    
    boolean canShowOffers() {
    	return this.canShowOffers;
    }
    
    boolean canChangeParameters() {
    	return this.canChangeParameters;
    }
    
    boolean canRequestOffers() {
    	return this.canRequestOffers;
    }
} 