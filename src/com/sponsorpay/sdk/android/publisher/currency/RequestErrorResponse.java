/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.currency;

/**
 * Response generated locally when a request to the Virtual Currency Server couldn't be sent due to a network
 * connectivity issue.
 * 
 */
public class RequestErrorResponse extends CurrencyServerAbstractResponse {

	/**
	 * Initializes the response as containing an error of the {@link RequestErrorType#ERROR_NO_INTERNET_CONNECTION}
	 * type.
	 */
	public RequestErrorResponse() {
		super();
		mErrorType = RequestErrorType.ERROR_NO_INTERNET_CONNECTION;
	}

	/**
	 * Empty implementation.
	 */
	@Override
	public void parseSuccessfulResponse() {
	}

	/**
	 * Empty implementation.
	 */
	@Override
	public void invokeOnSuccessCallback() {
	}

}
