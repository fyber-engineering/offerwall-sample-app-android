package com.sponsorpay.sdk.mbe.mediation;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sponsorpay.sdk.mbe.mediation.helpers.Item;
import com.sponsorpay.sdk.mbe.mediation.helpers.MockSetting;
import com.sponsorpay.sdk.mbe.mediation.helpers.Section;
import com.sponsorpay.sdk.mbe.mediation.helpers.Setting;

public class MockMediationBaseAdapter extends BaseAdapter {


			
	private Activity activity;
	private LayoutInflater inflater;
	
	private List<Item> items = new LinkedList<Item>();
	
	public MockMediationBaseAdapter(Activity act) {
		activity = act;
		inflater = act.getLayoutInflater();
		items.add(new Section("Validation events"));
		items.add(new Setting(MockSetting.SPValidationTimeout));
		items.add(new Setting(MockSetting.TPNValidationTimeout));
		items.add(new Setting(MockSetting.NoVideoAvailable));
		items.add(new Setting(MockSetting.NetowrokError));
		items.add(new Setting(MockSetting.DiskError));
		items.add(new Setting(MockSetting.OtherError));

		items.add(new Section("Video playing events"));

		items.add(new Setting(MockSetting.PlayingTimeout));
		items.add(new Setting(MockSetting.PlayingStartAndTimeout));
		items.add(new Setting(MockSetting.PlayingStartedAborted));
		items.add(new Setting(MockSetting.PlayingStartedFinishedClosed));
		items.add(new Setting(MockSetting.PlayingNoVideo));
		items.add(new Setting(MockSetting.PlayingOtherError));
		items.add(new Setting(MockSetting.PlayingStartedOtherError));
	}
	
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    final Item item = (Item) getItem(position);
	    TextView text = null;
	    if (convertView == null) {
	    	if (item instanceof Section) {
	    		convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
	    	} else {
	    		convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
	    	}
	    }
	    text = (TextView) convertView.findViewById(android.R.id.text1);
	    text.setText(item.getTitle());
	    if (item.isClickable()) {
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(activity, item.getTitle(), Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	    return convertView;
	}
	
}
