package com.sponsorpay.publisher.currency;

import com.sponsorpay.publisher.currency.SPCurrencyServerRequester.SPCurrencyServerReponse;

public class SPCurrencyServerSuccesfulResponse implements SPCurrencyServerReponse{

	private double mDeltaOfCoins;
	private String mLatestTransactionId;

	public SPCurrencyServerSuccesfulResponse(double deltaOfCoins,
			String latestTransactionId) {
		mDeltaOfCoins = deltaOfCoins;
		mLatestTransactionId = latestTransactionId;
	}

	public double getDeltaOfCoins() {
		return mDeltaOfCoins;
	}

	public String getLatestTransactionId() {
		return mLatestTransactionId;
	}

}
