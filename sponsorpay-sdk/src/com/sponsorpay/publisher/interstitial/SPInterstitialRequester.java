/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.HostInfo;
import com.sponsorpay.utils.SPHttpConnection;
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
				.addScreenMetrics()
				.addScreenOrientation();

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
			String requestUrl = params[0].buildUrl();
			SponsorPayLogger.d(TAG, "Querying URL: " + requestUrl);

			String bodyContent = SPHttpConnection.getConnection(requestUrl).open().getBodyContent();
			
			//parsing offers
			if (StringUtils.notNullNorEmpty(bodyContent)) {
				HostInfo hostInfo = HostInfo.getHostInfo(null);
				String screenOrientation = hostInfo.getScreenOrientation();
				SponsorPayLogger.d(TAG, "Parsing ads reponse\n" + bodyContent);
				try {
					JSONObject json = new JSONObject(bodyContent);
					JSONArray ads = json.getJSONArray("ads");
					for (int i = 0 ; i < ads.length() ; i++) {
						JSONObject jsonAd = ads.getJSONObject(i);
						String providerType = jsonAd.getString("provider_type");
						String adId = jsonAd.getString("ad_id");
						
						SPInterstitialAd ad = new SPInterstitialAd(providerType, adId);
						
						JSONArray names = jsonAd.names();
						for (int j = 0 ; j < names.length() ; j++) {
							String key = names.getString(j);
							if (!(key.equals("ad_id") || (key.equals("provider_type")))) {
								ad.setContextData(key, jsonAd.getString(key));
							}
						}
						
						if (!ad.getContextData().containsKey("orientation")){
							ad.getContextData().put("orientation", screenOrientation);
						}
						
						int rotation = hostInfo.getRotation();
						ad.getContextData().put("rotation", Integer.toString(rotation));
						
						interstitialAds.add(ad);
						
					}
				} catch (JSONException e) {
					SponsorPayLogger.e(TAG, e.getMessage(), e);;
				}
			}
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		}
		
		return interstitialAds.toArray(new SPInterstitialAd[interstitialAds.size()]);
	}
	
	@Override
	protected void onPostExecute(SPInterstitialAd[] result) {
		SPInterstitialClient.INSTANCE.processAds(result);
	}

}
