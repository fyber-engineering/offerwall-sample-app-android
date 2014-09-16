/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SPHttpConnection {
	
	private static final String TAG = "SPHttpConnection";

	public static SPHttpConnection getConnection(String url) throws MalformedURLException {
		return new SPHttpConnection(url);
	}

	public static SPHttpConnection getConnection(UrlBuilder builder) throws MalformedURLException {
		return new SPHttpConnection(builder.buildUrl());
	}

	private String mBody;
	private URL mUrl;
	private Map<String, List<String>> mHeaders;
	private int mResponseCode;
	private List<Header> mHeadersToAdd;
	private boolean mOpen = false;
	
	private SPHttpConnection(String url) throws MalformedURLException {
		mUrl = new URL(url);
	}
	
	public SPHttpConnection addHeader(String header, String headerValue) {
		if (mHeadersToAdd == null) {
			 mHeadersToAdd = new LinkedList<Header>();
		}
		mHeadersToAdd.add(new Header(header, headerValue));
		return this;
	}
	
	public SPHttpConnection open() {
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) mUrl.openConnection();
			if (mHeadersToAdd != null) {
				for (Header header : mHeadersToAdd) {
					urlConnection.addRequestProperty(header.key, header.value);
				}
			}
			
			InputStream is = null;
			try {
				is = urlConnection.getInputStream();
			} catch (IOException exception) {
				is = urlConnection.getErrorStream();
			}
			mBody = readStream(is);
			mResponseCode = urlConnection.getResponseCode();
			mHeaders = Collections.unmodifiableMap(urlConnection
					.getHeaderFields());
		} catch (Exception e) {
			SponsorPayLogger.e(TAG, e.getLocalizedMessage(), e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			mOpen = true; 
		}
		return this;
	}
	
	private String readStream(InputStream is) throws IOException {
		String content = null;
		if (is != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line + '\n');
					line = br.readLine();
				}
				content = sb.toString();
			} finally {
				br.close();
			}
		}
		return content;
	}

	public String getBodyContent() throws IOException {
		if (!mOpen) {
			throw new IOException("The connection has not been opened yet.");
		}
		return mBody;
	}
	
	public Map<String, List<String>> getHeaders() throws IOException {
		if (!mOpen) {
			throw new IOException("The connection has not been opened yet.");
		}
		return mHeaders;
	}
	
	public List<String> getHeader(String header) throws IOException {
		if (!mOpen) {
			throw new IOException("The connection has not been opened yet.");
		}
		return mHeaders.get(header);
	}
	
	public int getResponseCode() throws IOException {
		if (!mOpen) {
			throw new IOException("The connection has not been opened yet.");
		}
		return mResponseCode;
	}
	
	// helper class
	private class Header {
		
		String key;
		String value;
		
		public Header(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
	}

}
