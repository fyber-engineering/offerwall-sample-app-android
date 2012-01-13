/**

 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.sponsorpay.sdk.android.HostInfo;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;

/**
 * <p>
 * Performs the request for the interstitial in the background, notifies a registered listener of
 * the results and shows the {@link InterstitialActivity} if and ad is returned.
 * </p>
 * <p>
 * Shows a progress dialog on top of the calling activity while it contacts the SponsorPay’s servers
 * to determine whether there’s an interstitial available.
 * </p>
 * <p>
 * If an ad is available, it will continue showing the loading progress dialog while the ad contents
 * are loaded in the background. The entire process must happen in less than
 * {@value #LOADING_TIMEOUT_SECONDS_DEFAULT} seconds (or a custom amount of time that can be
 * configured, see {@link #setLoadingTimeoutSecs(int)}). Otherwise a timeout error will be triggered
 * and the loading progress dialog will disappear without showing the ad.
 * </p>
 * <p>
 * When the contents of the ad finish loading, the loading progress view dialog will disappear and
 * the interstitial will be shown on the screen (by showing the {@link InterstitialActivity}).
 * </p>
 * <p>
 * If no ad is available for this request, the loading progress dialog will disappear too and the
 * process will be completed.
 * </p>
 * <p>
 * An {@link InterstitialLoadingStatusListener} can be defined which will be notified of interesting
 * events in the life of the interstitial.
 * </p>
 */
public class InterstitialLoader implements AsyncRequest.ResultListener {
	/**
	 * Interface to be implemented by parties interested in being notified of interesting events in
	 * the life of the interstitial.
	 */
	public interface InterstitialLoadingStatusListener {
		/**
		 * Invoked when SponsorPay’s backend has returned an ad. The {@link InterstitialActivity} is
		 * about to be shown.
		 */
		void onWillShowInterstitial();

		/**
		 * Invoked when a response has been received from SponsorPay’s backend indicating that no
		 * interstitial ad is available for this particular request.
		 */
		void onNoInterstitialAvailable();

		/**
		 * Invoked when the interstitial ad could not be requested or loaded due to an error. The
		 * device might not have a working connection to the Internet.
		 */
		void onInterstitialRequestError();

		/**
		 * Invoked when the interstitial ad request or the loading of the interstitial contents
		 * could not be performed within the amount of time defined by the loading timeout value.
		 */
		void onInterstitialLoadingTimeOut();
	}

	public static final String LOG_TAG = "InterstitialLoader";

	private static final boolean SHOULD_INTERSTITIAL_REMAIN_OPEN_DEFAULT = false;
	private static final int LOADING_TIMEOUT_SECONDS_DEFAULT = 5;
	private static final String SKIN_NAME_DEFAULT = "DEFAULT";

	private static final String INTERSTITIAL_PRODUCTION_BASE_URL = "http://iframe.sponsorpay.com/mobile";
	private static final String INTERSTITIAL_STAGING_BASE_URL = "http://staging.iframe.sponsorpay.com/mobile";

	private static final String INTERSTITIAL_PRODUCTION_DOMAIN = "http://iframe.sponsorpay.com";
	private static final String INTERSTITIAL_STAGING_DOMAIN = "http://staging.iframe.sponsorpay.com";

	private static final String URL_PARAM_INTERSTITIAL_KEY = "interstitial";

	private static final String URL_PARAM_SKIN_KEY = "skin";
	private static final String URL_PARAM_BACKGROUND_KEY = "background";

	private static final int MILLISECONDS_IN_SECOND = 1000;

	/**
	 * Static counter which gets incremented every time an interstitial ad is returned.
	 */
	private static int sInterstitialAvailableResponseCount = 0;

	/**
	 * The activity which created this InterstitialLoader instance. Used to launch the
	 * {@link InterstitialActivity} and to attach the loading progress dialog to.
	 */
	private Activity mCallingActivity;
	private String mUserId;
	private HostInfo mHostInfo;
	private InterstitialLoadingStatusListener mLoadingStatusListener;

	private String mBackgroundUrl = "";
	private String mSkinName = SKIN_NAME_DEFAULT;
	private boolean mShouldStayOpen = SHOULD_INTERSTITIAL_REMAIN_OPEN_DEFAULT;
	private int mLoadingTimeoutSecs = LOADING_TIMEOUT_SECONDS_DEFAULT;

