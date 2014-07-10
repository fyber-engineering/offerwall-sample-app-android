/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

/**
 * <p>
 * Interface that defines the methods to be implemented in order to get notifications 
 * about the status of an ad.
 * </p>
 * 
 * Used internally by the {@link SPInterstitialActivity} in order to return the proper
 * status when finished.
 */
public interface SPInterstitialAdListener {

	/**
	 * Called when the ad was shown to the user.
	 */
	public void onSPInterstitialAdShown();
	
	/**
	 * Called when the ad was dismissed.
	 * 
	 * @param reason
	 * 			The reason of the dismissal.
	 */
	public void onSPInterstitialAdClosed(SPInterstitialAdCloseReason reason);
	
	/**
	 * Called when an error occurred while showing the ad. 
	 * 
	 * @param error
	 * 			The error message
	 */
	public void onSPInterstitialAdError(String error);
	
}
