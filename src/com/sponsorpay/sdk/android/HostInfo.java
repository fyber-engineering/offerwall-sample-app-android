/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android;

import java.lang.reflect.Field;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.sponsorpay.sdk.android.utils.StringUtils;

/**
 * Extracts device information from the host device in which the SDK runs and SponsorPay App ID
 * contained in the Android Application Manifest of the host app.
 */
public class HostInfo {

	/**
	 * Prefix appended to the OS version to identify the Android platform.
	 */
	private static final String ANDROID_OS_PREFIX = "Android OS ";

	private static final String SCREEN_DENSITY_CATEGORY_VALUE_LOW = "LOW";

	private static final String SCREEN_DENSITY_CATEGORY_VALUE_MEDIUM = "MEDIUM";

	private static final String SCREEN_DENSITY_CATEGORY_VALUE_HIGH = "HIGH";

	private static final String SCREEN_DENSITY_CATEGORY_VALUE_EXTRA_HIGH = "EXTRA_HIGH";

	private static final String SCREEN_DENSITY_CATEGORY_VALUE_TV = "TV";

	private static final String UNDEFINED_VALUE = "undefined";

	private static final String CONNECTION_TYPE_CELLULAR  = "cellular";
	private static final String CONNECTION_TYPE_WIFI = "wifi";
	
	protected static boolean sSimulateNoReadPhoneStatePermission = false;
	protected static boolean sSimulateNoAccessWifiStatePermission = false;
	protected static boolean sSimulateInvalidAndroidId = false;
	protected static boolean sSimulateNoHardwareSerialNumber = false;
	protected static boolean sSimulateNoAccessNetworkState = false;

	public static void setSimulateNoReadPhoneStatePermission(boolean value) {
		sSimulateNoReadPhoneStatePermission = value;
	}

	public static void setSimulateNoAccessWifiStatePermission(boolean value) {
		sSimulateNoAccessWifiStatePermission = value;
	}

	public static void setSimulateInvalidAndroidId(boolean value) {
		sSimulateInvalidAndroidId = value;
	}

	public static void setSimulateNoHardwareSerialNumber(boolean value) {
		sSimulateNoHardwareSerialNumber = value;
	}

	public static void setSimulateNoAccessNetworkState(boolean value) {
		sSimulateNoAccessNetworkState = value;
	}

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
	 * Android ID as reported by Settings.Secure
	 */
	private String mAndroidId;

	/**
	 * MAC Address of the WiFi Adapter
	 */
	private String mWifiMacAddress;

	/**
	 * Device's hardware serial number, reported by versions of Android >=2.3
	 */
	private String mHardwareSerialNumber;

	/**
	 * The SponsorPay's App ID Key that is used in the AndroidManifest.xml file.
	 * 
	 * @deprecated this will no longer be supported in a future release of the SDK
	 */
	private static final String SPONSORPAY_APP_ID_KEY = "SPONSORPAY_APP_ID";

	/**
	 * The App ID value.
	 */
	private String mAppId;

	/**
	 * Android application context, used to retrieve the rest of the properties.
	 */
	private Context mContext;

	private DisplayMetrics mDisplayMetrics;

	private String mScreenDensityCategory;
	private int mScreenWidth;
	private int mScreenHeight;
	private float mScreenDensityX;
	private float mScreenDensityY;

	private String mCarrierCountry;
	private String mCarrierName;

	private String mConnectionType;

	private String mAppVersion;

