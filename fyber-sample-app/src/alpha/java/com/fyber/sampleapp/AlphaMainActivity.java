package com.fyber.sampleapp;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.fyber.annotations.FyberSDK;

import io.fabric.sdk.android.Fabric;

@FyberSDK
public class AlphaMainActivity extends MainActivity {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
	}
}
