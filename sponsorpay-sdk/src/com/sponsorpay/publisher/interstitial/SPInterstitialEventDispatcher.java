/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.SPHttpClient;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;


public class SPInterstitialEventDispatcher extends AsyncTask<String, Void, Boolean> {
	
	private static final String TAG = "SPInterstitialEventDispatcher";

	private static final int SUCCESSFUL_HTTP_STATUS_CODE = 200;

	public static void trigger(SPCredentials credentials, String requestId,
			SPInterstitialAd ad, SPInterstitialEvent event) {
		if (credentials == null || StringUtils.nullOrEmpty(requestId) || event == null) {
			SponsorPayLogger.d(TAG, "The event cannot be sent, a required field is missing.");
		} else {
			if (ad != null) {
				SponsorPayLogger.d(TAG,	String.format(
						"Notifiying tracker of event=%s with request_id=%s for ad_id=%s and provider_type=%s ",
						event, requestId, ad.getAdId(),
						ad.getProviderType()));
			} else {
				SponsorPayLogger.d(TAG,	String.format(
						"Notifiying tracker of event=%s with request_id=%s",
						event, requestId));
			}
			new SPInterstitialEventDispatcher().execute(buildUrl(credentials, requestId, ad,
					event));
		}
	}
	
	private static String buildUrl(SPCredentials credentials, String requestId,
			SPInterstitialAd ad, SPInterstitialEvent event) {
		UrlBuilder builder = UrlBuilder.newBuilder(getBaseUrl(), credentials)
				.addKeyValue("request_id", requestId)
				.addKeyValue("event", event.toString());
		if (ad != null) {
			builder.addKeyValue("ad_id", ad.getAdId())
			.addKeyValue("provider_type", ad.getProviderType());
		}
		return builder.buildUrl();
	}

	private static String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl("tracker");
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		Thread.currentThread().setName(TAG);
		Boolean returnValue = false;

		String url = params[0];
		
		SponsorPayLogger.d(TAG, "Sending event to "+ url);

		HttpGet httpRequest = new HttpGet(url);
		HttpClient httpClient = SPHttpClient.getHttpClient();

		try {
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			// We're not parsing the response, just making sure that a successful status code has
			// been received.
			int responseStatusCode = httpResponse.getStatusLine().getStatusCode();
			
			httpResponse.getEntity().consumeContent();
			
			returnValue = responseStatusCode == SUCCESSFUL_HTTP_STATUS_CODE;
		} catch (Exception e) {
			SponsorPayLogger.e(TAG,
					"An exception occurred when trying to send advertiser callback: " + e);
		}
		return returnValue;
	}
	
}
