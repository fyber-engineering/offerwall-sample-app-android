/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.webkit.WebView;

import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

/**
 * {@link OfferWebClient} defining common functionality for {@link WebView} instances displaying
 * SponsorPay offers inside a dedicated activity.
 * 
 */
public class ActivityOfferWebClient extends OfferWebClient {

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

		SponsorPayLogger.i(OfferWebClient.LOG_TAG, "Should stay open: " + mShouldHostActivityStayOpen
				+ ", will close activity: " + willCloseHostActivity);

		if (willCloseHostActivity) {
			hostActivity.finish();
		}
	}
}
