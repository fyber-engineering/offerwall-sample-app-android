/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.currency;


import org.json.JSONObject;

import com.sponsorpay.sdk.android.utils.SignatureTools;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * <p>
 * Encloses a basic response received from the SponsorPay's Virtual Currency Server and methods to perform parsing of
 * the returned JSON-encoded data.
 * </p>
 * 
 */
public abstract class CurrencyServerAbstractResponse {

	/*
	 * JSON keys used to enclose error information.
	 */
	protected static final String ERROR_CODE_KEY = "code";
	protected static final String ERROR_MESSAGE_KEY = "message";

	/**
	 * Status code of the HTTP response.
	 */
	protected int mResponseStatusCode;

	/**
	 * Body of the HTTP response.
	 */
	protected String mResponseBody;

	/**
	 * Server-provided signature of the response.
	 */
	protected String mResponseSignature;
	
	/**
	 * Type of error condition in which the request / response has resulted.
	 */
	protected RequestErrorType mErrorType;
	
	/**
	 * Error code provided by the server.
	 */
	protected String mErrorCode;
	
	/**
	 * Error message provided by the server.
	 */
	protected String mErrorMessage;
	
	/**
	 * Listener which will be notified after parsing the response. Every response type may call a different listener
	 * method.
	 */
	protected SPCurrencyServerListener mListener;

	/**
	 * Implement to parse a successful-HTTP-status-code containing response.
	 */
	public abstract void parseSuccessfulResponse();

	/**
	 * Implement to invoke the response-type-specific on success callback notification.
	 */
	public abstract void onSuccessfulResponseParsed();

	/**
	 * Sets the raw response data.
	 * 
	 * @param responseStatusCode
	 *            HTTP status code returned by the server.
	 * @param responseBody
	 *            Body of the HTTP response.
	 * @param responseSignature
	 *            Signature of the response extracted from the response headers.
	 */
	public void setResponseData(int responseStatusCode, String responseBody,
			String responseSignature) {
		mResponseStatusCode = responseStatusCode;
		mResponseBody = responseBody;
		mResponseSignature = responseSignature;
	}

	/**
	 * Verify calculate the signature of the response with the provided security token and compare
	 * it against the server-provided response signature.
	 * 
	 * @param securityToken
	 *            Security token which will be used to calculate the signature.
	 * @return true if the calculated signature matches the server-provided signature. false
	 *         otherwise.
	 */
	public boolean verifySignature(String securityToken) {
		String generatedSignature = SignatureTools.generateSignatureForString(mResponseBody,
				securityToken);
		if (!(generatedSignature.equals(mResponseSignature))) {
			mErrorType = RequestErrorType.ERROR_INVALID_RESPONSE_SIGNATURE;
			return false;
		}
		return true;
	}
	/**
	 * Performs a second-stage error checking, parses the response and invokes the relevant method
	 * of the registered listener.
	 * 
	 * @param securityToken
	 *            Security token used to verify the authenticity of the response.
	 */
	public void parseAndCallListener(String securityToken) {
		if (mErrorType == RequestErrorType.ERROR_NO_INTERNET_CONNECTION) {
			onErrorTriggered();
		} else if (hasErrorStatusCode()) {
			parseErrorResponse();
			onErrorTriggered();
		} else if (!verifySignature(securityToken)) {
			onErrorTriggered();
		} else {
			parseSuccessfulResponse();
			if (mErrorType == RequestErrorType.NO_ERROR) {
				onSuccessfulResponseParsed();
			} else {
				onErrorTriggered();
			}
		}
	}

	/**
	 * Returns true if the response contains an HTTP status code out of the 200s.
	 * 
	 * @return false if HTTP status code is between 200 and 299. True otherwise.
	 */
	public boolean hasErrorStatusCode() {
		return mResponseStatusCode < 200 || mResponseStatusCode > 299;
	}
	
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
	
	/**
	 * Parses a response containing a non-successful HTTP status code. Tries to extract the error
	 * code and error message from the response body.
	 */
	public void parseErrorResponse() {
		try {
			JSONObject responseBodyAsJsonObject = new JSONObject(mResponseBody);
			mErrorCode = responseBodyAsJsonObject.getString(ERROR_CODE_KEY);
			mErrorMessage = responseBodyAsJsonObject.getString(ERROR_MESSAGE_KEY);
			mErrorType = RequestErrorType.SERVER_RETURNED_ERROR;
		} catch (Exception e) {
			SponsorPayLogger.w(getClass().getSimpleName(),
					"An exception was triggered while parsing error response", e);
			mErrorType = RequestErrorType.ERROR_OTHER;
		}
	}
	
	/**
	 * Gets the error condition in which this request / response has resulted.
	 * 
	 * @return A {@link RequestErrorType}.
	 */
	public RequestErrorType getErrorType() {
		return mErrorType;
	}

	/**
	 * Gets the error code returned by the server.
	 * 
	 * @return
	 */
	public String getErrorCode() {
		return mErrorCode != null ? mErrorCode : StringUtils.EMPTY_STRING;
	}

	/**
	 * Gets the error message returned by the server.
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return mErrorMessage != null ? mErrorMessage : StringUtils.EMPTY_STRING;
	}
	
	/**
	 * Types of error condition which a request / response might result in.
	 */
	public enum RequestErrorType {
		/**
		 * A correct response was received.
		 */
		NO_ERROR,

		/**
		 * Request couldn't be sent, usually due to a down network connection.
		 */
		ERROR_NO_INTERNET_CONNECTION,

		/**
		 * Returned response is not formatted in an expected way.
		 */
		ERROR_INVALID_RESPONSE,

		/**
		 * Response doesn't contain a valid signature.
		 */
		ERROR_INVALID_RESPONSE_SIGNATURE,

		/**
		 * The server returned an error. Use {@link #getErrorCode()}
		 * and {@link #getErrorMessage()} to extract more details
		 * about this error.
		 */
		SERVER_RETURNED_ERROR,

		/**
		 * An error whose cause couldn't be determined.
		 */
		ERROR_OTHER
	}
	
}
