/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.HttpResponseParser;
import com.sponsorpay.utils.SPHttpClient;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;

/**
 * <p> 
 * Internal class that will perform a back-end query for interstitial ads. 
 * </p>
 * 
 * This class is not meant to be used directly. 
 * It is used by {@link SPInterstitialClient}.
 */
public class SPInterstitialRequester extends AsyncTask<UrlBuilder, Void, SPInterstitialAd[]> {

	private static final String TAG = "SPInterstitialRequester";
	private static final String INTERSTITIAL_URL_KEY = "interstitial";
	
	public static void requestAds(SPCredentials credentials, String requestId,
			Map<String, String> customParameters) {
		UrlBuilder urlBuilder = UrlBuilder.newBuilder(getBaseUrl(), credentials)
				.addExtraKeysValues(customParameters)
				.addKeyValue(SPInterstitialClient.SP_REQUEST_ID_PARAMETER_KEY, requestId)
				.addScreenMetrics();
		new SPInterstitialRequester().execute(urlBuilder);
	}
	
	private static String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(INTERSTITIAL_URL_KEY);
	}
	
	private SPInterstitialRequester() {
	}

	@Override
	protected SPInterstitialAd[] doInBackground(UrlBuilder... params) {
		Thread.currentThread().setName(TAG);
		LinkedList<SPInterstitialAd> interstitialAds = new LinkedList<SPInterstitialAd>();
		try {
			HttpClient httpClient = SPHttpClient.getHttpClient();
			String requestUrl = params[0].buildUrl();
			SponsorPayLogger.d(TAG, "Querying URL: " + requestUrl);
			HttpUriRequest request = new HttpGet(requestUrl);
			HttpResponse response = httpClient.execute(request);
	
			String bodyContent = HttpResponseParser.extractResponseString(response);
			
			//parsing offers
			if (StringUtils.notNullNorEmpty(bodyContent)) {
				SponsorPayLogger.d(TAG, "Parsing ads reponse\n" + bodyContent);
				try {
					JSONObject json = new JSONObject(bodyContent);
					JSONArray ads = json.getJSONArray("ads");
					for (int i = 0 ; i < ads.length() ; i++) {
						JSONObject ad = ads.getJSONObject(i);
						String providerType = ad.getString("provider_type");
						String adId = ad.getString("ad_id");

						interstitialAds.add(new SPInterstitialAd(
								providerType, adId));
		
					}
				} catch (JSONException e) {
					SponsorPayLogger.e(TAG, e.getMessage(), e);;
				}
			}
		} catch (ClientProtocolException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);;
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);;
		}
		
		return interstitialAds.toArray(new SPInterstitialAd[interstitialAds.size()]);
	}
	
	@Override
	protected void onPostExecute(SPInterstitialAd[] result) {
		SPInterstitialClient.INSTANCE.processAds(result);
	}

}
