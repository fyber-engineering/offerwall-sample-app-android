package com.sponsorpay.sdk.mbe.mediation.helpers;


public class Section implements Item{

	private String title;

	public Section(String title) {
		this.title = title;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean isClickable() {
		return false;
	}
	
	
}
