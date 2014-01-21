/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.interstitial;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MockInterstitialMediationListAdapter extends BaseAdapter {

//	private MockVideoSetting[] validation = {MockVideoSetting.SPValidationTimeout,
//			MockVideoSetting.TPNValidationTimeout,
//			MockVideoSetting.NoVideoAvailable,
//			MockVideoSetting.NetowrokError,
//			MockVideoSetting.DiskError,
//			MockVideoSetting.OtherError};
//	
//	private MockVideoSetting[] video = {MockVideoSetting.PlayingTimeout,
//			MockVideoSetting.PlayingStartAndTimeout,
//			MockVideoSetting.PlayingStartedAborted,
//			MockVideoSetting.PlayingStartedFinishedClosed,
//			MockVideoSetting.PlayingNoVideo,
//			MockVideoSetting.PlayingOtherError,
//			MockVideoSetting.PlayingStartedOtherError};
//	
//	private MockVideoSetting[][] groups = {validation, video};

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
//	    final MockVideoSetting child = (MockVideoSetting) getChild(groupPosition, childPosition);
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
	
//	@Override
//	public Object getChild(int arg0, int arg1) {
//		return groups[arg0][arg1];
//	}
//
//	@Override
//	public long getChildId(int groupPosition, int childPosition) {
//		return 0;
//	}
//
//	@Override
//	public View getChildView(int groupPosition, int childPosition,
//			boolean isLastChild, View convertView, ViewGroup parent) {
//	    final MockVideoSetting child = (MockVideoSetting) getChild(groupPosition, childPosition);
//	    TextView text = null;
//	    if (convertView == null) {
//	      convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
//	      convertView.setOnClickListener(listener);
//	    }
//	    convertView.setTag(child);
//	    text = (TextView) convertView.findViewById(android.R.id.text1);
//	    text.setText(child.toString());
//
//	    return convertView;
//	}
//
//	@Override
//	public int getChildrenCount(int groupPosition) {
//		return groups[groupPosition].length;
//	}
//
//	@Override
//	public Object getGroup(int groupPosition) {
//		if (groupPosition == 0) {
//			return "Validation events";
//		} else {
//			return "Video playing events";
//		}
//	}
//
//	@Override
//	public int getGroupCount() {
//		return groups.length;
//	}
//
//	@Override
//	public long getGroupId(int groupPosition) {
//		return 0;
//	}
//
//	@Override
//	public View getGroupView(int groupPosition, boolean isExpanded,
//			View convertView, ViewGroup parent) {
//		if (convertView == null) {
//			convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
//		}
//		TextView text = null;
//		text = (TextView) convertView.findViewById(android.R.id.text1);
//		text.setText(getGroup(groupPosition).toString());
//
//		return convertView;
//	}
//
//	@Override
//	public boolean hasStableIds() {
//		return false;
//	}
//
//	@Override
//	public boolean isChildSelectable(int groupPosition, int childPosition) {
//		return true;
//	}
		
}
