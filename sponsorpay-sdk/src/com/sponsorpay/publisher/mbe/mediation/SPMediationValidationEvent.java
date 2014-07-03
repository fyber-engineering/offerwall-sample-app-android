/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe.mediation;

import java.util.Map;

public interface SPMediationValidationEvent {

	public void validationEventResult(String adapterName,
			SPTPNVideoValidationResult result, Map<String, String> contextData);

}
