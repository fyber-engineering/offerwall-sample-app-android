package com.sponsorpay.publisher;

import java.util.Date;
import java.util.HashMap;

import android.location.Location;

public final class SPUser extends HashMap<String, Object>  {
	
	private static final long serialVersionUID = -5963403748409731798L;
	
	public static final String AGE = "age";
	public static final String BIRTHDAY = "birthdate";
	public static final String GENDER = "gender";
	public static final String SEXUAL_ORIENTATION = "sexualOrientation";
	public static final String ETHNICITY = "ethnicity";
	public static final String LOCATION = "location";
	public static final String LAT = "lat";
	public static final String LONGT = "longt";
	public static final String MARITAL_STATUS = "maritalStatus";
	public static final String HAS_CHILDREN = "hasChildren";
	public static final String NUMBER_OF_CHILDRENS = "numberOfChildrens";
	public static final String ANNUAL_HOUSEHOLD_INCOME = "annualHouseholdIncome";
	public static final String EDUCATION = "education";
	public static final String ZIPCODE = "zipcode";
	public static final String POLITICAL_AFFILIATION = "politicalAffiliation";
	public static final String INTERESTS = "interests";
	public static final String IAP = "iap";
	public static final String IAP_AMOUNT = "iap_amount";
	public static final String NUMBER_OF_SESSIONS = "numberOfSessions";
	public static final String PS_TIME = "ps_time";
	public static final String LAST_SESSION = "last_session";
	public static final String CONNECTION = "connection";
	public static final String DEVICE = "device";
	public static final String APP_VERSION = "app_version";
	


	private static final class SingletonHolder {
		static final SPUser singleton = new SPUser();
	}

	private SPUser() {
	}

	public static SPUser getInstance() {
		return SingletonHolder.singleton;
	}
     
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
	
	public enum SPUserMaritalStatus{
		single,
        relationship,
        married,
        divorced,
        engaged,
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
		return  (Integer) SingletonHolder.singleton.get(AGE);
	}


	public void setAge(Integer age) {
		put(AGE, age);
	}


	public Date getBirthdate() {
		return (Date) SingletonHolder.singleton.get(BIRTHDAY);
	}


	public void setBirthdate(Date birthdate) {
		put(BIRTHDAY, birthdate);
	}


	public static SPUserGender getGender() {
		return (SPUserGender) SingletonHolder.singleton.get(GENDER);
	}


	public void setGender(SPUserGender gender) {
		put(GENDER, gender);
	}


	public static SPUserSexualOrientation getSexualOrientation() {
		return (SPUserSexualOrientation) SingletonHolder.singleton.get(SEXUAL_ORIENTATION);
	}


	public void setSexualOrientation(SPUserSexualOrientation sexualOrientation) {
		put(SEXUAL_ORIENTATION, sexualOrientation);
	}


	public static SPUserEthnicity getEthnicity() {
		return (SPUserEthnicity) SingletonHolder.singleton.get(ETHNICITY);
	}


	public void setEthnicity(SPUserEthnicity ethnicity) {
		put(ETHNICITY, ethnicity);
	}


	public static Location getLocation() {
		return (Location) SingletonHolder.singleton.get(LOCATION);
	}


	public void setLocation(Location location) {
		put(LOCATION, location);
	}


	public static Float getLat() {
		return (Float) SingletonHolder.singleton.get(LAT);
	}


	public void setLat(Float lat) {
		put(LAT, lat);
	}


	public static Float getLongt() {
		return (Float) SingletonHolder.singleton.get(LONGT);
	}


	public void setLongt(Float longt) {
		put(LONGT, longt);
	}


	public static SPUserMaritalStatus getMaritalStatus() {
		return (SPUserMaritalStatus) SingletonHolder.singleton.get(MARITAL_STATUS);
	}


	public void setMaritalStatus(SPUserMaritalStatus maritalStatus) {
		put(MARITAL_STATUS, maritalStatus);
	}
	
	public static Boolean hasChildren() {
		return (Boolean) SingletonHolder.singleton.get(HAS_CHILDREN);
	}


	public void setHasChildren(Boolean hasChildren) {
		put(HAS_CHILDREN, hasChildren);
	}
	
	public static Integer getNumberOfChildrens(){
		return (Integer) SingletonHolder.singleton.get(NUMBER_OF_CHILDRENS);
	}

	public void setNumberOfChildrens(Integer numberOfChildrens) {
		put(NUMBER_OF_CHILDRENS, numberOfChildrens);
	}

	public static Integer getAnnualHouseholdIncome() {
		return (Integer) SingletonHolder.singleton.get(ANNUAL_HOUSEHOLD_INCOME);
	}


	public void setAnnualHouseholdIncome(Integer annualHouseholdIncome) {
		put(ANNUAL_HOUSEHOLD_INCOME, annualHouseholdIncome);
	}


	public static SPUserEducation getEducation() {
		return (SPUserEducation) SingletonHolder.singleton.get(EDUCATION);
	}


	public void setEducation(SPUserEducation education) {
		put(EDUCATION, education);
	}


	public static String getZipcode() {
		return (String) SingletonHolder.singleton.get(ZIPCODE);
	}


	public void setZipcode(String zipcode) {
		put("ZIPCODE", zipcode);
	}


	public static String getPoliticalAffiliation() {
		return (String) SingletonHolder.singleton.get(POLITICAL_AFFILIATION);
	}


	public void setPoliticalAffiliation(String politicalAffiliation) {
		put(POLITICAL_AFFILIATION, politicalAffiliation);
	}


	public static String[] getInterests() {
		return (String[]) SingletonHolder.singleton.get(INTERESTS);
	}


	public void setInterests(String[] interests) {
		put(INTERESTS, interests);
	}


	public static Boolean getIap() {
		return (Boolean) SingletonHolder.singleton.get(IAP);
	}


	public void setIap(Boolean iap) {
		put(IAP, iap);
	}


	public static Float getIapAmount() {
		return (Float) SingletonHolder.singleton.get(IAP_AMOUNT);
	}


	public void setIapAmount(Float iap_amount) {
		put(IAP_AMOUNT, iap_amount);
	}


	public static Integer getNumberOfSessions() {
		return (Integer) SingletonHolder.singleton.get(NUMBER_OF_SESSIONS);
	}


	public void setNumberOfSessions(Integer numberOfSessions) {
		put(NUMBER_OF_SESSIONS, numberOfSessions);
	}


	public static Long getPsTime() {
		return (Long) SingletonHolder.singleton.get(PS_TIME);
	}


	public void setPsTime(Long ps_time) {
		put(PS_TIME, ps_time);
	}


	public static Long getLastSession() {
		return (Long) SingletonHolder.singleton.get(LAST_SESSION);
	}


	public void setLastSession(Long last_session) {
		put(LAST_SESSION, last_session);
	}


	public static SPUserConnection getConnection() {
		return (SPUserConnection) SingletonHolder.singleton.get(CONNECTION);
	}

	public void setConnection(SPUserConnection connection) {
		put(CONNECTION, connection);
	}


	public static String getDevice() {
		return (String) SingletonHolder.singleton.get(DEVICE);
	}


	public void setDevice(String device) {
		put(DEVICE, device);
	}


	public static String getAppVersion() {
		return (String) SingletonHolder.singleton.get(APP_VERSION);
	}


	public void setAppVersion(String app_version) {
		put(APP_VERSION, app_version);
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