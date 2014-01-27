/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import android.os.AsyncTask;

import com.sponsorpay.mediation.SPMediationAdFormat;
import com.sponsorpay.mediation.SPMediationCoordinator;
import com.sponsorpay.utils.SponsorPayLogger;

public class SPInterstitialAdsProcessor extends AsyncTask<SPInterstitialAd, Void, SPInterstitialAd> {

	private static final String TAG = "SPInterstitialAdsProcessor";

	public static void processAds(SPInterstitialAd[] ads) {
		new SPInterstitialAdsProcessor().execute(ads);
	}
	
	private SPInterstitialAdsProcessor() {
	}
	
	@Override
	protected SPInterstitialAd doInBackground(SPInterstitialAd... ads) {
		Thread.currentThread().setName(TAG);
		for(SPInterstitialAd ad : ads)  {
			SponsorPayLogger.d(TAG, "Processing ad from " + ad.getProviderType());
			if (SPMediationCoordinator.INSTANCE.isProviderAvailable(ad.getProviderType(), SPMediationAdFormat.Interstitial)) {
				SponsorPayLogger.d(TAG, ad.getProviderType() + " is available, proceeding...");
				SPInterstitialClient.INSTANCE.fireEvent(ad, SPInterstitialEvent.ValidationRequest);
				if (SPInterstitialClient.INSTANCE.validateAd(ad)) {
					SponsorPayLogger.d(TAG, "Ad is available from " + ad.getProviderType());
					SPInterstitialClient.INSTANCE.fireEvent(ad, SPInterstitialEvent.ValidationFill);
					SPInterstitialClient.INSTANCE.fireEvent(null, SPInterstitialEvent.ValidationFill);
					return ad;
				} else {
					SponsorPayLogger.d(TAG, "No ad available from " + ad.getProviderType());
					SPInterstitialClient.INSTANCE.fireEvent(ad, SPInterstitialEvent.ValidationNoFill);
				}
			} else {
				SponsorPayLogger.d(TAG, ad.getProviderType() + " is not integrated");
				SPInterstitialClient.INSTANCE.fireEvent(ad, SPInterstitialEvent.NotIntegrated);
			}
		}
		SponsorPayLogger.d(TAG, "There are no ads available currently.");
		SPInterstitialClient.INSTANCE.fireEvent(null, SPInterstitialEvent.ValidationNoFill);
		return null;
	}
	
	@Override
	protected void onPostExecute(SPInterstitialAd result) {
		super.onPostExecute(result);
		SPInterstitialClient.INSTANCE.availableAd(result);
	}

}
