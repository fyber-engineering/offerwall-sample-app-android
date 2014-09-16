/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.io.IOException;

import android.os.AsyncTask;

public class GetRemoteFileContentTask extends AsyncTask<String, Void, String> {

	private static final String TAG = "GetRemoteFileContentTask";

	@Override
	protected String doInBackground(String... params) {
		Thread.currentThread().setName(TAG);
		try {
			return SPHttpConnection.getConnection(params[0]).open().getBodyContent();
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		}
		return null;
	}
    
}
