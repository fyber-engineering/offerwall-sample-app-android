package com.sponsorpay.sdk.android.publisher.interstitial;

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
//		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		super.onCreate(savedInstanceState);
//		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//		RelativeLayout layout = new RelativeLayout(this);
//		layout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(layout);
		
		if(!SPInterstitialClient.INSTANCE.showInterstitial(this)) {
			finishActivity(RESULT_CANCELED);
		} else {
			SPInterstitialClient.INSTANCE.setAdStateListener(this);
		}
	}
	
	@Override
	public void onSPInterstitialAdShown() {
		//do nothing
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
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
