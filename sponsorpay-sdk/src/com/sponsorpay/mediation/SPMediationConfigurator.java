/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

public class SPMediationConfigurator {

	private static final String TAG = "SPMediationConfigurator";

	
	public static SPMediationConfigurator INSTANCE = new SPMediationConfigurator();
	
	private Map<String, Map<String, Object>> mConfigurations;
	
	private SPMediationConfigurator() {
		mConfigurations = new HashMap<String, Map<String, Object>>();

		SponsorPayLogger.d(TAG, "Reading config file");
		String jsonString = SPMediationConfigurationFiles.getAdaptersConfig();
		if (StringUtils.notNullNorEmpty(jsonString)) {
			try {
				SponsorPayLogger.d(TAG, "Parsing configurations");
				JSONObject json = new JSONObject(jsonString);
				JSONArray configs = json.getJSONArray("adapters");
				for (int i = 0; i < configs.length(); i++) {
					JSONObject config = configs.getJSONObject(i);
					String adapterName = config.getString("name").toLowerCase();
					if (config.has("settings")) {
						JSONObject settings = config.getJSONObject("settings");
						Map<String, Object> map = new HashMap<String, Object>(
								settings.length());
						@SuppressWarnings("unchecked")
						Iterator<String> itr = settings.keys();
						while (itr.hasNext()) {
							String key = itr.next();
							Object value = settings.get(key);
							map.put(key, value);
						}
						mConfigurations.put(adapterName, map);
					} else {
						Map<String, Object> map = Collections.emptyMap();
						mConfigurations.put(adapterName, map);
					}
				}
				SponsorPayLogger.d(TAG, "adapters.config file successfully loaded");
			} catch (JSONException e) {
				SponsorPayLogger.e(TAG, "A JSON error occurred while parsing the adapters.config file,"
						+ " there will be no mediation configurations.", e);
			}
		} else {
			SponsorPayLogger.e(TAG, "The adapters.config file was not found, there will be no mediation configurations.");
		} 
		
	}
	
	public Map<String, List<String>> getMediationAdapters() {
		SponsorPayLogger.d(TAG, "Getting compatible adapters for SDK v" + SponsorPay.RELEASE_VERSION_STRING );
		
		String jsonString = SPMediationConfigurationFiles.getAdapterInfo();
		if (StringUtils.notNullNorEmpty(jsonString)) {
			try {
				JSONObject json = new JSONObject(jsonString);

				JSONArray array = json.getJSONArray("adapters");
				Map<String, List<String>> map = new HashMap<String, List<String>>(
						array.length());

				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					List<String> list = new LinkedList<String>();
					JSONArray versions = object.getJSONArray("versions");
					for (int j = 0; j < versions.length(); j++) {
						list.add(versions.getString(j));
					}
					map.put(object.getString("qualifiedName"), list);
				}

				SponsorPayLogger.d(TAG, "adapters.info file successfully loaded");
				return map;
			} catch (JSONException e) {
				SponsorPayLogger.e(TAG, "An JSON error occured while parsing the adapters.info file,"
						+ " no mediation adapters will be loaded.", e);
			}
		} else {
			SponsorPayLogger.e(TAG, "The adapters.info file was not found, no adapters will be loaded.");
		}
		return Collections.emptyMap();
	}
	
	public Map<String, Object> getConfigurationForAdapter(String adapter) {
		return mConfigurations.get(adapter.toLowerCase());
	}
	
	public boolean setConfigurationForAdapter(String adapter, Map<String, Object> configurations) {
		return mConfigurations.put(adapter.toLowerCase(), configurations) != null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T getConfiguration(String adapter, String key, Class<T> clasz) {
		Map<String, Object> configs = INSTANCE.getConfigurationForAdapter(adapter);
		if (configs != null && !configs.isEmpty()) {
			Object retValue = configs.get(key);
			if (retValue != null && retValue.getClass().isAssignableFrom(clasz)) {
				return (T) retValue;
			}
		}
		return null;
	}
	
	public static <T extends Object> T getConfiguration(String adapter,
			String key, T defaultValue, Class<T> clasz) {
		T config = getConfiguration(adapter, key, clasz);
		return config == null ? defaultValue : config;
	}
	
}
