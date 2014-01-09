/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial.mediation;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;

import com.sponsorpay.sdk.android.mediation.SPMediationAdapter;
import com.sponsorpay.sdk.android.mediation.SPMediationEngagementEvent;
import com.sponsorpay.sdk.android.mediation.SPMediationValidationEvent;

public abstract class SPInterstitialMediationAdapter<V extends SPMediationAdapter> {

	protected V mAdapter;

	public SPInterstitialMediationAdapter(V adapter) {
		mAdapter = adapter;
	}

	public abstract boolean interstitialAvailable(Context context,
			SPMediationValidationEvent validationEvent,
			HashMap<String, String> contextData);

	public abstract boolean show(Activity parentActivity,
			SPMediationEngagementEvent engagementEvent,
			HashMap<String, String> contextData);

}
