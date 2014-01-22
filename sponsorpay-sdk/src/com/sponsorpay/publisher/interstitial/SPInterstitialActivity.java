/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class SPInterstitialActivity extends Activity implements SPInterstitialAdStateListener {
	
	public final static String SP_AD_STATUS = "AD_STATUS"; 

	public final static String SP_AD_STATUS_ERROR = "AD_STATUS_ERROR"; 
	public final static String SP_ERROR_MESSAGE = "ERROR_MESSAGE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		SPInterstitialClient.INSTANCE.setAdStateListener(SPInterstitialActivity.this);
		SPInterstitialClient.INSTANCE.showInterstitial(SPInterstitialActivity.this);
	}
	
	@Override
	public void onSPInterstitialAdShown() {
		//do nothing
	}
	
		
	@Override
	public void onSPInterstitialAdClosed(SPInterstitialAdCloseReason reason) {
		Intent intent = new Intent();
		intent.putExtra(SP_AD_STATUS, reason);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onSPInterstitialAdError(String error) {
		Intent intent = new Intent();
		intent.putExtra(SP_AD_STATUS, SP_AD_STATUS_ERROR);
		intent.putExtra(SP_ERROR_MESSAGE, error);
		setResult(RESULT_OK, intent);
		finish();
	}

}
