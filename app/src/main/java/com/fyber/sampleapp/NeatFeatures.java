/**
 * Fyber Android SDK
 * <p/>
 * Copyright (c) 2015 Fyber. All rights reserved.
 */
package com.fyber.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.fyber.currency.VirtualCurrencyErrorResponse;
import com.fyber.currency.VirtualCurrencyResponse;
import com.fyber.exceptions.IdException;
import com.fyber.reporters.InstallReporter;
import com.fyber.reporters.RewardedActionReporter;
import com.fyber.requesters.OfferWallRequester;
import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.RewardedVideoRequester;
import com.fyber.requesters.VirtualCurrencyCallback;
import com.fyber.requesters.VirtualCurrencyRequester;
import com.fyber.utils.FyberLogger;

/**
 * This class demonstrates a couple of features that the sample app doesn't show you.
 * <p> Here you can see how to take advantage of :</p>
 * <ul>
 * <li>Advertiser reporters</li>
 * <li>Thread control on Requesters</li>
 * <li>Different ways of using requesters and reporters</li>
 * <li>Other stuff</li>
 * <ul>
 */
public class NeatFeatures {


	private static final String TAG = NeatFeatures.class.getSimpleName();

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

	//FIXME: this needs a new Fyber sdk rc to work. (ui sync stuff)
	public static void threadControlOverRequesterCallback(Context context) {
		/*
		*
		* Fyber sdk offers you control over thread sync on your callbacks.
		* By default Fyber sdk uses the UI thread to call the Requester's callback.
		* This means that if you are requesting an ad from a background thread you will get your response on the UI thread.
		* However, you can override this default behaviour and have your response delivered to a specific Handler. Here is an example of how to do it:
		*
		 */

		//simulate a Handler from a working thread
		HandlerThread thread = new HandlerThread("handler thread", HandlerThread.MIN_PRIORITY);
		thread.start();
		Handler requesterHandler = new Handler(thread.getLooper());

		final RewardedVideoRequester requesterWithBackgroundCallback = RewardedVideoRequester.create(new RequestCallback() {

			@Override
			public void onRequestError(RequestError requestError) {
				FyberLogger.d(TAG, "onRequestError");
			}

			@Override
			public void onAdAvailable(Intent intent) {
				FyberLogger.d(TAG, "onAdAvailable worker thread ?");
				//this code will run on a working thread, it is your responsibility to sync it with the UI thread. Uncommenting the line below will result in a crash.
//				Toast.makeText(getActivity(), "on adAvailable", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onAdNotAvailable() {
				FyberLogger.d(TAG, "onAdNotAvailable");
			}
		});

		//by chaining this method to your requester you are specifying the Handler where you want your callback to be called.
		requesterWithBackgroundCallback.notifyCallbackHandler(requesterHandler);

		requesterWithBackgroundCallback.request(context);
	}

	//FIXME: the public API still needs a couple of changes: 'notifyUserOnCompletion' is missing and should be renamed. Method 'dicas' needs a name and might replace 'forCurrencyId'.
	public static void createRequesterFromAnotherRequester(Context context) {

		/*
		* In some situations it might be useful to build a Requester from another requester.
		 * For instance, you build a video requester at some point in your code and later on you wish to have a similar requester but with a different placement id.
		 * In this scenario you can use the first to build the second and avoid passing the same params.
		 */

		//first video requester with extensive customisation
		RewardedVideoRequester requester = RewardedVideoRequester
				.create(new RequestCallback() {
					@Override
					public void onAdAvailable(Intent intent) {
						//do stuff
					}

					@Override
					public void onAdNotAvailable() {

					}

					@Override
					public void onRequestError(RequestError requestError) {

					}
				})
				.addParameter("customParamKey", "customParaVal")
				.withPlacementId("myPlacementId");

		VirtualCurrencyRequester vcsReq = VirtualCurrencyRequester
				.from(requester) //here is another example where you can create
				.notifyUserOnCompletion(true)
				.forCurrencyId("myCurrencyId")
				.withCallback(new VirtualCurrencyCallback() {
					@Override
					public void onError(VirtualCurrencyErrorResponse virtualCurrencyErrorResponse) {

					}

					@Override
					public void onSuccess(VirtualCurrencyResponse virtualCurrencyResponse) {

					}

					@Override
					public void onRequestError(RequestError requestError) {

					}
				});


		requester.dicas(vcsReq); // method that replaces adding a vcs and a currency id. Instead we pass a vcsCallback that has both.

		//here we spare the effort of building a different requester with all the same parameterization and simply use the first one as a basis for the second one.
		RewardedVideoRequester otherRequester = RewardedVideoRequester
				.from(requester)
				.withPlacementId("aDifferentPlacementId");

		//these will be separate requests with different placementsIds but will be handled by the same callbacks and have the same parameterization
		requester.request(context);
		otherRequester.request(context);
	}

	//Other stuff......
}
