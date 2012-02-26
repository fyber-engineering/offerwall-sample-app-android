package com.sponsorpay.sdk.android.publisher.unlock;

import com.sponsorpay.sdk.android.publisher.AbstractResponse;

public interface SPUnlockResponseListener {
	void onSPUnlockRequestError(AbstractResponse response);
	void onSPUnlockedItemsResponseReceived(UnlockedItemsResponse response);
}
