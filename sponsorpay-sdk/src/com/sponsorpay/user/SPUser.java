/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sponsorpay.utils.SponsorPayLogger;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

public final class SPUser extends HashMap<String, Object>  implements  LocationListener{
	
	private static final long serialVersionUID = -5963403748409731798L;
	
	private static final String CLASS_NAME = "SPUser";
		
	private String providedDataAsString;
	private boolean isProvidedMapDirty = false;
	
    private Context applicationContext;
	
	private LocationManager locationManager;
	
	private ArrayList<String> reservedKeys = new ArrayList<String>();
	
	public static final String AGE      = "age";
	public static final String BIRTHDAY = "birthdate";
	public static final String GENDER   = "gender";
	public static final String SEXUAL_ORIENTATION = "sexualOrientation";
	public static final String ETHNICITY = "ethnicity";
	public static final String LOCATION  = "location";
	public static final String LAT       = "lat";
	public static final String LONGT     = "longt";
	public static final String MARITAL_STATUS = "maritalStatus";
	public static final String NUMBER_OF_CHILDRENS     = "children";
	public static final String ANNUAL_HOUSEHOLD_INCOME = "annualHouseholdIncome";
	public static final String EDUCATION = "education";
	public static final String ZIPCODE   = "zipcode";
	public static final String POLITICAL_AFFILIATION = "politicalAffiliation";
	public static final String INTERESTS  = "interests";
	public static final String IAP        = "iap";
	public static final String IAP_AMOUNT = "iap_amount";
	public static final String NUMBER_OF_SESSIONS = "numberOfSessions";
	public static final String PS_TIME      = "ps_time";
	public static final String LAST_SESSION = "last_session";
	public static final String CONNECTION   = "connection";
	public static final String DEVICE       = "device";
	public static final String APP_VERSION  = "app_version";
	


	private static final SPUser singleton = new SPUser();

	private SPUser() {
		setReservedKeys();
	}
	
	// Make sure you only call this once - from the class that is providing the SPUser values.
    public void init(final Context context) {
    	
    	applicationContext = context.getApplicationContext();
    	
    	if (applicationContext == null) {
            
    		throw new IllegalStateException("The parameter context in the init(context), must not be null.");
            
        }else{
        	
        	// Acquire a reference to the system Location Manager
        	locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        	
        	requestUpdatesFromAvailableProvider();     
        }
    }

	public static Integer getAge() {
		return  (Integer) singleton.get(AGE);
	}


	public static void setAge(Integer age) {
		singleton.put(AGE, age);
	}


	public Date getBirthdate() {
		return (Date) singleton.get(BIRTHDAY);
	}


	public static void setBirthdate(Date birthdate) {
		singleton.put(BIRTHDAY, birthdate);
	}


	public static SPUserGender getGender() {
		return (SPUserGender) singleton.get(GENDER);
	}


	public static void setGender(SPUserGender gender) {
		singleton.put(GENDER, gender);
	}


	public static SPUserSexualOrientation getSexualOrientation() {
		return (SPUserSexualOrientation) singleton.get(SEXUAL_ORIENTATION);
	}


	public static void setSexualOrientation(SPUserSexualOrientation sexualOrientation) {
		singleton.put(SEXUAL_ORIENTATION, sexualOrientation);
	}


	public static SPUserEthnicity getEthnicity() {
		return (SPUserEthnicity) singleton.get(ETHNICITY);
	}


	public static void setEthnicity(SPUserEthnicity ethnicity) {
		singleton.put(ETHNICITY, ethnicity);
	}


	public static Location getLocation() {
		return (Location) singleton.get(LOCATION);
	}


	public static void setLocation(Location location) {
		singleton.put(LOCATION, location);
	}


	public static Float getLat() {
		return (Float) singleton.get(LAT);
	}


	public static void setLat(Float lat) {
		singleton.put(LAT, lat);
	}


	public static Float getLongt() {
		return (Float) singleton.get(LONGT);
	}


	public static void setLongt(Float longt) {
		singleton.put(LONGT, longt);
	}


	public static SPUserMaritalStatus getMaritalStatus() {
		return (SPUserMaritalStatus) singleton.get(MARITAL_STATUS);
	}


