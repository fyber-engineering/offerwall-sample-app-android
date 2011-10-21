/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.content.Context;

import com.sponsorpay.sdk.android.DeviceInfo;

/**
 * Retrieves and contains all the information from {@link DeviceInfo} plus the SponsorPay App ID contained in the
 * Android Application Manifest.
 */
public class PublisherHostInfo extends DeviceInfo {

	/**
	 * The Sponsorpay App ID Key that is used in the AndroidManifest.xml file.
	 */
	private static final String SPONSORPAY_APP_ID_KEY = "SPONSORPAY_APP_ID";

	/**
	 * The App ID value.
	 */
	private long mAppId;

	/**
	 * Constructor. Requires an Android application context which will be used to retrieve information from the device
	 * and the host application's Android Manifest.
	 * 
	 * @param pContext
	 *            Android application context.
	 */
	public PublisherHostInfo(Context context) {
		super(context);
	}

	/**
	 * <p>
	 * Extracts the App ID from the host application's Android Manifest XML file.
	 * </p>
	 * 
	 * <p>
	 * If the Offer Id has already been set (i.e. by calling the {@link #setOverriddenAppId(long)}), this method will
	 * just return the id which has been set without trying to retrieve it from the manifest.
	 * </p>
	 * 
	 * <p>
	 * If no App ID is present in the manifest and no ID has been set by calling the mentioned method, this method will
	 * return 0.
	 * </p>
	 * 
	 * @return The offer id previously set or defined in the manifest, or 0.
	 */
	public long getAppId() {
		if (mAppId == 0) {
			mAppId = getLongFromAppMetadata(SPONSORPAY_APP_ID_KEY);
		}
		return mAppId;
	}

	/**
	 * Set the offerId, overriding the one which would be read from the manifest.
	 * 
	 * @param offerId
	 */
	public void setOverriddenAppId(long appId) {
		mAppId = appId;
	}
}
