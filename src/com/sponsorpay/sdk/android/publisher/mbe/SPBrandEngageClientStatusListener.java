/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe;

public interface SPBrandEngageClientStatusListener {


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
	
	public void didReceiveOffers(boolean areOffersAvaliable);

	public void didChangeStatus( SPBrandEngageClientStatus newStatus);
	
}
