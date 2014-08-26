/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.user;

public enum SPUserEthnicity{
	asian("asian"),
	black("black"),
	hispanic("hispanic"),
	indian("indian"),
	middle_eastern("middle eastern"),	
	native_american("native american"),	
	pacific_islander("pacific islander"),	
	white("white"),	
	other("other");
	
	public final String ethnicity;

	private SPUserEthnicity(String ethnicity) {
		this.ethnicity = ethnicity;
	}

	@Override
	public String toString() {
		return ethnicity;
	}
}