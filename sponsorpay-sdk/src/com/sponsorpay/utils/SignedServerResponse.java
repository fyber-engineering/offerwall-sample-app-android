package com.sponsorpay.utils;

public class SignedServerResponse {
	
	private  int mStatusCode;
	private  String mResponseBody;
	private  String mResponseSignature;
	
	public SignedServerResponse(
			int mStatusCode, String mResponseBody,
			String mResponseSignature) {
		
		this.mStatusCode = mStatusCode;
		this.mResponseBody = mResponseBody;
		this.mResponseSignature = mResponseSignature;
	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public String getResponseBody() {
		return mResponseBody;
	}

	public String getResponseSignature() {
		return mResponseSignature;
	}
	
}