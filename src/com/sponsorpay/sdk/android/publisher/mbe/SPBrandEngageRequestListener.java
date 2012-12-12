/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe;

import android.content.Intent;

/**
 * Interface to be implemented by listeners notified of the results of a SponsorPay BrandEngage 
 * request.
 */
public interface SPBrandEngageRequestListener {
	/**
	 * Invoked when an available banner has been returned by the back end.
	 * 
	 * @param banner
	 *            The banner data returned by the server, enclosed in an {@link OfferBanner}
	 *            instance. Call {@link OfferBanner#getBannerView(android.app.Activity)} to get a
	 *            banner view which can be added to a view hierarchy.
	 */
	public void onSPBrandEngageOffersAvailable(Intent spBrandEngageActivity);

	/**
	 * Invoked when the back end cannot provide a banner for this request.
	 * 
	 * @param request
	 *            The {@link OfferBannerRequest} instance which sent the request.
	 */
	public void onSPBrandEngageOffersNotAvailable();

	/**
	 * Invoked when the request results in a local error, usually due to a problem connecting to the
	 * network.
	 * 
	 * @param request
	 *            The {@link OfferBannerRequest} instance which sent the request. Use
	 *            {@link OfferBannerRequest#getRequestThrownError()} to determine exactly the cause of
	 *            the error.
	 */
	public void onSPBrandEngageError(String errorMessage);

	
}
