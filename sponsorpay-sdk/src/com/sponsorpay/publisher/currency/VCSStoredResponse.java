/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.publisher.currency;

import java.io.Serializable;

public class VCSStoredResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private String mLatestTransactionId;
	private String mCurrencyId;
	
	public VCSStoredResponse(String mLatestTransactionId, String mCurrencyId) {
		this.mLatestTransactionId = mLatestTransactionId;
		this.mCurrencyId = mCurrencyId;
	}

	public String getLatestTransactionId() {
		return mLatestTransactionId;
	}

	public String getCurrencyId() {
		return mCurrencyId;
	}

	public void setLatestTransactionId(String mLatestTransactionId) {
		this.mLatestTransactionId = mLatestTransactionId;
	}

	public void setCurrencyId(String mCurrencyId) {
		this.mCurrencyId = mCurrencyId;
	}

}