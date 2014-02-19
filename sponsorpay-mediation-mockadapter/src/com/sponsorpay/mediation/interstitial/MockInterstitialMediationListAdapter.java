/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.interstitial;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MockInterstitialMediationListAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private OnClickListener listener;
	
	public MockInterstitialMediationListAdapter(Activity act, OnClickListener listener) {
		this.listener = listener;
	    inflater = act.getLayoutInflater();
	  }

	@Override
	public int getCount() {
		return MockInterstitialSetting.values().length;
	}

	@Override
	public Object getItem(int position) {
		return MockInterstitialSetting.values()[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MockInterstitialSetting item = (MockInterstitialSetting) getItem(position);
	    TextView text = null;
	    if (convertView == null) {
	      convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
	      convertView.setOnClickListener(listener);
	    }
	    convertView.setTag(item);
	    text = (TextView) convertView.findViewById(android.R.id.text1);
	    text.setText(item.toString());

	    return convertView;
	}
	
}
