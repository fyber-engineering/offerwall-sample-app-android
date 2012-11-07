/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.unlock;

import com.sponsorpay.sdk.android.publisher.AbstractResponse;

/**
 * <p>
 * Interface to be implemented by parties interested in the results of requests to the SponsorPay Unlock
 * Items server's response.
 * </p>
 */
public interface SPUnlockResponseListener {
	
	/**
	 * Called when a request to SponsorPay's Server resulted in an error.
	 * 
	 * @param response
	 *            Instance implementing the {@link AbstractResponse#getErrorType()},
	 *            {@link AbstractResponse#getErrorType()} and {@link AbstractResponse#getErrorMessage()} methods.
	 */
	void onSPUnlockRequestError(AbstractResponse response);

	/**
	 * Called when a response containing the unlock items' status has been answered by the SponsorPay's
	 * Server. Having this method invoked on your callback means that the request has been successful
	 * and the response contains valid data.
	 * 
	 * @param response
	 *            A response instance that implements the {@link UnlockedItemsResponse#getItems()}
	 *            method, returning a {@link Map} where the key is the ItemID and the value is 
	 *            {@link UnlockedItemsResponse.Item}.
	 */
	void onSPUnlockItemsStatusResponseReceived(UnlockedItemsResponse response);

}
