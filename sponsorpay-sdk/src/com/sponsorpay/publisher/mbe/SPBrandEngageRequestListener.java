/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe;

import android.content.Intent;

/**
 * Interface to be implemented by listeners notified of the results of a SponsorPay BrandEngage 
 * request.
 */
public interface SPBrandEngageRequestListener {
	/**
	 * Invoked when an offer is available for this request.
	 * 
	 * @param spBrandEngageActivity
	 *            The intent for the {@link SPBrandEngageActivity} that can launched for starting the engagement.
	 */
	public void onSPBrandEngageOffersAvailable(Intent spBrandEngageActivity);

	/**
	 * Invoked when the back end cannot provide any offers for this request.
	 * 
	 */
	public void onSPBrandEngageOffersNotAvailable();

	/**
	 * Invoked when the request results in a local error, usually due to a problem connecting to the
	 * network.
	 * 
	 * @param errorMessage
	 *            The description of the error for the request.
	 */
	public void onSPBrandEngageError(String errorMessage);

	
}
