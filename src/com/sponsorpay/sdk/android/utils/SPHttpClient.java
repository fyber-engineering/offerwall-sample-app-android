/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import java.security.KeyStore;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
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

import android.os.Build;

public class SPHttpClient {

	private static SPHttpClient INSTANCE = new SPHttpClient();
	
	public static HttpClient getHttpClient() {
		return INSTANCE.getClient();
	}
	
	private HttpClient client;
	
	private HttpClient getClient() {
		if (client == null) {
			if (Build.VERSION.SDK_INT < 11) {
				try {

					KeyStore trustStore = KeyStore.getInstance(KeyStore
							.getDefaultType());
					trustStore.load(null, null);

					SSLSocketFactory sf = new SPSSLSocketFactory(trustStore);
					sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

					HttpParams params = new BasicHttpParams();
					HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
					HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

					SchemeRegistry registry = new SchemeRegistry();
					registry.register(new Scheme("http", PlainSocketFactory
							.getSocketFactory(), 80));
					registry.register(new Scheme("https", sf, 443));

					ClientConnectionManager ccm = new ThreadSafeClientConnManager(
							params, registry);

					client = new DefaultHttpClient(ccm, params);
				} catch (Exception e) {
					client = new DefaultHttpClient();
				}
			} else {
				client = new DefaultHttpClient();
			}
		}
		return client;
	}
	
	
}
