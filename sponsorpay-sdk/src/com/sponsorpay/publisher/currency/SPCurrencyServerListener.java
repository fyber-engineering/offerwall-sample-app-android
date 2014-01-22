/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.currency;


/**
 * <p>
 * Interface to be implemented by parties interested in the results of requests to the SponsorPay Virtual Currency
 * Server.
 * </p>
 */
public interface SPCurrencyServerListener {
	/**
	 * Called when a request to SponsorPay's Virtual Currency Server resulted in an error.
	 * 
	 * @param response
	 *            Instance implementing the {@link CurrencyServerAbstractResponse#getErrorType()},
	 *            {@link CurrencyServerAbstractResponse#getErrorCode()},
	 *            {@link CurrencyServerAbstractResponse#getErrorMessage()} and methods.
	 */
	void onSPCurrencyServerError(CurrencyServerAbstractResponse response);

	/**
	 * Called when a response containing the currency delta for a given user has been answered by the SponsorPay's
	 * Virtual Currency Server. Having this method invoked on your callback means that the request has been successful
	 * and the response contains valid data.
	 * 
	 * @param response
	 *            A response instance that implements the {@link CurrencyServerDeltaOfCoinsResponse#getDeltaOfCoins()}
	 *            and {@link CurrencyServerDeltaOfCoinsResponse#getLatestTransactionId()} methods.
	 */
	void onSPCurrencyDeltaReceived(CurrencyServerDeltaOfCoinsResponse response);
}
