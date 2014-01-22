package com.sponsorpay.sdk.android.publisher.interstitial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class SPInterstitialActivity extends Activity implements SPInterstitialAdStateListener {
	
	public final static String SP_AD_STATUS = "AD_STATUS"; 

	public final static String SP_AD_STATUS_ERROR = "AD_STATUS_ERROR"; 
	public final static String SP_ERROR_MESSAGE = "ERROR_MESSAGE";

//	private RelativeLayout mLayout; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		super.onCreate(savedInstanceState);
//		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		requestWindowFeature(Window.FEATURE_NO_TITLE);

//		mLayout = new RelativeLayout(this);
//		mLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//		mLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		setContentView(mLayout);
		
		if(!SPInterstitialClient.INSTANCE.showInterstitial(SPInterstitialActivity.this)) {
			finishActivity(RESULT_CANCELED);
		} else {
			SPInterstitialClient.INSTANCE.setAdStateListener(SPInterstitialActivity.this);
		}
	}
	
	@Override
	public void onSPInterstitialAdShown() {
		//do nothing
	}
	
//	@Override
//	protected void onPause() {
////		removeViews();
//		super.onPause();
//	}
//
//	@Override
//	protected void onDestroy() {
////		removeViews();
//		super.onDestroy();
//	}
	
	
//	private void removeViews() {
//		View view = getWindow().
////				getWindowManager().
//				getDecorView().getRootView();
//		if (view instanceof ViewGroup) {
//			((ViewGroup)view).removeAllViews();
//		}
//
////		View view = findViewById(android.R.id.content);
////		if (view instanceof ViewGroup) {
////			((ViewGroup)view).removeAllViews();
////		}
//	}
	
		
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