	private AsyncRequest mAsyncRequest;
	private Runnable mCancelLoadingOnTimeOut;
	private Handler mHandler;

	/**
	 * Loading progress dialog.
	 */
	private ProgressDialog mProgressDialog;

	/**
	 * Initializes a new IntestitialLoader instance.
	 * 
	 * @param callingActivity
	 *            The activity from which the loading of the interstitial is requested.
	 * @param userId
	 *            The current user of the host application.
	 * @param hostInfo
	 *            {@link HostInfo} with information from the host device and publisher application.
	 * @param loadingStatusListener
	 *            {@link InterstitialLoadingStatusListener} to register to be notified of events in
	 *            the interstitial lifecycle.
	 */
	public InterstitialLoader(Activity callingActivity, String userId, HostInfo hostInfo,
			InterstitialLoadingStatusListener loadingStatusListener) {

		mCallingActivity = callingActivity;
		mUserId = userId;
		mHostInfo = hostInfo;
		mLoadingStatusListener = loadingStatusListener;

		mHandler = new Handler();
	}

	/**
	 * Can be set to the absolute URL of an image to use as background graphic for the interstitial.
	 * Must include the protocol scheme (http:// or https://) at the beginning of the URL. Leave it
	 * null for no custom background.
	 * 
	 * @param backgroundUrl
	 */
	public void setBackgroundUrl(String backgroundUrl) {
		mBackgroundUrl = backgroundUrl;
	}

	/**
	 * Used to specify the behavior of the interstitial once the user clicks on the presented ad and
	 * is redirected outside the host publisher app. The default behavior is to close the
	 * interstitial and let the user go back to the activity that called the interstitial when they
	 * come back to the app. If you want the interstitial not to close until the user does it
	 * explicitly, set this parameter to true.
	 * 
	 * @param shouldStayOpen
	 */
	public void setShouldStayOpen(boolean shouldStayOpen) {
		mShouldStayOpen = shouldStayOpen;
	}

	/**
	 * Used to specify the name of a custom skin or template for the requested interstitial. Leaving
	 * it null will make the interstitial fall back to the DEFAULT template.
	 * 
	 * @param skinName
	 */
	public void setSkinName(String skinName) {
		mSkinName = skinName;
	}

	/**
	 * Sets the maximum amount of time the interstitial should take to load. If you set it to 0 or a
	 * negative number, it will fall back to the default value of 5 seconds.
	 * 
	 * @param loadingTimeoutSecs
	 */
	public void setLoadingTimeoutSecs(int loadingTimeoutSecs) {
		mLoadingTimeoutSecs = loadingTimeoutSecs > 0 ? loadingTimeoutSecs
				: LOADING_TIMEOUT_SECONDS_DEFAULT;
	}

	/**
	 * <p>
	 * Triggers the loading of the interstitial with the configured parameters.
	 * </p>
	 * <p>
	 * The process will be performed in a background thread. The invocation of the
	 * {@link InterstitialLoadingStatusListener} registered in
	 * {@link #InterstitialLoader(Activity, String, HostInfo, InterstitialLoadingStatusListener)}
	 * will be done in the calling thread.
	 * </p>
	 */
	public void startLoading() {
		cancelInterstitialLoading();

		String[] interstitialUrlExtraKeys = new String[] { URL_PARAM_INTERSTITIAL_KEY,
				UrlBuilder.URL_PARAM_ALLOW_CAMPAIGN_KEY, URL_PARAM_SKIN_KEY,
				UrlBuilder.URL_PARAM_OFFSET_KEY, URL_PARAM_BACKGROUND_KEY };
		String[] interstitialUrlExtraValues = new String[] { UrlBuilder.URL_PARAM_VALUE_ON,
				UrlBuilder.URL_PARAM_VALUE_ON, mSkinName,
				String.valueOf(sInterstitialAvailableResponseCount), mBackgroundUrl };

		String interstitialBaseUrl = SponsorPayPublisher.shouldUseStagingUrls() ? INTERSTITIAL_STAGING_BASE_URL
				: INTERSTITIAL_PRODUCTION_BASE_URL;

		String interstitialUrl = UrlBuilder.buildUrl(interstitialBaseUrl, mUserId, mHostInfo,
				interstitialUrlExtraKeys, interstitialUrlExtraValues);

		Log.i("interstitial", "url: " + interstitialUrl);
		
		mAsyncRequest = new AsyncRequest(interstitialUrl, this);
		mAsyncRequest.execute();

		if (mCancelLoadingOnTimeOut != null) {
			mHandler.removeCallbacks(mCancelLoadingOnTimeOut);
		}

		mCancelLoadingOnTimeOut = new Runnable() {
			@Override
			public void run() {
				cancelInterstitialLoading();
				if (InterstitialLoader.this.mLoadingStatusListener != null) {
					InterstitialLoader.this.mLoadingStatusListener.onInterstitialLoadingTimeOut();
				}
			}
		};

		mHandler.postDelayed(mCancelLoadingOnTimeOut, mLoadingTimeoutSecs * MILLISECONDS_IN_SECOND);

		mProgressDialog = new ProgressDialog(mCallingActivity);
		mProgressDialog.setOwnerActivity(mCallingActivity);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setMessage(SponsorPayPublisher
				.getUIString(UIStringIdentifier.LOADING_INTERSTITIAL));
		mProgressDialog.show();
	}

