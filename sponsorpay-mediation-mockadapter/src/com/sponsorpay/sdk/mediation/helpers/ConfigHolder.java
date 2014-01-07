/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.helpers;


public class ConfigHolder {

	public static final ConfigHolder INSTANCE = new ConfigHolder();
	
	private MockSetting mSetting;
	
	private ConfigHolder() {
		mSetting = MockSetting.PlayingStartedFinishedClosed;
	}
	
	public void setCurrentConfig(MockSetting setting) {
		mSetting = setting;
	}
	
	public MockSetting getCurrentConfig() {
		return mSetting;
	}
	
}