/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.currency;

import org.json.JSONObject;

/**
 * <p>
 * Encloses a response received from the SponsorPay's Virtual Currency Server for the Get Delta of Coins request, as
 * well as methods to perform parsing of the returned JSON-encoded data.
 * </p>
 * 
 */
public class CurrencyServerDeltaOfCoinsResponse extends CurrencyServerAbstractResponse {
	/*
	 * JSON keys used to enclose data from a successful response.
	 */
	private static final String DELTA_OF_COINS_KEY = "delta_of_coins";
	private static final String LATEST_TRANSACTION_ID_KEY = "latest_transaction_id";

	/**
	 * Delta of coins returned by the server.
	 */
	private double mDeltaOfCoins;
	private String mLatestTransactionId;
	private VirtualCurrencyConnector mVirtualCurrencyConnector;

	public CurrencyServerDeltaOfCoinsResponse(
			VirtualCurrencyConnector virtualCurrencyConnector) {
		mVirtualCurrencyConnector = virtualCurrencyConnector;
	}

	/**
	 * Returns the delta of coins provided by the server.
	 * 
	 * @return The value returned by the server for delta of coins.
	 */
	public double getDeltaOfCoins() {
		return mDeltaOfCoins;
	}

	/**
	 * Returns the latest transaction ID returned by the server.
	 * 
	 * @return
	 */
	public String getLatestTransactionId() {
		return mLatestTransactionId;
	}

	/**
	 * Parses a successful delta-of-coins response.
	 */
	@Override
	public void parseSuccessfulResponse() {
		try {
			JSONObject responseBodyAsJsonObject = new JSONObject(mResponseBody);
			mDeltaOfCoins = responseBodyAsJsonObject.getDouble(DELTA_OF_COINS_KEY);
			mLatestTransactionId = responseBodyAsJsonObject.getString(LATEST_TRANSACTION_ID_KEY);
			mErrorType = RequestErrorType.NO_ERROR;
		} catch (Exception e) {
			mErrorType = RequestErrorType.ERROR_INVALID_RESPONSE;
		}
	}

	/**
	 * Invokes the {@link SPCurrencyServerListener#onSPCurrencyDeltaReceived(CurrencyServerDeltaOfCoinsResponse)}
	 * callback method of the registered listener.
	 */
	@Override
	public void onSuccessfulResponseParsed() {
		mVirtualCurrencyConnector.onDeltaOfCoinsResponse(this);
		if (mListener != null) {
			mListener.onSPCurrencyDeltaReceived(this);
		}
	}
}