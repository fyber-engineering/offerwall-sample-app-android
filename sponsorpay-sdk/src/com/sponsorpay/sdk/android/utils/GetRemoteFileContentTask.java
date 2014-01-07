package com.sponsorpay.sdk.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.AsyncTask;

public class GetRemoteFileContentTask extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {
		try {
			HttpClient httpClient = SPHttpClient.getHttpClient();
			HttpUriRequest request = new HttpGet(params[0]);
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			InputStream inStream = responseEntity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inStream));
			StringBuilder sb = new StringBuilder();

			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			inStream.close();

			return sb.toString();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    
}
