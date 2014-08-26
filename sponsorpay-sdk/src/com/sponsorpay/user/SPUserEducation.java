/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.user;

public enum SPUserEducation{
	other("other"),	
	none("none"),	
	high_school("high school"),	
	in_college("in college"),
	some_college("some college"),	
	associates("other"),	
	bachelors("other"),	
	masters("other"),	
	doctorate("other");
	
	
    public final String education;
    
    private SPUserEducation(String education) {
        this.education = education;
    }
    
    public String getEducation(){
        return this.education;
    }
}