/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.publisher.currency;

import com.sponsorpay.publisher.currency.SPCurrencyServerRequester.SPCurrencyServerReponse;

public class SPCurrencyServerSuccesfulResponse implements SPCurrencyServerReponse {

	private double mDeltaOfCoins;
	private String mLatestTransactionId;
	private String mCurrencyId;
	private String mCurrencyName;
	private boolean mIsDefault;

	public SPCurrencyServerSuccesfulResponse(double deltaOfCoins, String latestTransactionId, String currencyId, 
			String currencyName, boolean isDefault) {
		mDeltaOfCoins = deltaOfCoins;
		mLatestTransactionId = latestTransactionId;
		mCurrencyId = currencyId;
		mCurrencyName = currencyName;
		mIsDefault = isDefault;
	}

	public double getDeltaOfCoins() {
		return mDeltaOfCoins;
	}

	public String getLatestTransactionId() {
		return mLatestTransactionId;
	}

	public String getCurrencyId() {
		return mCurrencyId;
	}

	public String getCurrencyName() {
		return mCurrencyName;
	}

	public boolean isDefault() {
		return mIsDefault;
	}

}