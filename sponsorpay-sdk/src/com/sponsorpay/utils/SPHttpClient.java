/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.io.IOException;
import java.security.KeyStore;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class SPHttpClient {

	private static final String TAG = "SPHttpClient";
	private static SPHttpClient INSTANCE = new SPHttpClient();
	
	public static HttpClient getHttpClient() {
		return INSTANCE.getClient();
	}
	
	private HttpClient client;
	
	private HttpClient getClient() {
		if (client == null) {
			HttpUriRequest request = new HttpGet("https://service.sponsorpay.com");
			
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			
			try {
				final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
				registry.register(new Scheme("https", sslSocketFactory, 443));
				
				ClientConnectionManager cm = new ThreadSafeClientConnManager(params, registry);
				client = new DefaultHttpClient(cm, params);
				
				HttpResponse response = client.execute(request);
				response.getStatusLine();
				response.getEntity().consumeContent();
			} catch (ClientProtocolException e) {
				SponsorPayLogger.e(TAG, "Client protocol error", e);
			} catch (IOException e1) {
				// SSLPeerUnverifiedException - most likely, create custom http client
				try {

					KeyStore trustStore = KeyStore.getInstance(KeyStore
							.getDefaultType());
					trustStore.load(null, null);

					SSLSocketFactory sf = new SPSSLSocketFactory(trustStore);
					sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

					registry.unregister("https");
					registry.register(new Scheme("https", sf, 443));

					ClientConnectionManager ccm = new ThreadSafeClientConnManager(
							params, registry);

					client = new DefaultHttpClient(ccm, params);
				} catch (Exception e) {
					SponsorPayLogger.e(TAG, "Unknow error, aborting...", e);
				}
			}
		}
		return client;
	}
	
}