	private DisplayMetrics getDisplayMetrics() {
		if (null == mDisplayMetrics) {
			mDisplayMetrics = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
		}
		return mDisplayMetrics;
	}

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
	 * Fetches the device's hardware serial number, reported by versions of Android >=2.3
	 */
	public String getHardwareSerialNumber() {
		if (mHardwareSerialNumber == null) {
			if (!sSimulateNoHardwareSerialNumber) {
				Field serialField = null;
				try {
					serialField = android.os.Build.class.getField("SERIAL");
					Object serialValue = serialField.get(null);
					if (serialValue != null && serialValue.getClass().equals(String.class)) {
						mHardwareSerialNumber = (String) serialValue;
					}
				} catch (Exception e) {
					// Probably running on an older version of Android which doesn't include this
					// field
					mHardwareSerialNumber = StringUtils.EMPTY_STRING;
				}
			} else {
				mHardwareSerialNumber = StringUtils.EMPTY_STRING;
			}
		}
		return mHardwareSerialNumber;
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
	 * Constructor. Requires an Android application context which will be used to retrieve
	 * information from the device and the host application's Android Manifest.
	 * 
	 * @param context
	 *            Android application context
	 */
	public HostInfo(Context context) {
		mContext = context;

		if (!sSimulateNoReadPhoneStatePermission) {
			// Get access to the Telephony Services
			TelephonyManager tManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			try {
				mUDID = tManager.getDeviceId();
				mCarrierName = tManager.getNetworkOperatorName();
				mCarrierCountry = tManager.getNetworkCountryIso();
			} catch (SecurityException e) {
				mUDID = StringUtils.EMPTY_STRING;
				mCarrierName = StringUtils.EMPTY_STRING;
				mCarrierCountry = StringUtils.EMPTY_STRING;
			}
		} else {
			mUDID = StringUtils.EMPTY_STRING;
			mCarrierName = StringUtils.EMPTY_STRING;
			mCarrierCountry = StringUtils.EMPTY_STRING;
		}

		if (!sSimulateNoAccessNetworkState) {
			ConnectivityManager mConnectivity = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo info = mConnectivity.getActiveNetworkInfo();
			if (info == null) {
			    mConnectionType = StringUtils.EMPTY_STRING;
			} else {
				int netType = info.getType();
				mConnectionType = netType == ConnectivityManager.TYPE_WIFI ? CONNECTION_TYPE_WIFI
						: CONNECTION_TYPE_CELLULAR;
			}

		} else {
			mConnectionType = StringUtils.EMPTY_STRING;
		}
		
		// Get the default locale
		mLanguageSetting = Locale.getDefault().toString();

		// Get the Android version
		mOsVersion = ANDROID_OS_PREFIX + android.os.Build.VERSION.RELEASE;

		// Get the phone model
		mPhoneVersion = android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL;

		// Android ID
		if (!sSimulateInvalidAndroidId) {
			mAndroidId = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);

			if (mAndroidId == null) {
				mAndroidId = StringUtils.EMPTY_STRING;
			}
		} else {
			mAndroidId = StringUtils.EMPTY_STRING;
		}

		if (!sSimulateNoAccessWifiStatePermission) {
			try {
				// MAC address of WiFi adapter
				WifiManager wifiMan = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInf = wifiMan.getConnectionInfo();
				mWifiMacAddress = wifiInf.getMacAddress();
			} catch (RuntimeException re) {
				mWifiMacAddress = StringUtils.EMPTY_STRING;
			}
		} else {
			mWifiMacAddress = StringUtils.EMPTY_STRING;
		}
	}

	/**
	 * Extracts a numeric or alphanumeric value from the meta-data configured in the application
	 * manifest XML file and returns it as a String.
	 * 
	 * @param key
	 *            key to identify the piece of meta-data to return.
	 * @return the value for the given key, or null on failure.
	 */
	private String getValueFromAppMetadata(String key) {
		Object retrievedValue;

		ApplicationInfo ai = null;
		Bundle appMetadata = null;

		// Extract the meta data from the package manager
		try {
			ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(),
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return null;
		}

		appMetadata = ai.metaData;

		if (appMetadata == null) {
			return null;
		}

		retrievedValue = appMetadata.get(key);

		return retrievedValue == null ? null : retrievedValue.toString();
	}

	/**
	 * <p>
	 * Extracts the App ID from the host application's Android Manifest XML file.
	 * </p>
	 * 
	 * <p>
	 * If the App Id has already been set (i.e. by calling the {@link #setOverriddenAppId(String)}),
	 * this method will just return the id which has been set without trying to retrieve it from the
	 * manifest.
	 * </p>
	 * 
	 * <p>
	 * If no App ID is present in the manifest and no non-empty App ID has been set by calling the
	 * mentioned method, this method will throw a RuntimeException.
	 * </p>
	 * 
	 * @return The offer id previously set or defined in the manifest, or throws a {@link RuntimeException}.
	 */
	public String getAppId() {
		if (StringUtils.nullOrEmpty(mAppId)) {
			mAppId = getAppIdFromManifest();
			if (StringUtils.nullOrEmpty(mAppId)) {
				throw new RuntimeException(
						"SponsorPay SDK: no valid App ID has been provided. "
								+ "Please set a valid App ID in your application manifest or provide one at runtime. "
								+ "See the integration guide or the SDK javadoc for more information.");
			}
		}
		return mAppId;
	}
	/**
	 * <p>
	 * Get the Application ID set in meta-data on the AndroidManifest.xml file.
	 * </p>
	 * 
	 * @return the Application id set in the AndroidManifest.xml file
	 * 
	 * @deprecated this will no longer be supported in a future release of the SDK
	 */
	private String getAppIdFromManifest() {
		return StringUtils.trim(
				getValueFromAppMetadata(SPONSORPAY_APP_ID_KEY));
	}

