/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.currency;

import org.json.JSONObject;

import com.sponsorpay.sdk.android.SignatureTools;
import com.sponsorpay.sdk.android.publisher.currency.VirtualCurrencyConnector.RequestType;

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
	private static final String ERROR_CODE_KEY = "code";
	private static final String ERROR_MESSAGE_KEY = "message";

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
		 * The server returned an error. Use {@link CurrencyServerAbstractResponse#getErrorCode()} and
		 * {@link CurrencyServerAbstractResponse#getErrorMessage()} to extract more details about this error.
		 */
		SERVER_RETURNED_ERROR,

		/**
		 * An error whose cause couldn't be determined.
		 */
		ERROR_OTHER
	}

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
	 * Listener which will be notified after parsing the response. Every response type may call a different listener
	 * method.
	 */
	protected SPCurrencyServerListener mListener;

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
	 * Implement to parse a successful-HTTP-status-code containing response.
	 */
	public abstract void parseSuccessfulResponse();

	/**
	 * Implement to invoke the response-type-specific on success callback notification.
	 */
	public abstract void invokeOnSuccessCallback();

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
	public void setResponseData(int responseStatusCode, String responseBody, String responseSignature) {
		mResponseStatusCode = responseStatusCode;
		mResponseBody = responseBody;
		mResponseSignature = responseSignature;
	}

	/**
	 * Set the response listener which will be notified when the parsing is complete. Every response type may call a
	 * different listener method.
	 * 
	 * @param listener
	 */
	public void setResponseListener(SPCurrencyServerListener listener) {
		mListener = listener;
	}

	/**
	 * Verify calculate the signature of the response with the provided security token and compare it against the
	 * server-provided response signature.
	 * 
	 * @param securityToken
	 *            Security token which will be used to calculate the signature.
	 * @return true if the calculated signature matches the server-provided signature. false otherwise.
	 */
	public boolean verifySignature(String securityToken) {
		String generatedSignature = SignatureTools.generateSignatureForString(mResponseBody, securityToken);
		if (!(generatedSignature.equals(mResponseSignature))) {
			mErrorType = RequestErrorType.ERROR_INVALID_RESPONSE_SIGNATURE;
			return false;
		}
		return true;
	}

	/**
	 * Performs a second-stage error checking, parses the response and invokes the relevant method of the listener
	 * registered with {@link #setResponseListener(SPCurrencyServerListener)}.
	 * 
	 * @param securityToken
	 *            Security token used to verify the authenticity of the response.
	 */
	public void parseAndCallListener(String securityToken) {
		if (mErrorType == RequestErrorType.ERROR_NO_INTERNET_CONNECTION) {
			invokeOnErrorCallback();
		} else if (hasErrorStatusCode()) {
			parseErrorResponse();
			invokeOnErrorCallback();
		} else if (!verifySignature(securityToken)) {
			invokeOnErrorCallback();
		} else {
			parseSuccessfulResponse();
			if (mErrorType == RequestErrorType.NO_ERROR)
				invokeOnSuccessCallback();
			else
				invokeOnErrorCallback();
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
	 * Parses a response containing a non-successful HTTP status code. Tries to extract the error code and error message
	 * from the response body.
	 */
	public void parseErrorResponse() {
		try {
			JSONObject responseBodyAsJsonObject = new JSONObject(mResponseBody);
			mErrorCode = responseBodyAsJsonObject.getString(ERROR_CODE_KEY);
			mErrorMessage = responseBodyAsJsonObject.getString(ERROR_MESSAGE_KEY);
			mErrorType = RequestErrorType.SERVER_RETURNED_ERROR;
		} catch (Exception e) {
			mErrorType = RequestErrorType.ERROR_OTHER;
		}
	}

	/**
	 * Invokes the {@link SPCurrencyServerListener#onSPCurrencyServerError(CurrencyServerAbstractResponse)} of the
	 * registered callback.
	 */
	public void invokeOnErrorCallback() {
		if (mListener != null) {
			mListener.onSPCurrencyServerError(this);
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
		return mErrorCode != null ? mErrorCode : "";
	}

	/**
	 * Gets the error message returned by the server.
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return mErrorMessage != null ? mErrorMessage : "";
	}

	/**
	 * Returns the appropriate subclass to enclose and parse the response to a request of the passed type.
	 * 
	 * @param requestType
	 *            The type of the request whose response can be parsed by the returned instance.
	 * @return An instance of a subclass of {@link CurrencyServerAbstractResponse}, or null if no subclass is known for
	 *         parsing the provided {@link RequestType}.
	 */
	public static CurrencyServerAbstractResponse getParsingInstance(RequestType requestType) {
		CurrencyServerAbstractResponse instanceToReturn = null;

		switch (requestType) {
		case DELTA_COINS:
			instanceToReturn = new CurrencyServerDeltaOfCoinsResponse();
			break;
		default:
			break;
		}

		return instanceToReturn;
	}
}