	/**
	 * Cancels the request and loading of the interstitial if the {@link InterstitialActivity} has
	 * still not been launched. Will dismiss the loading progress dialog if it is currently being
	 * shown.
	 */
	private void cancelInterstitialLoading() {
		if (mAsyncRequest != null && !mAsyncRequest.isCancelled()) {
			mAsyncRequest.cancel(false);
		}
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mAsyncRequest = null;
	}

	/**
	 * Launches the {@link InterstitialActivity} with the initial contents of the interstitial ad,
	 * the initial base URL for images, scripts and other dependencies, and the desired behavior for
	 * staying open after user redirection.
	 * 
	 * @param result
	 */
	private void launchInterstitialActivity(AsyncRequest request) {
		Intent interstitialIntent = new Intent(mCallingActivity, InterstitialActivity.class);
		interstitialIntent.putExtra(InterstitialActivity.EXTRA_INITIAL_CONTENT_KEY, request
				.getResponseBody());
		interstitialIntent.putExtra(InterstitialActivity.EXTRA_COOKIESTRINGS_KEY, request
				.getCookieStrings());
		interstitialIntent.putExtra(InterstitialActivity.EXTRA_SHOULD_STAY_OPEN_KEY,
				mShouldStayOpen);

		String interstitialDomain = SponsorPayPublisher.shouldUseStagingUrls() ? INTERSTITIAL_STAGING_DOMAIN
				: INTERSTITIAL_PRODUCTION_DOMAIN;
		interstitialIntent.putExtra(InterstitialActivity.EXTRA_BASE_DOMAIN_KEY, interstitialDomain);

		mCallingActivity.startActivity(interstitialIntent);
	}

	/**
	 * Called when the results of the interstitial request are available, i.e. a response has been
	 * received from the server. Notifies the {@link InterstitialLoadingStatusListener} registered
	 * by the host publisher application of the events "Will Show Interstitial",
	 * "Interstitial Request Error" and "No Interstitial Available"
	 * 
	 * @param result
	 *            a {@link InterstitialLoadingResults} containing the status code and the contents
	 *            of the response.
	 */
	@Override
	public void onAsyncRequestComplete(AsyncRequest request) {
		Log.v(LOG_TAG, "Interstitial request completed with status code: "
				+ request.getHttpStatusCode() + ", did trigger exception: "
				+ request.didRequestTriggerException());

		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}

		if (request.hasSucessfulStatusCode()) {
			sInterstitialAvailableResponseCount++;
			if (mLoadingStatusListener != null) {
				mLoadingStatusListener.onWillShowInterstitial();
			}
			launchInterstitialActivity(request);
			// showInterstitialOverlay(request);
		} else if (request.didRequestTriggerException()) {
			if (mLoadingStatusListener != null) {
				mLoadingStatusListener.onInterstitialRequestError();
			}
		} else {
			if (mLoadingStatusListener != null) {
				mLoadingStatusListener.onNoInterstitialAvailable();
			}
		}
	}
}
