/**
 * SponsorPay Android Advertiser SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;


import android.content.Context;

/**
 * Retrieves and contains all the information from {@link DeviceInfo} plus the SponsorPay Program ID contained in the
 * Android Application Manifest.
 */
public class AdvertiserHostInfo extends DeviceInfo {
	/**
	 * The SponsorPay Program ID Key that is used in the AndroidManifest.xml file.
	 */
	private static final String SPONSORPAY_PROGRAM_ID_KEY = "SPONSORPAY_PROGRAM_ID";

	/**
	 * The Program ID value defined in the manifest.
	 */
	private String mProgramId;

	/**
	 * Constructor. Requires an Android application context which will be used to retrieve information from the device
	 * and the host application's Android Manifest.
	 * 
	 * @param pContext
	 *            Android application context.
	 */
	public AdvertiserHostInfo(Context pContext) {
		super(pContext);
	}

	/**
	 * <p>
	 * Extracts the Program ID from the host application's Android Manifest XML file. If the program id is not defined
	 * in the manifest as {@value AdvertiserHostInfo#SPONSORPAY_PROGRAM_ID_KEY} a {@link RuntimeException} will be
	 * thrown, unless Program Id has already been set by other means.
	 * </p>
	 * 
	 * <p>
	 * If the Program Id has already been set (i.e. by calling {@link #setOverridenProgramId(String)}), this method will
	 * just return the id which has been set without trying to retrieve it from the manifest.
	 * </p>
	 * 
	 * @return The program id defined in the manifest.
	 */
	public String getProgramId() {
		/* If no program id has been set, yet, try to get it from the manifest */
		if (mProgramId == null) {
			/* If the key was not found in the manifest, throw an exception */
			mProgramId = String.valueOf(getLongFromAppMetadata(SPONSORPAY_PROGRAM_ID_KEY));
			if (mProgramId == null || mProgramId.equals("0")) {
				throw new RuntimeException("SponsorPay program ID must be set in AndroidManifest.xml");
			}
		}
		return mProgramId;
	}

	/**
	 * Set the programId, overriding the one which would be read from the manifest.
	 * 
	 * @param programId
	 *            The programId to set.
	 */
	public void setOverriddenProgramId(String programId) {
		mProgramId = programId;
	}
}
