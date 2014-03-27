/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */
package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.sponsorpay.mediation.YuMeMediationAdapter;
import com.sponsorpay.mediation.helper.YuMeConfigurationsHelper;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.yume.android.sdk.YuMeAdBlockType;
import com.yume.android.sdk.YuMeAdEvent;
import com.yume.android.sdk.YuMeAppInterface;
import com.yume.android.sdk.YuMeException;
import com.yume.android.sdk.YuMeParentViewInfo;

public class YuMeInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<YuMeMediationAdapter> implements YuMeAppInterface {

	private static final int YUME_ACTIVITY_REQUEST_CODE = 3241;

	private static final String TAG = "YuMeInterstitialMediationAdapter";
	
	private boolean mAdPlaying = false;
	private Activity mYuMeActivity;
	
	public YuMeInterstitialMediationAdapter(YuMeMediationAdapter adapter) {
		super(adapter);
		YuMeConfigurationsHelper.setYuMeInterstitialAdapter(this);
	}

	@Override
	protected boolean show(Activity parentActivity) {
		mAdPlaying = false;
		Intent intent = new Intent(parentActivity, YuMeActivity.class);
		parentActivity.startActivityForResult(intent, YUME_ACTIVITY_REQUEST_CODE);
		return true;
	}

	@Override
	protected void checkForAds(Context context) {
		//don't do anything here
	}
	
	private Activity getYuMeActivity() {
		return mYuMeActivity;
	}
	
	public void setYuMeActivity( Activity activity) {
		mYuMeActivity = activity;
	}

	//YuMeAppInterface implementation
	
	@Override
	public void YuMeApp_EventListener(YuMeAdBlockType adBlockType, YuMeAdEvent adEvent,
			String eventInfo) {
		switch (adEvent) {
		case AD_READY:
			//do nothing
			break;
		case AD_AND_ASSETS_READY:
			// set ad availability
			setAdAvailable();
			break;
		case AD_NOT_READY:
			//do nothing
			break;
		case AD_PRESENT:
			//do nothing
			break;
		case AD_PLAYING:
			//set the ad as playing
			mAdPlaying = true;
			break;
		case AD_ABSENT:
			//do nothing
			break;
		case AD_COMPLETED:
			Log.d("Event", "AD COMPLETED");
			mAdPlaying = false;
			//close the activity
			closeYuMeActivity();
			break;
		case AD_ERROR:
			// fire show error event
			fireShowErrorEvent(eventInfo);
			try {
				// re init the interface for more ads
				YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
			} catch (YuMeException e) {
				SponsorPayLogger.e(TAG, e.getMessage(), e);
			}
			break;
		case AD_EXPIRED:
			// ads expired even though they are not shown
			try {
				// re init the interface for more ads
				YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
			} catch (YuMeException e) {
				SponsorPayLogger.e(TAG, e.getMessage(), e);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public Context YuMeApp_GetActivityContext() {
		return getYuMeActivity();
	}

	@Override
	public Context YuMeApp_GetApplicationContext() {
		if (getActivity() != null) {
			return getActivity().getApplicationContext();
		}
		return null;
	}

	@Override
	public YuMeParentViewInfo YuMeApp_GetParentViewInfo() {
		Context appContext =  YuMeApp_GetApplicationContext();
		if (appContext != null) {
			Display display = ((WindowManager) appContext
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			DisplayMetrics displayMetrics = new DisplayMetrics();
			display.getMetrics(displayMetrics);
			YuMeParentViewInfo parentViewInfo = new YuMeParentViewInfo();
			parentViewInfo.width = displayMetrics.widthPixels;
			parentViewInfo.height = displayMetrics.heightPixels;
			parentViewInfo.left = 0;
			parentViewInfo.top = 0;
			parentViewInfo.statusBarAndTitleBarHeight = 0;
			return parentViewInfo;
		}
		return null;
	}

	public void closeYuMeActivity() {
		getActivity().finishActivity(YUME_ACTIVITY_REQUEST_CODE);
	}
	
	public void notifyClose() {
		// fire close event 
		fireCloseEvent();
		if (mAdPlaying) {
			// if the ad is playing, stop it and re init the interface
			try {
				YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_StopAd();
				YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
				// sets ad availability
				setAdAvailable();
			} catch (YuMeException e) {
				SponsorPayLogger.e(TAG, e.getMessage(), e);
			}
		} else {
			// sets ad availability
			setAdAvailable();
		}
	}

	public void fireImpression() {
		fireImpressionEvent();
	}

	public void backButtonPressed() {
		try {
			// notify interface about the back button press
			YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_BackKeyPressed();
			mAdPlaying = false;
			// re init for more ads
			YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
		} catch (YuMeException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		}
	}
	
}
