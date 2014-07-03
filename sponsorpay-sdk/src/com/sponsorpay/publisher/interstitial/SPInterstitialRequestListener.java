/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import android.content.Intent;

/**
 * Interface to be implemented by listeners notified of the results of an 
 * Interstitial request.
 */
public interface SPInterstitialRequestListener {
	
	/**
	 * Invoked when an ad is available for this request.
	 * 
	 * @param interstitialActivity
	 * 			The intent for the {@link SPInterstitialActivity} that can be 
	 *          launched for showing the ad.
	 */
	public void onSPInterstitialAdAvailable(Intent interstitialActivity);
	
	/**
	 * Invoked when the back end cannot provide any ad for this request.
	 * 
	 */
	public void onSPInterstitialAdNotAvailable();
	
	/**
	 * Invoked when the request results in a local error, usually due to a problem connecting to the
	 * network.
	 * 
	 * @param errorMessage
	 * 			The description of the error for the request.
	 */
	public void onSPInterstitialAdError(String errorMessage);
	
}
