/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.mediation;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;

/**
 * <p>
 * Interface for SP Android Mediation adapter
 * </p>
 */
public interface SPMediationAdapter {

	/**
	 * Initializes the wrapped SDK, usually with the necessary credentials.
	 * @param activity
	 * 			The parent activity calling this method
	 * @return 
	 * 			true if the adapter was successfully started, false otherwise
	 */
	public boolean startAdapter(Activity activity);
	
	/**
	 * @return the name of the wrapped video network.
	 */
	public String getName();
	
	/**
	 * @return the current version of the adapter
	 */
	public String getVersion();
	
	public boolean supportMediationFormat(SPMediationFormat format);

	public void validate(Context context, SPMediationFormat adFormat,
			SPMediationValidationEvent validationEvent,
			HashMap<String, String> contextData);

	public void startEngagement(Activity parentActivity,
			SPMediationFormat adFormat, SPMediationEngagementEvent engagementEvent,
			HashMap<String, String> contextData);
	
}
