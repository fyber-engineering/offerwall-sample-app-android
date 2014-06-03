/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SPIdValidator {
	
	private static Pattern PATTERN = Pattern.compile("^[A-Z0-9_]+$");

	public static void validate(String id) throws SPIdException {
		if (StringUtils.nullOrEmpty(id)) {
			throw new SPIdException("An ID cannot be null or empty.");
		}
		Matcher matcher = PATTERN.matcher(id);
		if (!matcher.find()) {
			throw new SPIdException("An ID can only contain uppercase letters, numbers "
					+ "and the _ underscore symbol.");
		}
	}

}
