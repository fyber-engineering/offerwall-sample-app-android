/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android;

import java.util.HashMap;
import java.util.Set;

import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;

import android.net.Uri;

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
	 * The SDK internal version key for encoding the corresponding URL parameter.
	 */
	private static final String SDK_INTERNAL_VERSION_KEY = "version";

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

	/**
	 * Request signature parameter key.
	 */
	private static final String URL_PARAM_SIGNATURE = "signature";

	public static final String URL_PARAM_ALLOW_CAMPAIGN_KEY = "allow_campaign";
	public static final String URL_PARAM_VALUE_ON = "on";
	public static final String URL_PARAM_OFFSET_KEY = "offset";
	public static final String URL_PARAM_CURRENCY_NAME_KEY = "currency";

	/**
	 * Builds a String URL with information gathered from the device and the specified parameters.
	 * 
	 * @param resourceUrl
	 *            The base for the URL to be built and returned, including schema and host.
	 * @param userId
	 *            The user id parameter to encode in the result URL. It can be left to null and no user ID parameter key
	 *            will be included in the request parameters.
	 * @param hostInfo
	 *            A {@link HostInfo} instance used to retrieve data about the application id and the host
	 *            device.
	 * @param extraKeys
	 *            An array of keys for extra parameters to encode in the result URL.
	 * @param extraValues
	 *            An array of values corresponding to the provided extraKeys.
	 * @return The built URL as a String with the provided parameters encoded.
	 */
	public static String buildUrl(String resourceUrl, String userId, HostInfo hostInfo, String[] extraKeys,
			String[] extraValues) {

		return buildUrl(resourceUrl, userId, hostInfo, extraKeys, extraValues, null);
	}

	/**
	 * Builds a String URL with information gathered from the device and the specified parameters.
	 * 
	 * @param resourceUrl
	 *            The base for the URL to be built and returned, including schema and host.
	 * @param hostInfo
	 *            A {@link HostInfo} instance used to retrieve data about the application id and the host
	 *            device.
	 * @param extraKeys
	 *            An array of keys for extra parameters to encode in the result URL.
	 * @param extraValues
	 *            An array of values corresponding to the provided extraKeys.
	 * @return The built URL as a String with the provided parameters encoded.
	 */
	public static String buildUrl(String resourceUrl, HostInfo hostInfo, String[] extraKeys,
			String[] extraValues) {

		return buildUrl(resourceUrl, null, hostInfo, extraKeys, extraValues, null);
	}
	
	/**
	 * Builds a String URL with information gathered from the device and the specified parameters.
	 * 
	 * @param resourceUrl
	 *            The base for the URL to be built and returned, including schema and host.
	 * @param userId
	 *            The user id parameter to encode in the result URL. It can be left to null and no user ID parameter key
	 *            will be included in the request parameters.
	 * @param hostInfo
	 *            A {@link HostInfo} instance used to retrieve data about the application id and the host
	 *            device.
	 * @param extraKeys
	 *            An array of keys for extra parameters to encode in the result URL.
	 * @param extraValues
	 *            An array of values corresponding to the provided extraKeys.
	 * @param secretKey
	 *            The publisher's secret token which will be used to sign the request. If left to null the request will
	 *            be sent unsigned.
	 * @return The built URL as a String with the provided parameters encoded.
	 */
	public static String buildUrl(String resourceUrl, String userId, HostInfo hostInfo, String[] extraKeys,
			String[] extraValues, String secretKey) {
		HashMap<String, String> keyValueParams = new HashMap<String, String>();

		if (userId != null) {
			keyValueParams.put(USERID_KEY, userId);
		}
		keyValueParams.put(UDID_KEY, hostInfo.getUDID());
		keyValueParams.put(APPID_KEY, String.valueOf(hostInfo.getAppId()));
		keyValueParams.put(OS_VERSION_KEY, hostInfo.getOsVersion());
		keyValueParams.put(PHONE_VERSION_KEY, hostInfo.getPhoneVersion());
		keyValueParams.put(LANGUAGE_KEY, hostInfo.getLanguageSetting());
		keyValueParams.put(SDK_INTERNAL_VERSION_KEY,
				String.format("%d", SponsorPayPublisher.PUBLISHER_SDK_INTERNAL_VERSION));
		keyValueParams.put(SDK_RELEASE_VERSION_KEY, SponsorPay.RELEASE_VERSION_STRING);
		keyValueParams.put(ANDROID_ID_KEY, hostInfo.getAndroidId());
		keyValueParams.put(WIFI_MAC_ADDRESS_KEY, hostInfo.getWifiMacAddress());

		if (extraKeys != null && extraValues != null) {
			int minLength = Math.min(extraKeys.length, extraValues.length);
			for (int i = 0; i < minLength; i++) {
				String key = extraKeys[i];
				String value = extraValues[i];
				
				if (key != null && !key.equals("") && value != null && !value.equals(""))
					keyValueParams.put(key, value);
			}
		}

		Uri uri = Uri.parse(resourceUrl);
		Uri.Builder builder = uri.buildUpon();

		Set<String> keySet = keyValueParams.keySet();

		for (String key : keySet) {
			builder.appendQueryParameter(key, keyValueParams.get(key));
		}

		if (secretKey != null) {
			builder.appendQueryParameter(URL_PARAM_SIGNATURE,
					SignatureTools.generateSignatureForParameters(keyValueParams, secretKey));
		}

		uri = builder.build();

		return uri.toString();
	}
}
