/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.user;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.text.TextUtils;

import com.sponsorpay.utils.HostInfo;
import com.sponsorpay.utils.SponsorPayLogger;

public final class SPUser extends HashMap<String, Object> {
	
	private static final String TAG = "SPUser";
	private static final long serialVersionUID = -5963403748409731798L;
		
	private String mDataAsString;
	private boolean isMapDirty = false;
	
	private Set<String> mReservedKeys = new HashSet<String>();
	
	private Location mLocation;
	private Location mLastLocation;
	private Calendar mNextUpdate;
	
	private static final String AGE      = "age";
	private static final String BIRTHDAY = "birthdate";
	private static final String GENDER   = "gender";
	private static final String SEXUAL_ORIENTATION = "sexualOrientation";
	private static final String ETHNICITY = "ethnicity";
	private static final String LAT       = "lat";
	private static final String LONGT     = "longt";
	private static final String MARITAL_STATUS = "maritalStatus";
	private static final String NUMBER_OF_CHILDRENS     = "children";
	private static final String ANNUAL_HOUSEHOLD_INCOME = "annualHouseholdIncome";
	private static final String EDUCATION = "education";
	private static final String ZIPCODE   = "zipcode";
	private static final String POLITICAL_AFFILIATION = "politicalAffiliation";
	private static final String INTERESTS  = "interests";
	private static final String IAP        = "iap";
	private static final String IAP_AMOUNT = "iap_amount";
	private static final String NUMBER_OF_SESSIONS = "numberOfSessions";
	private static final String PS_TIME      = "ps_time";
	private static final String LAST_SESSION = "last_session";
	private static final String CONNECTION   = "connection";
	private static final String DEVICE       = "device";
	private static final String APP_VERSION  = "app_version";

	private static final SPUser singleton = new SPUser();

	private SPUser() {
		mReservedKeys.add(AGE);
		mReservedKeys.add(BIRTHDAY);
		mReservedKeys.add(GENDER);
		mReservedKeys.add(SEXUAL_ORIENTATION);
		mReservedKeys.add(ETHNICITY);
		mReservedKeys.add(LAT);
		mReservedKeys.add(LONGT);
		mReservedKeys.add(MARITAL_STATUS);
		mReservedKeys.add(NUMBER_OF_CHILDRENS);
		mReservedKeys.add(ANNUAL_HOUSEHOLD_INCOME);
		mReservedKeys.add(EDUCATION);
		mReservedKeys.add(ZIPCODE);
		mReservedKeys.add(POLITICAL_AFFILIATION);
		mReservedKeys.add(INTERESTS);
		mReservedKeys.add(IAP);
		mReservedKeys.add(IAP_AMOUNT);
		mReservedKeys.add(NUMBER_OF_SESSIONS);
		mReservedKeys.add(PS_TIME);
		mReservedKeys.add(LAST_SESSION);
		mReservedKeys.add(CONNECTION);
		mReservedKeys.add(DEVICE);
		mReservedKeys.add(APP_VERSION);
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
		return (Location) singleton.mLocation;
	}

	public static void setLocation(Location location) {
		singleton.mLocation = location;
		singleton.setLocationDetails(location);
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
	 * You can set custom values in the collection as long as the key doesn't match any 
	 * of the reserved keywords.
	 */
	public static void addCustomValue(String key, Object value) {
		if (!singleton.mReservedKeys.contains(key)) {
			singleton.put(key, value);
		} else {
			SponsorPayLogger.v(TAG, key + " is a reserved key for this HashMap, please select another name.");
		}
	}
	
	public static Object getCustomValue(String key) {
		return singleton.get(key);
	}
	
	public static String mapToString() {

		if (singleton.isMapDirty) {
			SponsorPayLogger.d(TAG, "SPUser data has changed, recreating...");
			
			singleton.checkAutoLocation();
			
			Uri.Builder builder = new Uri.Builder();

			for (Map.Entry<String, Object> entry : singleton.entrySet()) {
				builder.appendQueryParameter(entry.getKey(), singleton.getStringValue(entry.getValue()));
			}
			
            String providedData = builder.build().toString();
			
			//remove the first question mark
			singleton.mDataAsString = providedData.substring(1,providedData.length());

			SponsorPayLogger.d(TAG, "SPUSer data - " + singleton.mDataAsString);

			singleton.isMapDirty = false;
		}

		return singleton.mDataAsString;
	}

	private void checkAutoLocation() {
		// check if there's an provided location
		LocationManager locationManager = HostInfo.getHostInfo(null).getLocationManager();
		if (mLocation == null && locationManager != null) {
			Calendar now = Calendar.getInstance();
			if (mNextUpdate == null || now.after(mNextUpdate)) {
				// get the latest updated location
				List<String> locationProviders =  HostInfo.getHostInfo(null).getLocationProviders();
				for (String provider : locationProviders) {
					Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
					if (mLastLocation == null) {
						mLastLocation = lastKnownLocation;
					}		
					if (mLastLocation != null && mLastLocation.getTime() < lastKnownLocation.getTime()) {
						mLastLocation = lastKnownLocation;
					}
				}
				if (mLastLocation != null) {
					// finally, discard the location if it's more than a day old
					Calendar yesterday = Calendar.getInstance();
					yesterday.add(Calendar.DATE, -1);
					if (mLastLocation.getTime() > yesterday.getTimeInMillis()) {
						setLocationDetails(mLastLocation);
						mNextUpdate = now;
						mNextUpdate.add(Calendar.MINUTE, 10);
					}
				}
			}
		}
	}

	private String getStringValue(Object value) {
		if (value instanceof Date) {
			return String.format("%tY/%tm/%td", value, value, value);
		} else if (value instanceof String[]) {
			return TextUtils.join(",", (String[])value);
		}
		return value.toString();
	}
	
	@Override
	public Object put(String key, Object value) {		
		
		// The isProvidedMapDirty is used to check when changes are happening on the map
		// in order to avoid to continuous creation  of the String from the Map key/values
		// on the method above (mapToString())
		Object oldValue = get(key);
		isMapDirty = oldValue == null || !oldValue.equals(value);
		return super.put(key, value);
	}
	
	@Override
	public Object remove(Object key) {
		Object removed = super.remove(key);
		isMapDirty = removed != null;
		return removed;
	}
	
	private String formatInDegrees(double value){
		return Location.convert(value, Location.FORMAT_DEGREES);
	}

	private void setLocationDetails(Location location) {
		if (location != null) {
			put(LAT, formatInDegrees(location.getLatitude()));
			put(LONGT, formatInDegrees(location.getLongitude()));
		} else {
			remove(LAT);
			remove(LONGT);
		}
	}

}