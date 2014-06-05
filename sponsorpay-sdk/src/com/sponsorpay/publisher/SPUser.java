package com.sponsorpay.publisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.location.Location;

public final class SPUser extends HashMap<String, Object>  {
	
	private static final long serialVersionUID = -5963403748409731798L;


	private static final class SingletonHolder {
		static final SPUser singleton = new SPUser();
	}

	private SPUser() {
	}

//	public static SPUser getInstance() {
//		return SingletonHolder.singleton;
//	}
     
	public enum SPUserGender{
		male,
		female,
		other
	}
	
	public enum SPUserSexualOrientation{
		straight,
		bisexual,
		gay,
		unknown
	}
	
	public enum SPUserEthnicity{
		asian,
		black,
		hispanic,
		indian,
		middle_eastern,	
		native_american,	
		pacific_islander,	
		white,	
		other
	}
	
	public enum SPUserEducation{
		other,	
		none,	
		high_school,	
		in_college,
		some_college,	
		associates,	
		bachelors,	
		masters,	
		doctorate
	}
	
	public enum SPUserConnection{
		wifi,	
		three_g
	}
	


	public static Integer getAge() {
		return  (Integer) SingletonHolder.singleton.get("age");
	}


	public void setAge(Integer age) {
		put("age", age);
	}


	public Date getBirthdate() {
		return (Date) SingletonHolder.singleton.get("birthdate");
	}


	public void setBirthdate(Date birthdate) {
		put("birthdate", birthdate);
	}


	public static SPUserGender getGender() {
		return (SPUserGender) SingletonHolder.singleton.get("gender");
	}


	public void setGender(SPUserGender gender) {
		put("gender", gender);
	}


	public static SPUserSexualOrientation getSexualOrientation() {
		return (SPUserSexualOrientation) SingletonHolder.singleton.get("sexualOrientation");
	}


	public void setSexualOrientation(SPUserSexualOrientation sexualOrientation) {
		put("sexualOrientation", sexualOrientation);
	}


	public static SPUserEthnicity getEthnicity() {
		return (SPUserEthnicity) SingletonHolder.singleton.get("ethnicity");
	}


	public void setEthnicity(SPUserEthnicity ethnicity) {
		put("ethnicity", ethnicity);
	}


	public static Location getLocation() {
		return (Location) SingletonHolder.singleton.get("location");
	}


	public void setLocation(Location location) {
		put("location", location);
	}


	public static Float getLat() {
		return (Float) SingletonHolder.singleton.get("lat");
	}


	public void setLat(Float lat) {
		put("lat", lat);
	}


	public static Float getLongt() {
		return (Float) SingletonHolder.singleton.get("longt");
	}


	public void setLongt(Float longt) {
		put("longt", longt);
	}


	public static Boolean getChildren() {
		return (Boolean) SingletonHolder.singleton.get("children");
	}


	public void setChildren(Boolean children) {
		put("children", children);
	}


	public static Integer getAnnualHouseholdIncome() {
		return (Integer) SingletonHolder.singleton.get("annualHouseholdIncome");
	}


	public void setAnnualHouseholdIncome(Integer annualHouseholdIncome) {
		put("annualHouseholdIncome", annualHouseholdIncome);
	}


	public static SPUserEducation getEducation() {
		return (SPUserEducation) SingletonHolder.singleton.get("education");
	}


	public void setEducation(SPUserEducation education) {
		put("education", education);
	}


	public static String getZipcode() {
		return (String) SingletonHolder.singleton.get("zipcode");
	}


	public void setZipcode(String zipcode) {
		put("zipcode", zipcode);
	}


	public static String getPoliticalAffiliation() {
		return (String) SingletonHolder.singleton.get("politicalAffiliation");
	}


	public void setPoliticalAffiliation(String politicalAffiliation) {
		put("politicalAffiliation", politicalAffiliation);
	}


	@SuppressWarnings("unchecked")
	public static ArrayList<String> getInterests() {
		return (ArrayList<String>) SingletonHolder.singleton.get("interests");
	}


	public void setInterests(ArrayList<String> interests) {
		put("interests", interests);
	}


	public static Boolean getIap() {
		return (Boolean) SingletonHolder.singleton.get("iap");
	}


	public void setIap(Boolean iap) {
		put("iap", iap);
	}


	public static Float getIapAmount() {
		return (Float) SingletonHolder.singleton.get("iap_amount");
	}


	public void setIapAmount(Float iap_amount) {
		put("iap_amount", iap_amount);
	}


	public static Integer getNumberOfSessions() {
		return (Integer) SingletonHolder.singleton.get("numberOfSessions");
	}


	public void setNumberOfSessions(Integer numberOfSessions) {
		put("numberOfSessions", numberOfSessions);
	}


	public static Long getPsTime() {
		return (Long) SingletonHolder.singleton.get("ps_time");
	}


	public void setPsTime(Long ps_time) {
		put("ps_time", ps_time);
	}


	public static Long getLastSession() {
		return (Long) SingletonHolder.singleton.get("last_session");
	}


	public void setLastSession(Long last_session) {
		put("last_session", last_session);
	}


	public static SPUserConnection getConnection() {
		return (SPUserConnection) SingletonHolder.singleton.get("connection");
	}

	public void setConnection(SPUserConnection connection) {
		put("connection", connection);
	}


	public static String getDevice() {
		return (String) SingletonHolder.singleton.get("device");
	}


	public void setDevice(String device) {
		put("device", device);
	}


	public static String getAppVersion() {
		return (String) SingletonHolder.singleton.get("app_version");
	}


	public void setAppVersion(String app_version) {
		put("app_version", app_version);
	}
	
	
	/**
	 * You can set custom values in the collection
	 * as long as the key doesn't match with an 
	 * existing one.
	 */
	public void addCustomValue(String key, Object value){
		boolean doesKeyAlreadyExistInCollection = false;
		
		for (String existedkey : keySet()) {
		    if(existedkey.equalsIgnoreCase(key)){
		    	doesKeyAlreadyExistInCollection = true;
		    	break;
		    }
		} 
		
		if(!doesKeyAlreadyExistInCollection){
			put(key, value);
		}
	}

}