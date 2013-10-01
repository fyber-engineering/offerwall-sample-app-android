/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.net.Uri;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.credentials.SPCredentials;

/**
 * <p>
 * Contains utility methods to build URLs used to access the SponsorPay's back-end API.
 * </p>
 */
public class UrlBuilder {
	/**
	 * The unique device ID (for url-encoding).
	 */
	private static final String UDID_KEY = "device_id";

	/**
	 * The user id key for encoding the corresponding URL parameter.
	 */
	private static final String USERID_KEY = "uid";

	/**
	 * The App ID key for encoding the corresponding URL parameter.
	 */
	private static final String APPID_KEY = "appid";

	/**
	 * The OS version key for encoding the corresponding URL parameter.
	 */
	private static final String OS_VERSION_KEY = "os_version";

	/**
	 * The phone model key for encoding the corresponding URL parameter.
	 */
	private static final String PHONE_VERSION_KEY = "phone_version";

	/**
	 * The default language setting key for encoding the corresponding URL parameter.
	 */
	private static final String LANGUAGE_KEY = "language";

	/**
	 * The SDK release version key for encoding the corresponding URL parameter.
	 */
	private static final String SDK_RELEASE_VERSION_KEY = "sdk_version";

	/**
	 * The Android ID key for encoding the corresponding URL parameter.
	 */
	private static final String ANDROID_ID_KEY = "android_id";

	/**
	 * The WiFi MAC Address ID key for encoding the corresponding URL parameter.
	 */
	private static final String WIFI_MAC_ADDRESS_KEY = "mac_address";

	private static final String SCREEN_WIDTH_KEY = "screen_width";

	private static final String SCREEN_HEIGHT_KEY = "screen_height";

	private static final String SCREEN_DENSITY_X_KEY = "screen_density_x";

	private static final String SCREEN_DENSITY_Y_KEY = "screen_density_y";

	private static final String SCREEN_DENSITY_CATEGORY_KEY = "screen_density_category";
	
	private static final String CARRIER_COUNTRY_KEY = "carrier_country";
	
	private static final String CARRIER_NAME_KEY = "carrier_name";
	
	private static final String NETWORK_CONNECTION_TYPE_KEY = "network_connection_type";

	private static final String MANUFACTURER_KEY = "manufacturer";
	
	private static final String APP_BUNDLE_NAME_KEY = "app_bundle_name";

	private static final String APP_VERSION_KEY = "app_version";
	
	private static final String CURRENCY_KEY = "currency";

	/**
	 * Request signature parameter key.
	 */
	private static final String URL_PARAM_SIGNATURE = "signature";

	public static final String URL_PARAM_ALLOW_CAMPAIGN_KEY = "allow_campaign";
	public static final String URL_PARAM_VALUE_ON = "on";
	public static final String URL_PARAM_OFFSET_KEY = "offset";
	public static final String URL_PARAM_CURRENCY_NAME_KEY = "currency";



	/**
	 * Checks that the passed Map of key/value parameters doesn't contain empty or null keys or
	 * values. If it does, triggers an {@link IllegalArgumentException}.
	 * 
	 * @param kvParams
	 */
	public static void validateKeyValueParams(Map<String, String> kvParams) {
		if (kvParams != null) {
			for (Entry<String, String> entry : kvParams.entrySet()) {
				if (StringUtils.nullOrEmpty(entry.getKey())
						|| StringUtils.nullOrEmpty(entry.getValue())) {
					throw new IllegalArgumentException(
							"SponsorPay SDK: Custom Parameters cannot have an empty or null"
									+ " Key or Value.");
				}
			}
		}
	}

	/**
	 * Constructs a Map of key / value parameters given an array of keys and an array of values. If
	 * any of the arrays contains empty of null values, an {@link IllegalArgumentException} will be
	 * triggered.
	 * 
	 * @param keys
	 *            An array of keys
	 * @param values
	 *            an array of values in the same order than the provided array of keys
	 * 
	 * @return a Map of keys / values
	 */
	public static Map<String, String> mapKeysToValues(String[] keys, String[] values) {
		if (keys.length != values.length) {
			throw new IllegalArgumentException("SponsorPay SDK: When specifying Custom Parameters"
					+ " using two arrays of Keys and Values, both must have the same length.");
		}
		HashMap<String, String> retval = new HashMap<String, String>(keys.length);

		for (int i = 0; i < keys.length; i++) {
			String k = keys[i];
			String v = values[i];

			if (StringUtils.nullOrEmpty(k) || StringUtils.nullOrEmpty(v)) {
				throw new IllegalArgumentException("SponsorPay SDK: When specifying Custom"
						+ " Parameters using two arrays of Keys and Values, none of their"
						+ " elements can be empty or null.");
			}

			retval.put(k, v);
		}

		return retval;
	}

	private String mResourceUrl;

	private Map<String, String> mExtraKeysValues;

	private boolean mShouldAddScreenMetrics = false;

	private SPCredentials mCredentials;

	private boolean mShouldAddUserId = true;

	private String mCurrency;
	
