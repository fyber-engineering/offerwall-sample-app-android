package com.sponsorpay.sdk.android.testapp.fragments;

import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sponsorpay.sdk.android.publisher.AbstractResponse;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.unlock.SPUnlockResponseListener;
import com.sponsorpay.sdk.android.publisher.unlock.UnlockedItemsResponse;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class ItemsSettingsFragment extends AbstractSettingsFragment {
	
	private static final String UNLOCK_ITEM_ID_PREFS_KEY = "UNLOCK_ITEM_ID";
	private static final String UNLOCK_ITEM_NAME_PREFS_KEY = "UNLOCK_ITEM_NAME";
	private String mUnlockItemId;
	private String mUnlockItemName;
	private EditText mUnlockItemIdField;
	private EditText mUnlockItemNameField;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_items, container, false);
	}
	

	@Override
	protected String getFragmentTitle() {
		return getResources().getString(R.string.items);
	}


	@Override
	protected void setValuesInFields() {
		mUnlockItemIdField.setText(mUnlockItemId);
		mUnlockItemNameField.setText(mUnlockItemName);
	}


	@Override
	protected void bindViews() {
		mUnlockItemIdField = (EditText) findViewById(R.id.unlock_item_id_field);
		mUnlockItemNameField = (EditText) findViewById(R.id.unlock_item_name_field);
	}


	@Override
	protected void fetchValuesFromFields() {
		mUnlockItemId = mUnlockItemIdField.getText().toString();
		mUnlockItemName = mUnlockItemNameField.getText().toString();
	}


	@Override
	protected void readPreferences(SharedPreferences prefs) {
		mUnlockItemId = prefs.getString(UNLOCK_ITEM_ID_PREFS_KEY, StringUtils.EMPTY_STRING);
		mUnlockItemName = prefs.getString(UNLOCK_ITEM_NAME_PREFS_KEY, StringUtils.EMPTY_STRING);
	}


	@Override
	protected void storePreferences(Editor prefsEditor) {
		prefsEditor.putString(UNLOCK_ITEM_ID_PREFS_KEY, mUnlockItemId);
		prefsEditor.putString(UNLOCK_ITEM_NAME_PREFS_KEY, mUnlockItemName);
	}
	
	
	public void launchUnlockOfferWall(String userId, String overridingAppId){
		fetchValuesFromFields();
		try {
			startActivityForResult(SponsorPayPublisher.getIntentForUnlockOfferWallActivity(
					getApplicationContext(), userId, mUnlockItemId, mUnlockItemName, overridingAppId, null),
					SponsorPayPublisher.DEFAULT_UNLOCK_OFFERWALL_REQUEST_CODE);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
					ex);
		}
	}
	
	public void launchUnlockItems(String userId, String securityToken, String overridingAppId){
		fetchValuesFromFields();
		
		SPUnlockResponseListener listener = new SPUnlockResponseListener() {
			@Override
			public void onSPUnlockRequestError(AbstractResponse response) {
				showCancellableAlertBox("Response or Request Error", String.format("%s\n%s\n%s\n",
						response.getErrorType(), response.getErrorCode(), response
								.getErrorMessage()));
			}

			@Override
			public void onSPUnlockItemsStatusResponseReceived(UnlockedItemsResponse response) {
				
				Log.i("SPPlugin","inside item response");
				Map<String, UnlockedItemsResponse.Item> map = response.getItems();
				
				Log.i("SPPlugin","got map with "+map.size());
				
				Set<String> set = map.keySet();
				
				Object[] strings = set.toArray();
				for (int i = 0; i < strings.length; i++) {
					Log.i("SPPlugin", strings[i].toString());
				}
				
				UnlockedItemsResponse.Item item = map.get("SFTEST_ITEM_1");
				if (item != null) {
					Log.i("SPPlugin",
							"item id is " + item.getId() + " " + item.getName());
					if (item.isUnlocked()) {
						Log.i("SPPlugin", "item is open");
					} else {
						Log.i("SPPlugin", "item is locked");
					}
				} else {
					Log.i("SPPlugin", "item is empty");
				}
				
				Map<String, UnlockedItemsResponse.Item> items = response.getItems();

				UnlockedItemsResponse.Item[] values = new UnlockedItemsResponse.Item[items.size()];
				values = items.values().toArray(values);

				ArrayAdapter<UnlockedItemsResponse.Item> adapter = new ArrayAdapter<UnlockedItemsResponse.Item>(
						getApplicationContext(), R.layout.unlock_list_item, R.id.item_name, values) {

					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View view = super.getView(position, convertView, parent);

						TextView itemId = (TextView) view.findViewById(R.id.item_id);
						TextView itemName = (TextView) view.findViewById(R.id.item_name);
						TextView itemUnlocked = (TextView) view.findViewById(R.id.item_unlocked);
						TextView itemUnlockTimestamp = (TextView) view
								.findViewById(R.id.item_unlock_timestamp);

						itemId.setText(getItem(position).getId());
						itemName.setText(getItem(position).getName());
						itemUnlocked.setText(getItem(position).isUnlocked() ? "Unlocked"
								: "Not unlocked");

						if (getItem(position).isUnlocked()) {
							final long millisecondsInSecond = 1000;
							CharSequence formattedDate = DateFormat.format("MMM dd, yyyy h:mmaa",
									getItem(position).getTimestamp() * millisecondsInSecond);

							itemUnlockTimestamp.setText(formattedDate);
						} else {
							itemUnlockTimestamp.setText("---");
						}
						return view;
					}
				};

				ListView listView = new ListView(getApplicationContext());
				listView.setAdapter(adapter);

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
						getActivity());
				dialogBuilder.setTitle("Response From SponsorPay Unlock Server").setView(listView)
						.setCancelable(true);
				dialogBuilder.show();

			}
		};

		try {
			SponsorPayPublisher.requestUnlockItemsStatus(getApplicationContext(), userId,
					listener, securityToken, overridingAppId, null);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
					ex);
		}
	}
	
}
