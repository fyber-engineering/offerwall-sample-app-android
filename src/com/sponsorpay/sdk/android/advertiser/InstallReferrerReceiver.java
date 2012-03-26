package com.sponsorpay.sdk.android.advertiser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.util.Log;

/**
 * Listens to the broadcast sent by the Android Market app when the host application is installed
 * and extracts from it the value corresponding to the install subID which is used to track
 * the completion of an offer.
 */
public class InstallReferrerReceiver extends BroadcastReceiver {
	private static final String EXTRAS_KEY_REFERRER = "referrer";
	private static final String CONTENT_PARAM_KEY = "utm_content";

	@Override
	public void onReceive(Context context, Intent intent) {
		String referrer = "";
		Bundle extras = intent.getExtras();
		if (extras != null) {
			referrer = extras.getString(EXTRAS_KEY_REFERRER);
		}

		Log.i(getClass().getSimpleName(), "Received install referrer. Persisting data. "
				+ "Package name: " + context.getPackageName() + ". Install referrer: " + referrer);

		UrlQuerySanitizer referralParameters = new UrlQuerySanitizer();
		referralParameters.setAllowUnregisteredParamaters(true);

		referralParameters.parseQuery(referrer);
		String contentParameterSubId = referralParameters.getValue(CONTENT_PARAM_KEY);

		Log.i(getClass().getSimpleName(), "SubID extracted from received referrer: "
				+ contentParameterSubId);

		SponsorPayAdvertiserState persistentState = new SponsorPayAdvertiserState(context);
		persistentState.setInstallReferrer(referrer);
		persistentState.setInstallSubId(contentParameterSubId);
	}

}