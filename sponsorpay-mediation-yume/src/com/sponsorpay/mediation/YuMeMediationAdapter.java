/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Set;

import android.app.Activity;
import android.os.Build;

import com.sponsorpay.mediation.interstitial.YuMeInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.yume.android.sdk.YuMeAdBlockType;
import com.yume.android.sdk.YuMeAdParams;
import com.yume.android.sdk.YuMeException;
import com.yume.android.sdk.YuMeSDKInterfaceImpl;
import com.yume.android.sdk.YuMeStorageMode;

public class YuMeMediationAdapter extends SPMediationAdapter {
			
	private static final String TAG = "YuMeAdapter";

	private static final String ADAPTER_VERSION = "1.0.0";

	private static final String ADAPTER_NAME = "Yume";
	
	private static final String APP_ID = "app.id";

	private YuMeInterstitialMediationAdapter interstitialAdapter;

	private YuMeSDKInterfaceImpl yumeSDKInterface;


	@Override
	public boolean startAdapter(Activity activity) {
		if (Build.VERSION.SDK_INT >= 8) {
			SponsorPayLogger.d(TAG, "Starting YuMe adapter");
			try {
				yumeSDKInterface = new YuMeSDKInterfaceImpl();
				SponsorPayLogger.d(TAG, "YuMe SDK version " + yumeSDKInterface.YuMeSDK_GetVersion());
				String appId = "aaaa"; 
						//SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_ID, String.class);
				if (StringUtils.notNullNorEmpty(appId)) {
					interstitialAdapter = new YuMeInterstitialMediationAdapter(this, activity);
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							YuMeAdBlockType adBlockType = YuMeAdBlockType.PREROLL;
							try {
								YuMeAdParams adParams = new YuMeAdParams();
								adParams.adServerUrl = "http://shadow01.yumenetworks.com/";
								adParams.domainId = "1535FyPGabIb";
								adParams.storageSize = 10; //storage size in MBs
								adParams.eStorageMode = YuMeStorageMode.EXTERNAL_STORAGE;
								yumeSDKInterface.YuMeSDK_Init(adParams, interstitialAdapter);
								yumeSDKInterface.YuMeSDK_InitAd(adBlockType);
							} catch (YuMeException e) {
								SponsorPayLogger.e(TAG,e.getLocalizedMessage(), e);
							}
						}
					});
					return true;
				}
				SponsorPayLogger.d(TAG, "App Id  must have a valid value!");
			} catch (YuMeException e) {
				SponsorPayLogger.e(TAG,e.getLocalizedMessage(), e);
			}
		} else {
			SponsorPayLogger.d(TAG, "The OS version is not compatible with the SDK.");
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

	public YuMeSDKInterfaceImpl getYuMeSDKInterface() {
		return yumeSDKInterface;
	}

}
