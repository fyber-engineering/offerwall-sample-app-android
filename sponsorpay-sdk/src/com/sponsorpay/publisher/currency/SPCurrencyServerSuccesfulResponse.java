/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.publisher.currency;
/**
 * This class is depricated and will be removed on the next major release
 * of the SDK v.7.0.0
 *
 * @deprecated use {@link com.sponsorpay.publisher.currency.SPCurrencyServerSuccessfulResponse} instead.  
 */
@Deprecated
public class SPCurrencyServerSuccesfulResponse extends SPCurrencyServerSuccessfulResponse{

	public SPCurrencyServerSuccesfulResponse(double deltaOfCoins, String latestTransactionId, String currencyId,
			String currencyName, boolean isDefault) {
		super(deltaOfCoins, latestTransactionId, currencyId, currencyName, isDefault);
	}
	
}