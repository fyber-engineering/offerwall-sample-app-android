package com.sponsorpay.sdk.android.publisher.unlock;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sponsorpay.sdk.android.publisher.AbstractResponse;

public class UnlockedItemsResponse extends AbstractResponse {
	/*
	 * JSON keys used to enclose data from a successful response.
	 */
	private static final String ITEM_ID_KEY = "itemID";
	private static final String ITEM_NAME_KEY = "itemName";
	private static final String TIMESTAMP_KEY = "timestamp";
	private static final String UNLOCKED_KEY = "unlocked";

	private Map<String, Item> mReturnedItems;

	/**
	 * Listener which will be notified after parsing the response. Every response type may call a
	 * different listener method.
	 */
	protected SPUnlockResponseListener mListener;

	/**
	 * Set the response listener which will be notified when the parsing is complete. Every response
	 * type may call a different listener method.
	 * 
	 * @param listener
	 */
	public void setResponseListener(SPUnlockResponseListener listener) {
		mListener = listener;
	}

	public Map<String, Item> getItems() {
		return mReturnedItems;
	}

	@Override
	public void parseSuccessfulResponse() {
		try {
			JSONArray responseBodyAsJsonArray = new JSONArray(mResponseBody);
			int responseItemsCount = responseBodyAsJsonArray.length();
			mReturnedItems = new HashMap<String, UnlockedItemsResponse.Item>(responseItemsCount);
			for (int i = 0; i < responseItemsCount; i++) {
				JSONObject itemAsJsonObject = responseBodyAsJsonArray.getJSONObject(i);
				Item parsedItem = new Item();
				parsedItem.id = itemAsJsonObject.getString(ITEM_ID_KEY);
				parsedItem.name = itemAsJsonObject.getString(ITEM_NAME_KEY);
				parsedItem.unlocked = itemAsJsonObject.getBoolean(UNLOCKED_KEY);
				parsedItem.timestamp = itemAsJsonObject.getLong(TIMESTAMP_KEY);

				mReturnedItems.put(parsedItem.name, parsedItem);
			}
			mErrorType = RequestErrorType.NO_ERROR;
		} catch (JSONException e) {
			mErrorType = RequestErrorType.ERROR_INVALID_RESPONSE;
		}
	}

	@Override
	public void invokeOnSuccessCallback() {
		if (mListener != null) {
			mListener.onSPUnlockItemsStatusResponseReceived(this);
		}
	}

	@Override
	public void invokeOnErrorCallback() {
		if (mListener != null) {
			mListener.onSPUnlockRequestError(this);
		}
	}

	void setErrorType(RequestErrorType errorType) {
		mErrorType = errorType;
	}

	public static class Item {
		private String id, name;
		private long timestamp;
		private boolean unlocked;

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public boolean isUnlocked() {
			return unlocked;
		}

		@Override
		public String toString() {
			return String.format("Item ID: %s, name: %s, unlocked: %s, timestamp:%d", id, name,
					unlocked ? "true" : "false", timestamp);
		}
	}
}
