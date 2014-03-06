/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.interstitial;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;

import com.applift.playads.PlayAds;
import com.applift.playads.api.PlayAdsListener;
import com.applift.playads.api.PlayAdsPromo;
import com.applift.playads.api.PlayAdsType;
import com.sponsorpay.mediation.AppLiftMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;

public class AppLiftInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<AppLiftMediationAdapter> implements PlayAdsListener{

	private boolean mIsShown = false;;

	public AppLiftInterstitialMediationAdapter(AppLiftMediationAdapter adapter) {
		super(adapter);
	}
	
	public void start(Activity activity) {
		mActivityRef = new WeakReference<Activity>(activity);
		checkForAds(null);
	}
	
	@Override
	protected void checkForAds(Context context) {
		if (getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					PlayAds.cache();	
				}
			});
		} else {
			SponsorPayLogger.e(getName(), "Unable to check for ads, needs to be run in the main thread");
		}
	}
	
	@Override
	public boolean show(Activity parentActivity) {
		PlayAds.show(parentActivity);
		return true;
	}

	@Override
	public void onCached(PlayAdsType type) {
		setAdAvailable();
	}

	@Override
	public void onShown(PlayAdsType type) {
		mIsShown  = true;
		fireImpressionEvent();
	}

	@Override
	public void onTapped(PlayAdsPromo promo) {
		fireClickEvent();		
	}
	
	@Override
	public void onError(Exception exception) {
		if (mIsShown) {
			fireShowErrorEvent(exception.getMessage());
		} else {
//			if (exception.getLocalizedMessage().contains("timed out")) {
//				return;
//			}
			fireValidationErrorEvent(exception.getMessage());
		}
	}

	@Override
	public void onClosed(PlayAdsType arg0) {
		fireCloseEvent();
	}

}
