/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

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
			responseString = EntityUtils.toString(responseEntity);
			responseEntity.consumeContent();
			
		} catch (IOException e) {
			responseString = null;
		}

		return responseString;
	}
}
