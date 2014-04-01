/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.interstitial;

import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.InterstitialAd;
import com.sponsorpay.mediation.AdMobMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;

public class AdMobIntersitialMediationAdapter extends SPInterstitialMediationAdapter<AdMobMediationAdapter> {

	private static final String TAG = "AdMobIntersitialMediationAdapter";

	/**
	 * The following list of Strings are declared in the config file and are used
	 * to set manual settings for the request adapter.
	 */
	private static final String BUILDER_CONFIG_ADD_TEST_DEVICE = "addTestDevice";
	private static final String COOPA_COPLIANT                 = "isCOPPAcompliant";
		
	/**
	 * The following keys are declared in the config file and are used
	 * to set the runtime settings for the request adapter.
	 */
	private static final String BIRTHDAY_KEY   = "birthday";
	private static final String GENDER_KEY     = "gender";
	private static final String LOCATION_KEY   = "location";
	
	/**
	 * These are the 4 possible errors that we can take back when we are 
	 * making a request for ads.
	 **/
	private static final String INTERNAL_ERROR  = "ERROR_CODE_INTERNAL_ERROR";
	private static final String INVALID_REQUEST = "ERROR_CODE_INVALID_REQUEST";
	private static final String NETWORK_ERROR   = "ERROR_CODE_NETWORK_ERROR";
	private static final String CODE_NO_FILL    = "ERROR_CODE_NO_FILL";
	
	private static Integer gender;
	private static Date date;
	private static Location location;
	
	
	private InterstitialAd interstitial;

	public AdMobIntersitialMediationAdapter(AdMobMediationAdapter adapter, Activity activity) {
		super(adapter);
		
		// Create the interstitial.
	    interstitial = new InterstitialAd(activity);
	    
	    // Our ad unit id. Could be replaced with our actual ad unit id (could be either Home or Level up). 
	    String adUnitId = mAdapter.addUnitId();
	    interstitial.setAdUnitId(adUnitId);
	    
	    
	    AdListener adlistener = new AdListener() {

	    	/**
	    	 * Called when the user is about to return to the application after clicking on an ad.
	    	 */
			@Override
			public void onAdClosed() {
				super.onAdClosed();
				SponsorPayLogger.i(TAG, "Ad closed.");
				
				fireCloseEvent();
			}

			/**
			 * Called when an ad request failed. The error code is usually ERROR_CODE_INTERNAL_ERROR(=0), 
			 * ERROR_CODE_INVALID_REQUEST(=1), ERROR_CODE_NETWORK_ERROR(=2), or ERROR_CODE_NO_FILL(=3).
			 */
			@Override
			public void onAdFailedToLoad(int errorCode) {
				super.onAdFailedToLoad(errorCode);
				
				switch(errorCode) {
				  case 0:			  
					  fireValidationErrorEvent(INTERNAL_ERROR);
					  SponsorPayLogger.i(TAG, "Ad request failed due to internal error.");
					  break;			  
				  case 1:
					  fireValidationErrorEvent(INVALID_REQUEST);
					  SponsorPayLogger.i(TAG, "Ad request failed due to invalid request.");
					  break;				  
				  case 2:
					  fireValidationErrorEvent(NETWORK_ERROR);  
					  SponsorPayLogger.i(TAG, "Ad request failed due to network error.");
					  break;			  
				  case 3:
					  fireValidationErrorEvent(CODE_NO_FILL);  
					  SponsorPayLogger.i(TAG, "Ad request failed due to code not filled error.");
					  break;
				}
				
			}

			/**
			 * Called when an ad leaves the application (e.g., to go to the browser).
			 */
			@Override
			public void onAdLeftApplication() {
				super.onAdLeftApplication();
				
				fireClickEvent();
				SponsorPayLogger.i(TAG, "User leaves the application. Clicked on the ad.");
			}

			/**
			 * Called when an ad is received.
			 */
			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				setAdAvailable();
				SponsorPayLogger.i(TAG, "Ad received.");
			}

			/**
			 * Called when an ad opens an overlay that covers the screen.
			 */
			@Override
			public void onAdOpened() {
				super.onAdOpened();
				
				fireImpressionEvent();
				SponsorPayLogger.i(TAG, "Ad opened.");
			}	
	    	
		};
		
