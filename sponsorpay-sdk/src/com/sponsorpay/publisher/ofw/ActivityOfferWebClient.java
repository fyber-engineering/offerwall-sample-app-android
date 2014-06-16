/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.ofw;

import android.app.Activity;
import android.net.Uri;
import android.webkit.WebView;

import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.utils.SPWebClient;
import com.sponsorpay.utils.SponsorPayLogger;

/**
 * {@link SPWebClient} defining common functionality for {@link WebView} instances displaying
 * SponsorPay offers inside a dedicated activity.
 * 
 */
public class ActivityOfferWebClient extends SPWebClient {

	private boolean mShouldHostActivityStayOpen;

	public ActivityOfferWebClient(Activity hostActivity, boolean shouldStayOpen) {
		super(hostActivity);
		mShouldHostActivityStayOpen = shouldStayOpen;
	}

	@Override
	protected void onSponsorPayExitScheme(int resultCode, String targetUrl) {
		Activity hostActivity = getHostActivity();
		
		if (null == hostActivity) {
			return;
		}
		
		hostActivity.setResult(resultCode);
		boolean willCloseHostActivity = false;

		if (targetUrl == null) { // Exit scheme without target url: just close the host activity
			willCloseHostActivity = true;
		} else {
			willCloseHostActivity = !mShouldHostActivityStayOpen;
			if (!launchActivityWithUrl(targetUrl)) {
				return;
			}
		}

		SponsorPayLogger.i(SPWebClient.LOG_TAG, "Should stay open: " + mShouldHostActivityStayOpen
				+ ", will close activity: " + willCloseHostActivity);

		if (willCloseHostActivity) {
			hostActivity.finish();
		}
	}

	@Override
	protected void processSponsorPayScheme(String host, Uri uri) {
		// nothing more to do, everything is done by super class
	}

	@Override
	protected void onTargetActivityStart(String targetUrl) {
		// nothing to do 
	}
	
	@Override
	public void onReceivedError(WebView view, int errorCode, String description,
			String failingUrl) {
		SponsorPayLogger.e(getClass().getSimpleName(), String.format(
				"OfferWall WebView triggered an error. "
						+ "Error code: %d, error description: %s. Failing URL: %s",
				errorCode, description, failingUrl));

		UIStringIdentifier error;

		switch (errorCode) {
		case ERROR_HOST_LOOKUP:
		case ERROR_IO:
			error = UIStringIdentifier.ERROR_LOADING_OFFERWALL_NO_INTERNET_CONNECTION;
			break;
		default:
			error = UIStringIdentifier.ERROR_LOADING_OFFERWALL;
			break;
		}
		showDialog(SponsorPayPublisher.getUIString(error));
	}
	
}
