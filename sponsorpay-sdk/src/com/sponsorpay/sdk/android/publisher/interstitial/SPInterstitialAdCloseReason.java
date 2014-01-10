package com.sponsorpay.sdk.android.publisher.interstitial;

public enum SPInterstitialAdCloseReason {

	/* The interstitial was dismissed for an unknown reason. */
	ReasonUnknown,
    /* The interstitial was closed because the user clicked on the ad. */
    ReasonUserClickedOnAd,
    /* The interstitial was explicitly closed by the user. */
    ReasonUserClosedAd;
	
}
