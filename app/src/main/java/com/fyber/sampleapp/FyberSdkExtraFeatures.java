/**
 * Fyber Android SDK
 * <p/>
 * Copyright (c) 2015 Fyber. All rights reserved.
 */
package com.fyber.sampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.fyber.ads.AdFormat;
import com.fyber.currency.VirtualCurrencyErrorResponse;
import com.fyber.currency.VirtualCurrencyResponse;
import com.fyber.exceptions.IdException;
import com.fyber.reporters.InstallReporter;
import com.fyber.reporters.RewardedActionReporter;
import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.RewardedVideoRequester;
import com.fyber.requesters.VirtualCurrencyCallback;
import com.fyber.requesters.VirtualCurrencyRequester;
import com.fyber.sampleapp.fragments.FyberFragment;
import com.fyber.utils.FyberLogger;

/**
 * This class demonstrates a couple of features that the sample app doesn't show you.
 * <p> Here you can see how to take advantage of :</p>
 * <ul>
 * <li>Advertiser Reporters</li>
 * <li>Thread control on Requesters' callbacks</li>
 * <li>Different ways of using Requesters and Reporters</li>
 * <ul>
 */
public class FyberSdkExtraFeatures {


	private static final String TAG = FyberSdkExtraFeatures.class.getSimpleName();

	public static void reportInstall(Context context) {

		//if you are an advertiser that simply wants to track app installs you simply need to create an InstallReporter and call 'report' on it.
		String myAppId = "1234";
		InstallReporter.create(myAppId).report(context);
	}

	public static void reportRewardedAction(Context context) {

		//Reporting a rewarded aciton is pretty similar to reporting an app install. You just need an extra param (action id) a catch block for an IdException
		String myAppId = "1234";
		String actionId = "MY_ACTION_ID";
		try {
			RewardedActionReporter.create(myAppId, actionId).report(context);
		} catch (IdException e) {
			Log.d(TAG, e.getMessage());
		}
	}

	//Fyber SDK offers you control over thread sync on your callbacks. This method exemplifies how to do it
	public static void requestAdWithSpecificHandler(final Activity activity) {

		/*
		*
		* By default Fyber SDK uses the UI thread to call the Requester's callback.
		* This means that if you are requesting an ad from a background thread you will get your response on the UI thread.
		* However, you can override this default behaviour and have your response delivered to a specific Handler. Here is an example of how to do it:
		*
		 */

		//create a Handler from a working thread
		HandlerThread thread = new HandlerThread("handler thread", HandlerThread.MIN_PRIORITY);
		thread.start();
		Handler requesterHandler = new Handler(thread.getLooper());

		RewardedVideoRequester.create(new RequestCallback() {

			@Override
			public void onRequestError(RequestError requestError) {
				FyberLogger.d(TAG, "onRequestError");
			}

			@Override
			public void onAdAvailable(Intent intent) {
				FyberLogger.d(TAG, "on Ad available");
			}

			@Override
			public void onAdNotAvailable(AdFormat adFormat) {
				FyberLogger.d(TAG, "ad not available");
			}
		})

				//by chaining this method to your requester you are specifying the Handler where you want your callback to be called.
				//Note that by doing this the callback code will run on a working thread. You will need to sync with th UI thread if you wish to perform operations on views.
				.invokeCallbackOnHandler(requesterHandler)
				.request(activity);
	}

	public static void createRequesterFromAnotherRequester(final Activity activity) {

		/*
		* In some situations it might be useful to build a Requester from another requester.
		 * For instance, you build a video requester at some point in your code and later on you wish to have a similar requester but with a different placement id.
		 * In this scenario you can use the first to build the second and avoid passing the same params.
		 */

		//first video requester with extensive customisation
		RewardedVideoRequester rewardedVideoRequester = RewardedVideoRequester
				.create(new RequestCallback() {
					@Override
					public void onAdAvailable(Intent intent) {
						FyberLogger.d(TAG, "on Ad available");
					}

					@Override
					public void onAdNotAvailable(AdFormat adFormat) {
						FyberLogger.d(TAG, "on Ad Not available");
					}

					@Override
					public void onRequestError(RequestError requestError) {
						FyberLogger.d(TAG, "on request error");
					}
				})
				.addParameter("customParamKey", "customParaVal")
				.withPlacementId("myPlacementId");

		VirtualCurrencyRequester vcsReq = VirtualCurrencyRequester
				.create(new VirtualCurrencyCallback() {
					@Override
					public void onError(VirtualCurrencyErrorResponse virtualCurrencyErrorResponse) {
						FyberLogger.d(TAG, "VCS error received - " + virtualCurrencyErrorResponse.getErrorMessage());
					}

					@Override
					public void onSuccess(VirtualCurrencyResponse virtualCurrencyResponse) {
						FyberLogger.d(TAG, "VCS coins received - " + virtualCurrencyResponse.getDeltaOfCoins());
					}

					@Override
					public void onRequestError(RequestError requestError) {
						FyberLogger.d(TAG, "error requesting vcs: " + requestError.getDescription());
					}
				})
				.notifyUserOnReward(true)
				.forCurrencyId("coins");

		//add the vcs requester to the video requester
		rewardedVideoRequester.withVirtualCurrencyRequester(vcsReq);

		//here we spare the effort of building a different requester with all the same parameterization and simply use the first one as a basis for the second one.
		RewardedVideoRequester otherRequester = RewardedVideoRequester
				.from(rewardedVideoRequester)
						//the only thing different is the placement id
				.withPlacementId("aDifferentPlacementId");

		//this will be a separate Requester with different placementsId but will be handled by the same callbacks and have the same parameterization
		otherRequester.request(activity);
	}
}
