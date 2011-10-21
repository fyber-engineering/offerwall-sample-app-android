/**
 * SponsorPay Android Advertiser SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */
package com.sponsorpay.sdk.android;

import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Extracts device information from the host device in which the SDK runs.
 */
public class DeviceInfo {
	/**
	 * Prefix appended to the OS version to identify the Android platform.
	 */
	private static final String ANDROID_OS_PREFIX = "Android OS ";

	/**
	 * The unique device ID.
	 */
	private String mUDID;

	/**
	 * The running Android OS version (e.g. "2.1" for Android 2.1).
	 */
	private String mOsVersion;

	/**
	 * The device version (e.g. "HTC Nexus One").
	 */
	private String mPhoneVersion;

	/**
	 * Language settings (the default locale).
	 */
	private String mLanguageSetting;

	/**
	 * Android ID as reported by Settings.Secure.ANDROID_ID
	 */
	private String mAndroidId;
	
	/**
	 * MAC Address of the WiFi Adapter
	 */
	private String mWifiMacAddress;
	
	/**
	 * Android application context, used to retrieve the rest of the properties.
	 */
	protected Context mContext;

	/**
	 * Get the unique device ID
	 * 
	 * @return the unique device id
	 */
	public String getUDID() {
		return mUDID;
	}

	/**
	 * Get the running OS version
	 * 
	 * @return the OS version
	 */
	public String getOsVersion() {
		return mOsVersion;
	}

	/**
	 * Get the current phone model
	 * 
	 * @return the phone model
	 */
	public String getPhoneVersion() {
		return mPhoneVersion;
	}

	/**
	 * Get the default locale set by the user
	 * 
	 * @return the default language setting
	 */
	public String getLanguageSetting() {
		return mLanguageSetting;
	}

	/**
	 * Returns the device's Android ID. 
	 */
	public String getAndroidId() {
		return mAndroidId;
	}
	
	/**
	 * Returns the MAC address of the device's WiFi adapter. 
	 */
	public String getWifiMacAddress() {
		return mWifiMacAddress;
	}
	
	/**
	 * The constructor immediately retrieves all the device information.
	 * 
	 * @param context
	 *            Android application context
	 */
	public DeviceInfo(Context context) {
		mContext = context;

		// Get access to the Telephony Services
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		// Get the device id (UDID)
		mUDID = tManager.getDeviceId();

		// Get the default locale
		mLanguageSetting = Locale.getDefault().toString();

		// Get the Android version
		mOsVersion = ANDROID_OS_PREFIX + android.os.Build.VERSION.RELEASE;

		// Get the phone model
		mPhoneVersion = android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL;
		
		// Android ID
		mAndroidId = Settings.Secure.ANDROID_ID;
		
		if (mAndroidId == null) {
			mAndroidId = "";
		}
		
		try {
			// MAC address of WiFi adapter
			WifiManager wifiMan = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInf = wifiMan.getConnectionInfo();
			mWifiMacAddress = wifiInf.getMacAddress();
		} catch (RuntimeException re) {	}
		
		if (mWifiMacAddress == null) {
			mWifiMacAddress = "";
		}
	}

	/**
	 * Extracts a String value from the meta-data configured in the application manifest XML file
	 * 
	 * @param key
	 *            key to identify the piece of meta-data to return
	 * @return the value for the given key
	 */
	protected String getStringFromAppMetadata(String key) {
		String stringToReturn;
		ApplicationInfo ai;

		try {
			// Extract the meta data from the package manager
			ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(),
					PackageManager.GET_META_DATA);
			Bundle metadata = ai.metaData;

			/*
			 * If no meta data has been set, return nothing. Otherwise, get the value for the given key.
			 */
			if (metadata == null) {
				stringToReturn = null;
			} else {
				stringToReturn = metadata.getString(key);
			}
		} catch (NameNotFoundException e) {

			// The key wasn't found in the meta data, thus, we have to return null.
			stringToReturn = null;
		}

		return stringToReturn;
	}

	/**
	 * Extracts a long value from the meta-data configured in the application manifest XML file
	 * 
	 * @param key
	 *            key to identify the piece of meta-data to return
	 * @return the value for the given key
	 */
	protected long getLongFromAppMetadata(String key) {
		long longToReturn;
		ApplicationInfo ai;
		try {
			ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(),
					PackageManager.GET_META_DATA);
			Bundle metadata = ai.metaData;

			if (metadata == null) {
				longToReturn = 0;
			} else {
				try {
					longToReturn = metadata.getInt(key);
				} catch (ClassCastException e) {
					longToReturn = metadata.getLong(key);
				}
			}
		} catch (NameNotFoundException e) {
			longToReturn = 0;
		}

		return longToReturn;
	}
}
