/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPMediationValidationEvent;
import com.sponsorpay.publisher.mbe.mediation.SPMediationVideoEvent;

/**
 * <p>
 * Interface for SP Android Mediation adapter
 * </p>
 */
public abstract class SPMediationAdapter {

	/**
	 * Initializes the wrapped SDK, usually with the necessary credentials.
	 * 
	 * @param activity
	 * 			The parent activity calling this method
	 * @return 
	 * 			true if the adapter was successfully started, false otherwise
	 */
	public abstract boolean startAdapter(Activity activity);
	
	/**
	 * @return the name of the wrapped network.
	 */
	public abstract String getName();
	
	/**
	 * @return the current version of the adapter
	 */
	public abstract String getVersion();
	
	public abstract SPBrandEngageMediationAdapter<? extends SPMediationAdapter> getVideoMediationAdapter();
	
	public abstract SPInterstitialMediationAdapter<? extends SPMediationAdapter> getInterstitialMediationAdapter();
	
	public boolean supportMediationFormat(SPMediationAdFormat format) {
		switch (format) {
		case RewardedVideo:
			return getVideoMediationAdapter() != null;
		case Interstitial:
			return getInterstitialMediationAdapter() != null;
		default:
			return false;
		}
	}

	/* ======================================================
	 *                   Rewarded videos
	 * ======================================================
	 */

	public void validateVideoNetwork(Context context, SPMediationValidationEvent validationEvent,
			HashMap<String, String> contextData) {
		// validation is not required here, as SPMediationCoordinator is performing it before, but...
		SPBrandEngageMediationAdapter<? extends SPMediationAdapter> videoMediationAdapter = getVideoMediationAdapter();
		if (videoMediationAdapter != null) {
			videoMediationAdapter.videosAvailable(context, validationEvent, contextData);
		}
	}

	public void startVideoEngagement(Activity parentActivity, SPMediationVideoEvent engagementEvent,
			HashMap<String, String> contextData) {
		// validation is not required here, as SPMediationCoordinator is performing it before, but...
		SPBrandEngageMediationAdapter<? extends SPMediationAdapter> videoMediationAdapter = getVideoMediationAdapter();
		if (videoMediationAdapter != null) {
			getVideoMediationAdapter().startVideo(parentActivity,
					(SPMediationVideoEvent) engagementEvent, contextData);
		}
	}
	
	/* ======================================================
	 *                   Interstitials
	 * ======================================================
	 */
	
	public boolean validateInterstitialNetwork(Context context, SPInterstitialAd ad) {
		// validation is not required here, as SPMediationCoordinator is performing it before, but...
		SPInterstitialMediationAdapter<? extends SPMediationAdapter> interstitialMediationAdapter = getInterstitialMediationAdapter();
		if (interstitialMediationAdapter != null) {
			return interstitialMediationAdapter.isAdAvailable(context, ad);
		}
		return false;
	}
	
	public boolean showInterstitial(Activity parentActivity, SPInterstitialAd ad) {
		// validation is not required here, as SPMediationCoordinator is performing it before, but...
		SPInterstitialMediationAdapter<? extends SPMediationAdapter> interstitialMediationAdapter = getInterstitialMediationAdapter();
		if (interstitialMediationAdapter != null) {
			return interstitialMediationAdapter.show(parentActivity, ad);
		}
		return false;
	}
	
	
	/* ======================================================
	 *                        Helpers
	 * ======================================================
	 */
	
	protected abstract Set<? extends Object> getListeners();
	
	@SuppressWarnings("rawtypes")
	protected  void notifyListeners(final Object[] args, final Class[] classes) {
		runNotifyingThread(args, classes);
	}
	
	protected void notifyListeners(Object... args) {
		@SuppressWarnings("rawtypes")
		Class[] classes;
		if (args != null) {
			classes = new Class[args.length];
			for (int i = 0 ; i < args.length ; i++) {
				classes[i] = args[i].getClass();
			}
		} else {
			classes = new Class[0];
		}
		runNotifyingThread(args, classes);
	}
	
	@SuppressWarnings("rawtypes")
	private void runNotifyingThread(final Object[] args, final Class[] classes) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[4];//maybe this number needs to be corrected
		final String methodName = e.getMethodName();
		new Thread("EventBroadcaster") {
			public void run() {
				for (Object listener : getListeners()) {
					try {
						Method method = listener.getClass().getDeclaredMethod(methodName, classes);
						method.invoke(listener, args);
					} catch (SecurityException e) {
					} catch (NoSuchMethodException e) {
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					} catch (InvocationTargetException e) {
					}
				}
			};
		}.start();
	}
}
