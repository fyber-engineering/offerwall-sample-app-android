/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * <p>
 * One-stop-shop class that handles and is responsible for showing a MBE 
 * engagement.
 * </p>
 * 
 * When closed, it returns the BrandEngage Client status as a bundle extra with
 * the key {@link SPBrandEngageClient#SP_ENGAGEMENT_STATUS}
 */
public class SPBrandEngageActivity extends Activity implements SPBrandEngageClientStatusListener {
	
	//used to prevent the activity closed after a redirect
	private boolean mPendingClose = false;
	
	// variable used to prevent double STATUS notification 
	private boolean mEngagementAlreadyClosed = false;

	private boolean mPlayThroughMediation = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Screen orientation locked to landscape on Gingerbread
		mPlayThroughMediation = SPBrandEngageClient.INSTANCE.playThroughMediation();
		if ( !mPlayThroughMediation &&
				(Build.VERSION.SDK_INT == 9 || Build.VERSION.SDK_INT == 10)) {
			setRequestedOrientation(
					   ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		SPBrandEngageClient.INSTANCE.setStatusListener(this);
		SPBrandEngageClient.INSTANCE.startEngagement(SPBrandEngageActivity.this, mPlayThroughMediation);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mPendingClose) {
			SPBrandEngageClient.INSTANCE.closeEngagement();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (!mPendingClose && !mPlayThroughMediation
				&& !mEngagementAlreadyClosed) {
			SPBrandEngageClient.INSTANCE.onPause();
			SPBrandEngageClient.INSTANCE.closeEngagement();
		}
	}
	
	private void closeActivity() {
		mEngagementAlreadyClosed = true; 
		finish();
	}
	
	@Override
	protected void onDestroy() {
		SPBrandEngageClient.INSTANCE.setStatusListener(null);
		super.onDestroy();
	}
	
	
	//Status listener
	@Override
	public void didReceiveOffers(boolean areOffersAvaliable) {
		//do nothing
	}
	
	@Override
	public void didChangeStatus(SPBrandEngageClientStatus newStatus) {
		switch (newStatus) {
		case CLOSE_FINISHED:
			setResultAndClose(SPBrandEngageClient.SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE);
			break;
		case CLOSE_ABORTED:
			setResultAndClose(SPBrandEngageClient.SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE);
			break;
		case ERROR:
			setResultAndClose(SPBrandEngageClient.SP_REQUEST_STATUS_PARAMETER_ERROR);
			break;
		case PENDING_CLOSE:
			mPendingClose = true;
			break;
		default:
			break;
		}
	}
	
	private void setResultAndClose(String intentExtra) {
		Intent intent = new Intent();
		intent.putExtra(SPBrandEngageClient.SP_ENGAGEMENT_STATUS, intentExtra);
		setResult(RESULT_OK, intent);
		closeActivity();
	}
}
