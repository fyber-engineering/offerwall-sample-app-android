package com.sponsorpay.sdk.mbe.mediation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

public class MediationConfigurationFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		ExpandableListView lv = new ExpandableListView(getActivity());
//		lv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
//		
//		lv.setAdapter(new MockMediationListAdapter(getActivity()));
		
		ListView lv = new ListView(getActivity());
		lv.setAdapter(new MockMediationBaseAdapter(getActivity()));
		lv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		return lv;
	}
	
}
