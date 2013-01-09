package com.sponsorpay.sdk.android.testapp.fragments;

import com.sponsorpay.sdk.android.testapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LauncherFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_launcher, container, false);
	}
	
}
