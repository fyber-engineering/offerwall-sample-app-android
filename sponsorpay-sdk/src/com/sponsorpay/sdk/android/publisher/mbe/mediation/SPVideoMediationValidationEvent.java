/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.Map;

public interface SPVideoMediationValidationEvent {

	public void validationEventResult(String adapterName,
			SPTPNVideoValidationResult result, Map<String, String> contextData);

}
