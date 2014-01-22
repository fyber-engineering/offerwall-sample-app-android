/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

/**
 * Contains an utility method to extract the body of an HTTP response.
 */
public class HttpResponseParser {

	/**
	 * Extracts the body as a string from an HTTP response entity. Returns null on failure.
	 * 
	 * @param httpResponse
	 *            The response whose body will be extracted as string.
	 * @return The body as string or null on failure.
	 */
	public static String extractResponseString(HttpResponse httpResponse) {
		String responseString;

		try {
			HttpEntity responseEntity = httpResponse.getEntity();
			InputStream inStream = responseEntity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
			StringBuilder sb = new StringBuilder();

			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			inStream.close();

			responseEntity.consumeContent();
			
			responseString = sb.toString();
		} catch (IOException e) {
			responseString = null;
		}

		return responseString;
	}
}
