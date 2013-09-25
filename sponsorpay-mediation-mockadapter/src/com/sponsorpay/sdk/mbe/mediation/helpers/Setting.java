package com.sponsorpay.sdk.mbe.mediation.helpers;


public class Setting implements Item{

	private MockSetting setting;

	public Setting(MockSetting setting){
		this.setting = setting;
	}

	@Override
	public String getTitle() {
		return setting.toString();
	}

	@Override
	public boolean isClickable() {
		return true;
	}
	
}