	public static void setMaritalStatus(SPUserMaritalStatus maritalStatus) {
		singleton.put(MARITAL_STATUS, maritalStatus);
	}	
	
	public static Integer getNumberOfChildrens(){
		return (Integer) singleton.get(NUMBER_OF_CHILDRENS);
	}

	public static void setNumberOfChildrens(Integer numberOfChildrens) {
		singleton.put(NUMBER_OF_CHILDRENS, numberOfChildrens);
	}

	public static Integer getAnnualHouseholdIncome() {
		return (Integer) singleton.get(ANNUAL_HOUSEHOLD_INCOME);
	}


	public static void setAnnualHouseholdIncome(Integer annualHouseholdIncome) {
		singleton.put(ANNUAL_HOUSEHOLD_INCOME, annualHouseholdIncome);
	}


	public static SPUserEducation getEducation() {
		return (SPUserEducation) singleton.get(EDUCATION);
	}


	public static void setEducation(SPUserEducation education) {
		singleton.put(EDUCATION, education);
	}


	public static String getZipcode() {
		return (String) singleton.get(ZIPCODE);
	}


	public static void setZipcode(String zipcode) {
		singleton.put(ZIPCODE, zipcode);
	}


	public static String getPoliticalAffiliation() {
		return (String) singleton.get(POLITICAL_AFFILIATION);
	}


	public static void setPoliticalAffiliation(String politicalAffiliation) {
		singleton.put(POLITICAL_AFFILIATION, politicalAffiliation);
	}


	public static String[] getInterests() {
		return (String[]) singleton.get(INTERESTS);
	}


	public static void setInterests(String[] interests) {
		singleton.put(INTERESTS, interests);
	}


	public static Boolean getIap() {
		return (Boolean) singleton.get(IAP);
	}


	public static void setIap(Boolean iap) {
		singleton.put(IAP, iap);
	}


	public static Float getIapAmount() {
		return (Float) singleton.get(IAP_AMOUNT);
	}


	public static void setIapAmount(Float iap_amount) {
		singleton.put(IAP_AMOUNT, iap_amount);
	}


	public static Integer getNumberOfSessions() {
		return (Integer) singleton.get(NUMBER_OF_SESSIONS);
	}


	public static void setNumberOfSessions(Integer numberOfSessions) {
		singleton.put(NUMBER_OF_SESSIONS, numberOfSessions);
	}


	public static Long getPsTime() {
		return (Long) singleton.get(PS_TIME);
	}


	public static void setPsTime(Long ps_time) {
		singleton.put(PS_TIME, ps_time);
	}


	public static Long getLastSession() {
		return (Long) singleton.get(LAST_SESSION);
	}


	public static void setLastSession(Long last_session) {
		singleton.put(LAST_SESSION, last_session);
	}


	public static SPUserConnection getConnection() {
		return (SPUserConnection) singleton.get(CONNECTION);
	}

	public static void setConnection(SPUserConnection connection) {
		singleton.put(CONNECTION, connection);
	}


	public static String getDevice() {
		return (String) singleton.get(DEVICE);
	}


	public static void setDevice(String device) {
		singleton.put(DEVICE, device);
	}


	public static String getAppVersion() {
		return (String) singleton.get(APP_VERSION);
	}


	public static void setAppVersion(String app_version) {
		singleton.put(APP_VERSION, app_version);
	}
	
	
	/**
	 * You can set custom values in the collection
	 * as long as the key doesn't match with an 
	 * existing one and isn't the same with the
	 * existed reserved keywords.
	 */
	public static void addCustomValue(String key, Object value) {
		if (!singleton.reservedKeys.contains(key)) {
			singleton.put(key, value);
		}else{
			SponsorPayLogger.v(CLASS_NAME, key + " is a reserved key for this HashMap, please select another name.");
		}
	}
	
