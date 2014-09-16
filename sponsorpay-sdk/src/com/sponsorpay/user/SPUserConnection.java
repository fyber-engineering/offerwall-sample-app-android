/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.user;

public enum SPUserConnection{
	wifi("wifi"),	
	three_g("3g");

    public final String connection;
    
    private SPUserConnection(String connection) {
        this.connection = connection;
    }

	@Override
	public String toString() {
		return connection;
	}
    
}