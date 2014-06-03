/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.AsyncTask;

public class GetRemoteFileContentTask extends AsyncTask<String, Void, String> {

	private static final String TAG = "GetRemoteFileContentTask";

	@Override
	protected String doInBackground(String... params) {
		Thread.currentThread().setName(TAG);
		try {
			HttpClient httpClient = SPHttpClient.getHttpClient();
			HttpUriRequest request = new HttpGet(params[0]);
			HttpResponse response = httpClient.execute(request);
			
			return HttpResponseParser.extractResponseString(response);
		} catch (ClientProtocolException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		}
		return null;
	}
    
}
