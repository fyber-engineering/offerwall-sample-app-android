package com.sponsorpay.publisher.currency;

/**
 * Types of error condition which a request / response might result in.
 */
public enum SPCurrencyServerRequestErrorType {
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
	 * The server returned an error. Use {@link SPCurrencyServerErrorResponse#getErrorCode()}
	 * and {@link SPCurrencyServerErrorResponse#getErrorMessage()} to extract more details
	 * about this error.
	 */
	SERVER_RETURNED_ERROR,

	/**
	 * An error whose cause couldn't be determined.
	 */
	ERROR_OTHER
}
