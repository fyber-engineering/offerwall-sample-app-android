/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class SPMediationConfigurator {

	private static final String TAG = "SPMediationConfigurator";
	
	public static SPMediationConfigurator INSTANCE = new SPMediationConfigurator();

	private Map<String, Map<String, Object>> mConfigurations;
	
	private SPMediationConfigurator() {
		mConfigurations = new HashMap<String, Map<String, Object>>();
	}
	
	public Map<String, List<String>> getMediationAdaptors() {
		SponsorPayLogger.d(TAG, "Getting compatible adapters for SDK v" + SponsorPay.RELEASE_VERSION_STRING );
		
		try {
			// //Use http request to get adaptors and versions
			String readFile = readFile();
			if (StringUtils.notNullNorEmpty(readFile)) {

				JSONObject json = new JSONObject(readFile);
//				JSONArray sdks  = json.getJSONArray("sdks");
				
				JSONArray array = json.getJSONArray("adaptors");
				Map<String, List<String>> map = new HashMap<String, List<String>>(array.length());
				
				for (int i = 0 ; i < array.length() ; i++) {
					JSONObject object = array.getJSONObject(i);
					List<String> list = new LinkedList<String>();
					JSONArray versions = object.getJSONArray("versions");
					for (int j = 0 ; j < versions.length() ; j++) {
						list.add(versions.getString(j));
					}
					map.put(object.getString("qualifiedName"), list);
				}
					
				return map;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}
	
	//Helper method to be dropped when changing to server side information 
	private String readFile() throws IOException, URISyntaxException {
		String everything;
//		URL resource = this.getClass().getClassLoader().getResource("adaptors.info");		
//		URL resource = this.getClass().getResource("/adaptors.info");		
//		URL resource = ClassLoader.getSystemResource("/adaptors.info");		
//		URL resource = Thread.currentThread().getContextClassLoader().getResource("adaptors.info");
//		BufferedReader br = new BufferedReader(new FileReader(new File(resource.toURI())));
		InputStream is = getClass().getResourceAsStream("/"+SponsorPay.RELEASE_VERSION_STRING + "-adaptors.info");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line + '\n');
//	            sb.append('\n');
	            line = br.readLine();
	        }
	        everything = sb.toString();
	    } finally {
	        br.close();
	    }
	    return everything;
	}
	
	public Map<String, Object> getConfigurationForAdaptor(String adaptor) {
		return mConfigurations.get(adaptor);
	}
	
	public boolean setConfigurationForAdaptor(String adaptor, Map<String, Object> configurations) {
		return mConfigurations.put(adaptor, configurations) != null;
	}
	
}
