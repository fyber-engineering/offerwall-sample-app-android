/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;


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

	private static HostInfo hostinInfoInstance;

	public static HostInfo getHostInfo(Context context) {
		if (hostinInfoInstance == null) {
			hostinInfoInstance = new HostInfo(context);
		}
		return hostinInfoInstance;
	}
	
	
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

	private String mAdvertisingId;
	
	private boolean mAdvertisingIdLimitedTrackingEnabled = true;
	
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
		if (mDisplayMetrics == null) {
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

	
	private CountDownLatch mIdLatch = new CountDownLatch(1);
	
	/**
	 * Constructor. Requires an Android application context which will be used to retrieve
	 * information from the device and the host application's Android Manifest.
	 * 
	 * @param context
	 *            Android application context
	 */
	protected HostInfo(Context context) {
		if (context == null) {
			throw new RuntimeException("A context is required to initialize HostInfo");
		}
		mContext = context;
		
		// check if we're running in the main thread
		if (Looper.myLooper() == Looper.getMainLooper()) {
//			Handler handler = new Handler() {
//				public void handleMessage(Message msg) {
//					retrieveAdvertisingId();
//				};
//			};
//			handler.obtainMessage().sendToTarget();
			new Thread("AdvertisingIdRetriever") {
				public void run() {
					retrieveAdvertisingId();
				};
			}.start();
		} else {
			retrieveAdvertisingId();
		}

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

	protected void retrieveAdvertisingId() {
		try {
			
			//need to call this reflexively
//			import com.google.android.gms.ads.identifier.AdvertisingIdClient;
//			import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
			Class<?> advertisingIdClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
				
			Method getAdInfoMethod = advertisingIdClientClass.getMethod("getAdvertisingIdInfo", Context.class);
			Object adInfo = getAdInfoMethod.invoke(null, mContext);
			
			Method getIdMethod = adInfo.getClass().getMethod("getId");
			Method isLimitAdTrackingEnabledMethod = adInfo.getClass().getMethod("isLimitAdTrackingEnabled");
			
			mAdvertisingId = getIdMethod.invoke(adInfo).toString();
			mAdvertisingIdLimitedTrackingEnabled = (Boolean)isLimitAdTrackingEnabledMethod.invoke(adInfo);
			
//			Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
//			mAdvertisingId = adInfo.getId();
//			mAdvertisingIdLimitedTrackingEnabled = adInfo
//					.isLimitAdTrackingEnabled();
			
		} catch (Exception e) {
//			// Unrecoverable error connecting to Google Play services (e.g.,
//			// the old version of the service doesn't support getting
//			// AdvertisingId).
//			SponsorPayLogger.e("tttttttt", e.getLocalizedMessage(), e);
//		} catch (GooglePlayServicesNotAvailableException e) {
//			// Google Play services is not available entirely.
//			SponsorPayLogger.e("tttttttt", e.getLocalizedMessage(), e);
//		} catch (IllegalStateException e) {
//			SponsorPayLogger.e("tttttttt", e.getLocalizedMessage(), e);
//		} catch (GooglePlayServicesRepairableException e) {
			SponsorPayLogger.e("tttttttt", e.getLocalizedMessage(), e);
		}
		mIdLatch.countDown();
	}

	public String getAdvertisingId() {
		try {
			mIdLatch.await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mAdvertisingId;
	}

	public Boolean isAdvertisingIdLimitedTrackingEnabled() {
		return mAdvertisingIdLimitedTrackingEnabled;
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
		return Integer.toString(mScreenWidth);
	}
	
	public String getScreenHeight() {
		if (0 == mScreenHeight) {
			mScreenHeight = getDisplayMetrics().heightPixels;
		}
		return Integer.toString(mScreenHeight);
	}
	
	public String getScreenDensityX() {
		if (0 == mScreenDensityX) {
			mScreenDensityX = getDisplayMetrics().xdpi;
		}
		return Integer.toString(Math.round(mScreenDensityX));
	}
	
	public String getScreenDensityY() {
		if (0 == mScreenDensityY) {
			mScreenDensityY = getDisplayMetrics().ydpi;
		}
		return Integer.toString(Math.round(mScreenDensityY));
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
