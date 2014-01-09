package com.sponsorpay.sdk.android.publisher.interstitial;

import android.os.AsyncTask;

public class SPInterstitialAdsProcessorTask extends AsyncTask<SPInterstitialAd, Void, SPInterstitialAd> {

	@Override
	protected SPInterstitialAd doInBackground(SPInterstitialAd... ads) {
		for(SPInterstitialAd ad : ads)  {
			if (SPInterstitialClient.INSTANCE.validateAd(ad)) {
				return ad;
			};
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(SPInterstitialAd result) {
		super.onPostExecute(result);
		SPInterstitialClient.INSTANCE.availableAd(result);
	}

}
