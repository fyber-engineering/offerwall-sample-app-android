package com.sponsorpay.utils;

import java.io.BufferedInputStream;
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
	private List<Header> mHeadersToAdd = new LinkedList<Header>();
	
	private SPHttpConnection(String url) throws MalformedURLException {
		mUrl = new URL(url);
	}
	
	public SPHttpConnection addHeader(String header, String headerValue){
		mHeadersToAdd.add(new Header(header, headerValue));
		return this;
	}
	
	public SPHttpConnection open() {
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) mUrl.openConnection();
			for (Header header : mHeadersToAdd) {
				urlConnection.addRequestProperty(header.key, header.value);
			}
			
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			 mBody = readStream(in);
			 mHeaders = Collections.unmodifiableMap(urlConnection.getHeaderFields());
			 mResponseCode = urlConnection.getResponseCode();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
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

	// TODO check if this has been open first
	public String getBodyContent() {
		return mBody;
	}
	
	public Map<String, List<String>> getHeaders() {
		return mHeaders;
	}
	
	public List<String> getHeader(String header) {
		return mHeaders.get(header);
	}
	
	public int getResponseCode() {
		return mResponseCode;
	}
	
	private class Header {
		
		String key;
		String value;
		
		public Header(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
	}

}
