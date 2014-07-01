package com.sponsorpay.utils;

public class CurrencyAndMediationServerResponse {
	
	private  int mStatusCode;
	private  String mResponseBody;
	private  String mResponseSignature;
	
	public CurrencyAndMediationServerResponse(
			int mStatusCode, String mResponseBody,
			String mResponseSignature) {
		
		this.mStatusCode = mStatusCode;
		this.mResponseBody = mResponseBody;
		this.mResponseSignature = mResponseSignature;
	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public void setStatusCode(int mStatusCode) {
		this.mStatusCode = mStatusCode;
	}

	public String getResponseBody() {
		return mResponseBody;
	}

	public void setResponseBody(String mResponseBody) {
		this.mResponseBody = mResponseBody;
	}

	public String getResponseSignature() {
		return mResponseSignature;
	}

	public void setResponseSignature(String mResponseSignature) {
		this.mResponseSignature = mResponseSignature;
	}
	
}