	private ArrayList<String> setReservedKeys(){
		reservedKeys.add(AGE);
		reservedKeys.add(BIRTHDAY);
		reservedKeys.add(GENDER);
		reservedKeys.add(SEXUAL_ORIENTATION);
		reservedKeys.add(ETHNICITY);
		reservedKeys.add(LOCATION);
		reservedKeys.add(LAT);
		reservedKeys.add(LONGT);
		reservedKeys.add(MARITAL_STATUS);
		reservedKeys.add(NUMBER_OF_CHILDRENS);
		reservedKeys.add(ANNUAL_HOUSEHOLD_INCOME);
		reservedKeys.add(EDUCATION);
		reservedKeys.add(ZIPCODE);
		reservedKeys.add(POLITICAL_AFFILIATION);
		reservedKeys.add(INTERESTS);
		reservedKeys.add(IAP);
		reservedKeys.add(IAP_AMOUNT);
		reservedKeys.add(NUMBER_OF_SESSIONS);
		reservedKeys.add(PS_TIME);
		reservedKeys.add(LAST_SESSION);
		reservedKeys.add(CONNECTION);
		reservedKeys.add(DEVICE);
		reservedKeys.add(APP_VERSION);
		
		return reservedKeys;
	}
	
	public static String mapToString() {

		if (singleton.isProvidedMapDirty) {
			
			Uri.Builder builder = new Uri.Builder();

			for (Map.Entry<String, Object> entry : singleton.entrySet()) {

				builder.appendQueryParameter(entry.getKey(), singleton.getStringValue(entry.getValue()));
			}
			
            String providedData = builder.build().toString();
			
			//remove the first question mark
			singleton.providedDataAsString = providedData.substring(1,providedData.length());

			SponsorPayLogger.v("SPUser submitted data", singleton.providedDataAsString);

			singleton.isProvidedMapDirty = false;
		}

		return singleton.providedDataAsString;
	}

	private String getStringValue(Object value) {
		if (value instanceof Date) {
			return String.format("%tY/%tm/%td", value, value, value);
		} else if (value instanceof String[]) {
			return TextUtils.join(",", (String[])value);
		} else if (value instanceof Location){
			Location location = (Location) value;
			return "latitude:"+ formatInDegrees(location.getLatitude())+ ",longitude:"+ formatInDegrees(location.getLongitude())
					+ ",accuracy:"+location.getAccuracy();
		}
		return value.toString();
	}
	
	@Override
	public Object put(String key, Object value) {		
		
		// The isProvidedMapDirty is used to check
		// when changes are happening on the map
		// in order to avoid to continuous creation
		// of the String from the Map key/values
		// on the method above (mapToString())
		isProvidedMapDirty = true;
		
		return super.put(key, value);
	}
	
	private String formatInDegrees(double value){
		return Location.convert(value, Location.FORMAT_DEGREES);
	}

	@Override
	public void onLocationChanged(Location location) {
		setLocationDetails(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Remove the listener you previously added
		locationManager.removeUpdates(this);
	}

	@Override
	public void onProviderEnabled(String provider) {
		LocationManager locManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);         

		SponsorPayLogger.d(CLASS_NAME, "onProviderEnabled() User has enabled the provider.");
		
		Location location = null;
		
		if(doesUserHaveCoarseLocationPermission()){
			location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
		}else if(doesUserHaveFineLocationPermission()){
			
			location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
		}
		    
		setLocationDetails(location);	
	}
	

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			// Remove the listener you previously added
			locationManager.removeUpdates(this);
			break;

		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			// Do nothing
			break;

		case LocationProvider.AVAILABLE:
			//Re-instantiate the available provider
			requestUpdatesFromAvailableProvider();
			break;
		}

	}
	
    
    private boolean doesUserHaveFineLocationPermission(){
		//set a static boolean to avoid the continuous check
		int result = applicationContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
	    return result == PackageManager.PERMISSION_GRANTED;
		
	}
	
    private boolean doesUserHaveCoarseLocationPermission(){
		 
		 int result = applicationContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
		 return result == PackageManager.PERMISSION_GRANTED;

	}
    
    private void requestUpdatesFromAvailableProvider(){
    	
    	if(doesUserHaveFineLocationPermission()){
    		
    		// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

		}else if(doesUserHaveCoarseLocationPermission()){
			
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
    }
    

    private void setLocationDetails(Location location){
    	
    	if (location != null) {

			setLocation(location);
			
			setLat((float) location.getLatitude());
			
			setLongt((float) location.getLongitude());
			
		}
    }

}