/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial;

public interface SPIntertitialListener {

	/*
@protocol SPInterstitialClientDelegate <NSObject>

/** Called in your delegate instance to deliver the answer relaed to the checkInterstitialAvailable request.
 @param client The SPInterstitialClient delivering this answer.
 @param canShowInterstitial Whether an interstitial ad can be shown at this time.
- (void)interstitialClient:(SPInterstitialClient *)client
       canShowInterstitial:(BOOL)canShowInterstitial;

/** Called in your delegate instance to notify that an interstitial is being shown.
 @param client The SPInterstitialClient delivering this answer.
- (void)interstitialClientDidShowInterstitial:(SPInterstitialClient *)client;

/** Called in your delegate instance to notify that an interstitial is being dismissed.
 @param client The SPInterstitialClient delivering this answer.
 @param reason One of the values defined in the SPInterstitialDismissReason enum corresponding to the condition that caused dismissal of the interstitial.
- (void)interstitialClient:(SPInterstitialClient *)client
    didDismissInterstitialWithReason:(SPInterstitialDismissReason)dismissReason;

/** Called in your delegate instance to notify of an error condition.
 @param client The SPInterstitialClient delivering this answer.
 @param error An NSError instance enclosing more information about the error.
- (void)interstitialClient:(SPInterstitialClient *)client
          didFailWithError:(NSError *)error;

@end
	 */
}