		// Set the AdListener.
		interstitial.setAdListener(adlistener);
		loadInterstitial();
	}
	
	private void loadInterstitial() {
		
		// Create a builder object.
		Builder requestBuilder = null;
		
		
		// Get the list of the test device ids.
		JSONArray metadata = getConfigListOfDevices();
		
		
		//  Set the emulator as a default test device
		requestBuilder = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
		
		
		// Set the ids of all testing devices that have been declared in the config file.
		if (metadata != null) {
			if (metadata.length() > 0) {
				for (int i = 0; i < metadata.length(); i++) {
					String deviceId;
					try {
						deviceId = metadata.getJSONObject(i).toString();
						requestBuilder.addTestDevice(deviceId);
					} catch (JSONException jsonexc) {
						SponsorPayLogger.e(TAG, "Error on parsing device id.");
					}
				}
			}
		}
		
		
		//set all the runtime configuration for gender, location and date
		//which will be used for ad targeting
		
		Map<String, Object> runtimeConfigurationForAdapter = getRuntimeMetadata();
		if (!runtimeConfigurationForAdapter.isEmpty()) {
			if (runtimeConfigurationForAdapter.containsKey(GENDER_KEY)) {

				requestBuilder.setGender((Integer) runtimeConfigurationForAdapter.get(GENDER_KEY));
			}

			if (runtimeConfigurationForAdapter.containsKey(BIRTHDAY_KEY)) {

				requestBuilder.setBirthday((Date) runtimeConfigurationForAdapter.get(BIRTHDAY_KEY));
			}

			if (runtimeConfigurationForAdapter.containsKey(LOCATION_KEY)) {

				requestBuilder.setLocation((Location) runtimeConfigurationForAdapter.get(LOCATION_KEY));
			}
		}
		
		//if the developer explicitly set that is COOPA compliant in the config file 
		//then we set it as true otherwise we don't set it and it will be false by default;
		if(isCOPPAcompliant()){
			requestBuilder.tagForChildDirectedTreatment(true);
		}
		
		// Finally we build the requst
		AdRequest adRequest =  requestBuilder.build();
		
		// and load the interstitial ad.
		interstitial.loadAd(adRequest);
		
		SponsorPayLogger.i(TAG, "Loading the add.");
	}

	@Override
	protected boolean show(Activity parentActivity) {
		//show the add
		interstitial.show();
		return true;
	}

	@Override
	protected void checkForAds(Context context) {

	}
	
	/**
	 * @return the list of the test devices
	 */
	private JSONArray getConfigListOfDevices() {
		JSONArray metadata = SPMediationConfigurator.getConfiguration(getName(), BUILDER_CONFIG_ADD_TEST_DEVICE, JSONArray.class);
		
		return metadata;
	}
	
	/**
	 * @return true- if it's COOPA compliant otherwise false.
	 */
	private boolean isCOPPAcompliant(){
		String coppaCompliant = SPMediationConfigurator.getConfiguration(getName(), COOPA_COPLIANT, String.class);
		
		return  Boolean.parseBoolean(coppaCompliant);
	}


	private Map<String, Object> getRuntimeMetadata(){
		Map<String, Object> configurationForAdapter = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter("AdMob");
		
		if(getGender() != null){
			configurationForAdapter.put(GENDER_KEY, getGender());
		}
		
		if(getDate() != null){
			configurationForAdapter.put(BIRTHDAY_KEY, getDate());
		}
		
		if(getLocation() != null){
			configurationForAdapter.put(LOCATION_KEY, getLocation());
		}
		
		return configurationForAdapter;
	}

	public static Integer getGender() {
		return gender;
	}

	/**
	 * 
	 * @param gender - use the static fields of the AdRequest class
	 *                 GENDER_FEMALE(=2), GENDER_MALE(=1), GENDER_UNKNOWN(=0)
	 * @return
	 */
	public static void setGender(Integer gender) {
		AdMobIntersitialMediationAdapter.gender = gender;
	}

	public static Date getDate() {
		return date;
	}

	/**
	 * @param date - the selected date
	 * @return
	 */
	public static void setDate(Date date) {
		AdMobIntersitialMediationAdapter.date = date;
	}

	/**
	 * @param location - the selected location
	 * @return
	 */
	public static Location getLocation() {
		return location;
	}

	public static void setLocation(Location location) {
		AdMobIntersitialMediationAdapter.location = location;
	}
}