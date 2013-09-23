package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.Map;

public interface SPMediationVideoEvent {

	public void videoEventOccured(String name, SPTPNVideoEvent event, Map<String, String> contextData);
	
}