	/**
	 * Set the application ID, overriding the one which would be read from the manifest.
	 * 
	 * @param appId 
	 * 			the application ID
	 */
	public void setOverriddenAppId(String appId) {
		mAppId = appId;
	}

	public String getScreenDensityCategory() {
		if (null == mScreenDensityCategory) {
			int densityCategoryDpi = getDisplayMetrics().densityDpi;

			switch (densityCategoryDpi) {
			case DisplayMetrics.DENSITY_MEDIUM:
				mScreenDensityCategory = SCREEN_DENSITY_CATEGORY_VALUE_MEDIUM;
				break;
			case DisplayMetrics.DENSITY_HIGH:
				mScreenDensityCategory = SCREEN_DENSITY_CATEGORY_VALUE_HIGH;
				break;
			case DisplayMetrics.DENSITY_LOW:
				mScreenDensityCategory = SCREEN_DENSITY_CATEGORY_VALUE_LOW;
				break;
			default:
				mScreenDensityCategory = getScreenDensityCategoryFromModernAndroidDevice(densityCategoryDpi);
			}
		}
		return mScreenDensityCategory;
	}

	private String getScreenDensityCategoryFromModernAndroidDevice(int densityCategoryDpi) {
		String[] remainingScreenDensityCategoryStaticFieldNames = { "DENSITY_XHIGH", "DENSITY_TV" };
		String[] correspondingScreenDensityCategoryStringValues = { SCREEN_DENSITY_CATEGORY_VALUE_EXTRA_HIGH, SCREEN_DENSITY_CATEGORY_VALUE_TV };

		int iterationLimit = Math.min(remainingScreenDensityCategoryStaticFieldNames.length,
				correspondingScreenDensityCategoryStringValues.length);

		String densityCategory = null;
		
		for (int i = 0; i < iterationLimit; i++) {
			try {
				Field eachField = DisplayMetrics.class
						.getField(remainingScreenDensityCategoryStaticFieldNames[i]);
				int thisFieldvalue = eachField.getInt(null);
				if (densityCategoryDpi == thisFieldvalue) {
					densityCategory = correspondingScreenDensityCategoryStringValues[i];
				}
			} catch (Exception e) {
				// Assumed field doesn't exist in the version of Android the host is running
			}
			
			if (null != densityCategory) {
				break;
			}
		}
		
		return null == densityCategory ? UNDEFINED_VALUE : densityCategory;
	}
	
	public String getScreenWidth() {
		if (0 == mScreenWidth) {
			mScreenWidth = getDisplayMetrics().widthPixels;
		}
		return String.format("%d", mScreenWidth);
	}
	
	public String getScreenHeight() {
		if (0 == mScreenHeight) {
			mScreenHeight = getDisplayMetrics().heightPixels;
		}
		return String.format("%d", mScreenHeight);
	}
	
	public String getScreenDensityX() {
		if (0 == mScreenDensityX) {
			mScreenDensityX = getDisplayMetrics().xdpi;
		}
		return String.format("%d", Math.round(mScreenDensityX));
	}
	
	public String getScreenDensityY() {
		if (0 == mScreenDensityY) {
			mScreenDensityY = getDisplayMetrics().ydpi;
		}
		return String.format("%d", Math.round(mScreenDensityY));
	}
	
	public String getCarrierCountry() {
		return mCarrierCountry;
	}
	
	public String getCarrierName() {
		return mCarrierName;
	}
	
	public String getConnectionType() {
		return mConnectionType;
	}

	public String getManufacturer() {
		return Build.MANUFACTURER;
	}

	public String getAppVersion() {
		if (mAppVersion == null) {
			try {
				PackageInfo pInfo = mContext.getPackageManager()
						.getPackageInfo(mContext.getPackageName(), 0);
				mAppVersion = pInfo.versionName;
			} catch (NameNotFoundException e) {
				mAppVersion = StringUtils.EMPTY_STRING;
			}
		}
		return mAppVersion;
	}

	public String getAppBundleName() {
		return mContext.getPackageName();
	}
}