	protected UrlBuilder(String resourceUrl, SPCredentials credentials) {
		mResourceUrl = resourceUrl;
		mCredentials = credentials;
	}

	public UrlBuilder addExtraKeysValues(Map<String, String> extraKeysValues) {
		if (extraKeysValues != null) {
			if (mExtraKeysValues == null) {
				mExtraKeysValues = new HashMap<String, String>(extraKeysValues);
			} else {
				mExtraKeysValues.putAll(extraKeysValues);
			}
		}
		return this;
	}

	public UrlBuilder addScreenMetrics() {
		mShouldAddScreenMetrics = true;
		return this;
	}
	
	public UrlBuilder sendUserId(boolean shouldSend) {
		mShouldAddUserId = shouldSend;
		return this;
	}
	
	public UrlBuilder setCurrency(String currency) {
		mCurrency = currency;
		return this;
	}

	/**
	 * <p>
	 * This will build the final URL with all the provided parameters
	 * </p>
	 * 
	 * @return the generated URL
	 */
	public String buildUrl() {
		HashMap<String, String> keyValueParams = new HashMap<String, String>();

		if (mShouldAddUserId) {
			keyValueParams.put(USERID_KEY, mCredentials.getUserId());
		}

		HostInfo hostInfo = mCredentials.getHostInfo();

		keyValueParams.put(SDK_RELEASE_VERSION_KEY, SponsorPay.RELEASE_VERSION_STRING);
		keyValueParams.put(APPID_KEY, mCredentials.getAppId());
		keyValueParams.put(UDID_KEY, hostInfo.getUDID());

		keyValueParams.put(OS_VERSION_KEY, hostInfo.getOsVersion());
		keyValueParams.put(PHONE_VERSION_KEY, hostInfo.getPhoneVersion());
		keyValueParams.put(LANGUAGE_KEY, hostInfo.getLanguageSetting());
		keyValueParams.put(ANDROID_ID_KEY, hostInfo.getAndroidId());
		keyValueParams.put(WIFI_MAC_ADDRESS_KEY, hostInfo.getWifiMacAddress());
		
		keyValueParams.put(CARRIER_NAME_KEY, hostInfo.getCarrierName());
		keyValueParams.put(CARRIER_COUNTRY_KEY, hostInfo.getCarrierCountry());
		keyValueParams.put(NETWORK_CONNECTION_TYPE_KEY, hostInfo.getConnectionType());
		keyValueParams.put(MANUFACTURER_KEY, hostInfo.getManufacturer());
		

		keyValueParams.put(APP_BUNDLE_NAME_KEY, hostInfo.getAppBundleName());
		keyValueParams.put(APP_VERSION_KEY, hostInfo.getAppVersion());

		if (StringUtils.notNullNorEmpty(mCurrency)) {
			keyValueParams.put(CURRENCY_KEY, mCurrency);
		}
		
		if (mShouldAddScreenMetrics) {
			keyValueParams.put(SCREEN_WIDTH_KEY, hostInfo.getScreenWidth());
			keyValueParams.put(SCREEN_HEIGHT_KEY, hostInfo.getScreenHeight());
			keyValueParams.put(SCREEN_DENSITY_X_KEY, hostInfo.getScreenDensityX());
			keyValueParams.put(SCREEN_DENSITY_Y_KEY, hostInfo.getScreenDensityY());
			keyValueParams.put(SCREEN_DENSITY_CATEGORY_KEY, hostInfo.getScreenDensityCategory());
		}

		if (mExtraKeysValues != null) {
			validateKeyValueParams(mExtraKeysValues);
			keyValueParams.putAll(mExtraKeysValues);
		}
		
		Map<String, String> spExtraParams = SponsorPayParametersProvider.getParameters();
		if (!spExtraParams.isEmpty()) {
			keyValueParams.putAll(spExtraParams);
		}

		Uri uri = Uri.parse(mResourceUrl);
		Uri.Builder builder = uri.buildUpon();

		for (Entry<String, String> entry : keyValueParams.entrySet()) {
			builder.appendQueryParameter(entry.getKey(), entry.getValue());
		}

		String secretKey = mCredentials.getSecurityToken();
		if (StringUtils.notNullNorEmpty(secretKey)) {
			builder.appendQueryParameter(URL_PARAM_SIGNATURE, SignatureTools
					.generateSignatureForParameters(keyValueParams, secretKey));
		}

		uri = builder.build();

		return uri.toString();
	}
	
	/**
	 * <p>
	 * Used to retrieved a new {@link UrlBuilder}.
	 * </p> 
	 * 
	 * @param resourceUrl
	 * 		  The base URL for this builder.
	 * @param credentials
	 * 		  The {@link SPCredentials} holding the values (userId, appId and secret key) 
	 * 		  to be used within this builder.
	 * 		  
	 * @return a new {@link UrlBuilder}
	 */
	public static UrlBuilder newBuilder(String resourceUrl, SPCredentials credentials) {
		return new UrlBuilder(resourceUrl, credentials);
	}
}
