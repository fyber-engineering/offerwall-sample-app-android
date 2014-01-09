/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.mediation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.sdk.android.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationVideoEvent;

/**
 * <p>
 * Interface for SP Android Mediation adapter
 * </p>
 */
public abstract class SPMediationAdapter {

	/**
	 * Initializes the wrapped SDK, usually with the necessary credentials.
	 * @param activity
	 * 			The parent activity calling this method
	 * @return 
	 * 			true if the adapter was successfully started, false otherwise
	 */
	public abstract boolean startAdapter(Activity activity);
	
	/**
	 * @return the name of the wrapped video network.
	 */
	public abstract String getName();
	
	/**
	 * @return the current version of the adapter
	 */
	public abstract String getVersion();
	
	public abstract SPBrandEngageMediationAdapter<SPMediationAdapter> getVideoMediationAdapter();
	
	public abstract SPInterstitialMediationAdapter<SPMediationAdapter> getInterstitialMediationAdapter();
	
	public boolean supportMediationFormat(SPMediationFormat format) {
		switch (format) {
		case BrandEngage:
			return getVideoMediationAdapter() != null;
		case Interstitial:
			return getInterstitialMediationAdapter() != null;
		default:
			return false;
		}
	}

	public boolean validate(Context context, SPMediationFormat adFormat,
			SPMediationValidationEvent validationEvent,
			HashMap<String, String> contextData) {
		// validation is not required here, as SPMediationCoordinator is performing it before, but...
		switch (adFormat) {
		case BrandEngage:
			if (supportMediationFormat(adFormat)) {
				getVideoMediationAdapter().videosAvailable(context, validationEvent, contextData);
				return true;
			}
			break;
		case Interstitial:
			if (supportMediationFormat(adFormat)) {
				return getInterstitialMediationAdapter().interstitialAvailable(context, validationEvent, contextData);
			}
		default:
			break;
		}
		return false;
	}

	public void startEngagement(Activity parentActivity,SPMediationFormat adFormat, 
			SPMediationEngagementEvent engagementEvent,
			HashMap<String, String> contextData) {
		switch (adFormat) {
		// validation is not required here, as SPMediationCoordinator is performing it before, but...
		case BrandEngage:
			if (supportMediationFormat(adFormat)) {
				getVideoMediationAdapter().startVideo(parentActivity, (SPMediationVideoEvent)engagementEvent, contextData);
			}
			break;
		case Interstitial:
			if (supportMediationFormat(adFormat)) {
				getInterstitialMediationAdapter().show(parentActivity, (SPMediationEngagementEvent)engagementEvent, contextData);
			}
			break;
		default:
			break;
		}
	}
	
	
	//Helper
	
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
//		String name = e.getMethodName();
//		if (e.getMethodName().equals("runInThread")) {
//			 e = stacktrace[4];//maybe this number needs to be corrected
//			name = e.getMethodName();
//		}
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
