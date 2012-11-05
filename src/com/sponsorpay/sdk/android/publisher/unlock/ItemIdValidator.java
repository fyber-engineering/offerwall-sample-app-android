/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.unlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sponsorpay.sdk.android.utils.StringUtils;

public class ItemIdValidator {
	private String mValue;
	private Pattern mCompiledPattern;

	public ItemIdValidator(String itemId) {
		setValue(itemId);
	}

	public void setValue(String value) {
		String previousValue = mValue;
		mValue = value;

		if (value == null || !value.equals(previousValue)) {
			mCompiledPattern = null;
		}
	}

	public boolean validate() {
		if (StringUtils.nullOrEmpty(mValue)) {
			return false;
		}

		if (mCompiledPattern == null) {
			String pattern = "^[A-Z0-9_]+$";
			mCompiledPattern = Pattern.compile(pattern);
		}
		Matcher matcher = mCompiledPattern.matcher(mValue);
		return matcher.find();
	}

	public String getValidationDescription() {
		return "An Unlock Item ID can only contain uppercase letters, numbers "
				+ "and the _ underscore symbol.";
	}
}
