/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

/**
 * Represents the reason why an interstitial ad was dismissed.
 */
public enum SPInterstitialAdCloseReason {

	/**
	 *  The interstitial was dismissed for an unknown reason. 
	 */
	ReasonUnknown,
    /**
     *  The interstitial was closed because the user clicked on the ad. 
     */
    ReasonUserClickedOnAd,
    /**
     *  The interstitial was explicitly closed by the user. 
     */
    ReasonUserClosedAd,
    /**
     *  An error occurred while displaying the ad.
     */  
    ReasonError;
	
}
