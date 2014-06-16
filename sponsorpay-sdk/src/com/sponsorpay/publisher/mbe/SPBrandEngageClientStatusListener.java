/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe;

public interface SPBrandEngageClientStatusListener {

	/**
	 * The BrandEngage client's engagement status
	 * 
	 */
	public enum SPBrandEngageClientStatus {
	     // The BrandEngage player's underlying content has been loaded and the engagement has started.
	    STARTED,
	    
	    // The engagement has finished after completing. User will be rewarded.
		CLOSE_FINISHED,
	    
	    // The engagement has finished before completing.
	    // The user might have aborted it, either explicitly (by tapping the close button) or
	    // implicitly (by switching to another app) or it was interrupted by an asynchronous event
	    // like an incoming phone call.
		CLOSE_ABORTED,
	    
		PENDING_CLOSE,
		
	    // The engagement was interrupted by an error.
		ERROR
	};
	
	/**
	 * Called when the client receives the answer after requesting for offers
	 * 
	 * @param areOffersAvaliable
	 * 			true if offers are available for this request, false otherwise
	 */
	public void didReceiveOffers(boolean areOffersAvaliable);

	/**
	 * Called when the client changes to a new status.
	 * 
	 * @param newStatus
	 * 			the status for the current engagement
	 */
	public void didChangeStatus(SPBrandEngageClientStatus newStatus);
	
}
