package com.sponsorpay.sdk.mediation;

import java.util.Set;

import android.app.Activity;

import com.applift.playads.PlayAds;
import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.mediation.interstitial.AppLiftInterstitialMediationAdapter;

public class AppLiftMediationAdapter extends SPMediationAdapter{

	private static final String TAG = "AppLiftAdapter";
	
	private static final String ADAPTER_VERSION = "1.0.0";
	private static final String ADAPTER_NAME = "AppLift";
	
	public static final String APP_ID = "app.id";
	public static final String APP_SECRET = "app.secret";
	
	private AppLiftInterstitialMediationAdapter mInterstitialAdapter;
	
	@Override
	public boolean startAdapter(final Activity activity) {
		SponsorPayLogger.d(TAG, "Starting AppLift adapter");// - SDK version " + PlayAds.getSDKVersion());
		final Integer appId = Integer.decode(SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_ID, String.class));
		final String appSecret = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_SECRET, String.class);
		if (appId != null && StringUtils.notNullNorEmpty(appSecret)) {
			mInterstitialAdapter = new AppLiftInterstitialMediationAdapter(this);
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					PlayAds.init(activity, appId, appSecret);
					mInterstitialAdapter.start();
					PlayAds.addListener(mInterstitialAdapter);
				}
			});
			return true;
		}
		SponsorPayLogger.i(TAG, "One of the provided values (appId/appSecret) is not valid");
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
	public AppLiftInterstitialMediationAdapter getInterstitialMediationAdapter() {
		return mInterstitialAdapter;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

}
