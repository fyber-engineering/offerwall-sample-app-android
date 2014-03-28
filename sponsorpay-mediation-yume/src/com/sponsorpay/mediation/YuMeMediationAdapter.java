/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;
import android.os.Build;

import com.sponsorpay.mediation.helper.YuMeConfigurationsHelper;
import com.sponsorpay.mediation.interstitial.YuMeInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.yume.android.sdk.YuMeAdBlockType;
import com.yume.android.sdk.YuMeAdParams;
import com.yume.android.sdk.YuMeException;
import com.yume.android.sdk.YuMeSDKInterfaceImpl;

public class YuMeMediationAdapter extends SPMediationAdapter {
			
	public static final String ADAPTER_NAME = "Yume";

	private static final String TAG = "YuMeAdapter";

	private static final String ADAPTER_VERSION = "1.0.0";

	
	private YuMeInterstitialMediationAdapter interstitialAdapter;

	private YuMeSDKInterfaceImpl yumeSDKInterface;


	@Override
	public boolean startAdapter(Activity activity) {
		if (Build.VERSION.SDK_INT >= 8) {
			SponsorPayLogger.d(TAG, "Starting YuMe adapter");
			final YuMeAdParams adParams = YuMeConfigurationsHelper.getAdParams();
			if (adParams != null) {
				try {
					yumeSDKInterface = new YuMeSDKInterfaceImpl();
					SponsorPayLogger.d(TAG, "YuMe SDK version " + yumeSDKInterface.YuMeSDK_GetVersion());
					interstitialAdapter = new YuMeInterstitialMediationAdapter(this, activity);
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							YuMeAdBlockType adBlockType = YuMeAdBlockType.PREROLL;
							try {
								
								yumeSDKInterface.YuMeSDK_Init(adParams, interstitialAdapter);
								yumeSDKInterface.YuMeSDK_InitAd(adBlockType);
								
								YuMeConfigurationsHelper.setYuMeSDKInterface(yumeSDKInterface);
							} catch (YuMeException e) {
								SponsorPayLogger.e(TAG,e.getLocalizedMessage(), e);
							}
						}
					});
					return true;
				} catch (YuMeException e) {
					SponsorPayLogger.e(TAG,e.getLocalizedMessage(), e);
				}
			} else {
				SponsorPayLogger.e(TAG, "YuMeAdParams is missing a required field.");
			}
		} else {
			SponsorPayLogger.e(TAG, "The OS version is not supported by the SDK.");
		}
		return false;
	}

	@Override
	public String getName() {
		return ADAPTER_NAME;
	}

	@Override
	public String getVersion() {
		return ADAPTER_VERSION;
	}
	

	@Override
	public SPBrandEngageMediationAdapter<SPMediationAdapter> getVideoMediationAdapter() {
		return null;
	}
	
	@Override
	public YuMeInterstitialMediationAdapter getInterstitialMediationAdapter() {
		return interstitialAdapter;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

}
