/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.currency;


import com.sponsorpay.sdk.android.publisher.AbstractResponse;

/**
 * <p>
 * Encloses a basic response received from the SponsorPay's Virtual Currency Server and methods to perform parsing of
 * the returned JSON-encoded data.
 * </p>
 * 
 */
public abstract class CurrencyServerAbstractResponse extends AbstractResponse {

	/**
	 * Listener which will be notified after parsing the response. Every response type may call a different listener
	 * method.
	 */
	protected SPCurrencyServerListener mListener;

	/**
	 * Set the response listener which will be notified when the parsing is complete. Every response
	 * type may call a different listener method.
	 * 
	 * @param listener
	 */
	public void setResponseListener(SPCurrencyServerListener listener) {
		mListener = listener;
	}

	/**
	 * Invokes the {@link SPCurrencyServerListener#onSPCurrencyServerError(CurrencyServerAbstractResponse)} of the
	 * registered callback.
	 */
	public void onErrorTriggered() {
		if (mListener != null) {
			mListener.onSPCurrencyServerError(this);
		}
	}
}
