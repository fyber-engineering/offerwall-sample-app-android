package com.sponsorpay.mediation.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.mediation.YuMeMediationAdapter;
import com.sponsorpay.mediation.interstitial.YuMeInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.yume.android.sdk.YuMeAdParams;
import com.yume.android.sdk.YuMePlayType;
import com.yume.android.sdk.YuMeSDKInterface;
import com.yume.android.sdk.YuMeSDKInterfaceImpl;
import com.yume.android.sdk.YuMeStorageMode;

public class YuMeConfigurationsHelper {
	
	private static final String TAG = "YuMeConfigurationsHelper";
	
	private static final String YUME_INTERFACE = "YuMeSDKInterface";
	private static final String YUME_INTERSTITIAL_ADAPTER = "YuMeInterstitialAdapter";
	
	private static final String DOMAIN_ID = "domainId";
	private static final String AD_SERVER_URL = "adServerUrl";
	
	private static final String AD_PARAMS = "adParams";

	public static void setYuMeSDKInterface(YuMeSDKInterface yumeSDKInterface) {
		Map<String, Object> configs = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(getName());
		configs.put(YUME_INTERFACE, yumeSDKInterface);
	}
	
	public static YuMeSDKInterface getYuMeSDKInterface() {
		return SPMediationConfigurator.getConfiguration(getName(),
				YUME_INTERFACE, YuMeSDKInterfaceImpl.class);
	}
	
	public static void setYuMeInterstitialAdapter(YuMeInterstitialMediationAdapter interstitialAdapter) {
		Map<String, Object> configs = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(getName());
		configs.put(YUME_INTERSTITIAL_ADAPTER, interstitialAdapter);
	}
	
	public static YuMeInterstitialMediationAdapter getYuMeInterstitialAdapter() {
		return SPMediationConfigurator.getConfiguration(getName(),
				YUME_INTERSTITIAL_ADAPTER, YuMeInterstitialMediationAdapter.class);
	}
	
	private static String getName() {
		return YuMeMediationAdapter.ADAPTER_NAME;
	}
	
	public static YuMeAdParams getAdParams() {
		YuMeAdParams adParams = new YuMeAdParams();
		
		//required params
		adParams.adServerUrl = SPMediationConfigurator.getConfiguration(getName(), AD_SERVER_URL, String.class);
		adParams.domainId = SPMediationConfigurator.getConfiguration(getName(), DOMAIN_ID, String.class);
		
		if (StringUtils.nullOrEmpty(adParams.adServerUrl) || 
				StringUtils.nullOrEmpty(adParams.domainId)) {
			return null;
		}
		
		//optional ones
		Map<String, String> settings = getAdParamsSettings();
		if (!settings.isEmpty()) {
			getAdParamsNumericValues(adParams, settings);
			getAdParamsEnumValues(adParams, settings);
			getAdParamsBooleanValues(adParams, settings);
		}
		
		return adParams;
	}
	
	// Helpers
	private static Map<String, String> getAdParamsSettings() {
		JSONObject adParams = SPMediationConfigurator.getConfiguration(getName(), AD_PARAMS, JSONObject.class);
		if(adParams != null) {
			try {
				Map<String, String> map = new HashMap<String, String>(
						adParams.length());
				@SuppressWarnings("unchecked")
				Iterator<String> itr = adParams.keys();
				while (itr.hasNext()) {
					String key = itr.next();
					String value = adParams.getString(key);
					map.put(key, value);
				}
				return map;
			} catch (JSONException e) {
				SponsorPayLogger.d(TAG, "Error parsing json - " + e.getLocalizedMessage() );
			}
		}
		return Collections.emptyMap();
	}
	
	//
	
	private static void getAdParamsNumericValues(YuMeAdParams adParams, Map<String, String> settings) {
		Integer intValue = Integer.getInteger(settings.get("adTimeout"));
		if (intValue != null) {
			adParams.adTimeout = intValue;
		}
		intValue = Integer.getInteger(settings.get("videoTimeout"));
		if (intValue != null) {
			adParams.videoTimeout = intValue;
		}
		try {
			adParams.storageSize = Float.parseFloat(settings.get("storageSize"));
		} catch (Exception e) {
		}
	}
	
	private static void getAdParamsEnumValues(YuMeAdParams adParams, Map<String, String> settings) {
		try {
			adParams.eStorageMode = YuMeStorageMode.valueOf(settings.get("eStorageMode"));
		}catch (Exception e) {
		}
		try {
			adParams.ePlayType = YuMePlayType.valueOf(settings.get("ePlayType"));
		}catch (Exception e) {
		}
	}
	
	private static void getAdParamsBooleanValues(YuMeAdParams adParams, Map<String, String> settings) {
		String[] keys = {"bSupportMP4","bSupport3GPP","bSupportHighBitRate","bSupportAutoNetworkDetect",
				"bEnableCaching","bEnableAutoPrefetch","bEnableCBToggle","bEnableLocationSupport",
				"bEnableFileLogging","bEnableConsoleLogging","bRequireVastAds","bOverrideOrientation",
				"bSupportSurvey"};
		Boolean[] values = {adParams.bSupportMP4, adParams.bSupport3GPP, adParams.bSupportHighBitRate,
				adParams.bSupportAutoNetworkDetect, adParams.bEnableCaching, adParams.bEnableAutoPrefetch, 
				adParams.bEnableCBToggle, adParams.bEnableLocationSupport, adParams.bEnableFileLogging,
				adParams.bEnableConsoleLogging , adParams.bRequireVastAds, adParams.bOverrideOrientation,
				adParams.bSupportSurvey};
		
		for (int i = 0 ; i < keys.length ; i++) {
			String stringBoolean = settings.get(keys[i]);
			if (StringUtils.notNullNorEmpty(stringBoolean)) {
				values[i] = Boolean.parseBoolean(stringBoolean);
			}
		}
	}
}
