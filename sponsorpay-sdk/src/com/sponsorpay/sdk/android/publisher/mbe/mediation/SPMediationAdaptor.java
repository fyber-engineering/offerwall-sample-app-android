/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.Map;

import android.app.Activity;

public interface SPMediationAdaptor {

	public boolean startAdaptor(Activity activity);

	public String getName();

	public String getVersion();

	public void videosAvailable(SPMediationValidationEvent event,
			Map<String, String> contextData);

	public void startVideo(Activity parentActivity,
			SPMediationVideoEvent event, Map<String, String> contextData);

}
