/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.Map;

import com.sponsorpay.sdk.android.mediation.SPMediationEngagementEvent;

public interface SPMediationVideoEvent extends SPMediationEngagementEvent{

	public void videoEventOccured(String adapterName, SPTPNVideoEvent event,
			Map<String, String> contextData);

}
