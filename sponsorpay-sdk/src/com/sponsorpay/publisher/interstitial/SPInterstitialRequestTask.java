/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.sponsorpay.utils.HttpResponseParser;
import com.sponsorpay.utils.SPHttpClient;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;

public class SPInterstitialRequestTask extends AsyncTask<UrlBuilder, Void, SPInterstitialAd[]> {

	private static final String TAG = "SPInterstitialRequestTask";
	
	@Override
	protected SPInterstitialAd[] doInBackground(UrlBuilder... params) {
		Thread.currentThread().setName(TAG);
		LinkedList<SPInterstitialAd> interstitialAds = new LinkedList<SPInterstitialAd>();
		try {
			HttpClient httpClient = SPHttpClient.getHttpClient();
			String requestUrl = params[0].buildUrl();
			SponsorPayLogger.d(TAG, "Loading URL: " + requestUrl);
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
					e.printStackTrace();
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return interstitialAds.toArray(new SPInterstitialAd[interstitialAds.size()]);
	}
	
	@Override
	protected void onPostExecute(SPInterstitialAd[] result) {
		super.onPostExecute(result);
		SPInterstitialClient.INSTANCE.processAds(result);
	}
    
}
