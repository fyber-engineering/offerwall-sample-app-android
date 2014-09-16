/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


/**
 * <p>
 * Contains convenience methods to generate digital signatures for texts and URL-encoded key-value
 * maps.
 * </p>
 */
public class SignatureTools {
	public static final String NO_SHA1_RESULT = "nosha1";
	private static final String SHA1_ALGORITHM = "SHA1";
	private static final String TAG = "SignatureTools";

	/**
	 * <p>
	 * Returns the SHA1 hash of a key-value map serialized into a string in URL-encoded form,
	 * ordered alphabetically by key in the following form:
	 * </p>
	 * <p>
	 * key_1=value_1&key_2&value_2&...&key_n=value_n&secret_token
	 * </p>
	 * 
	 * @param parameters
	 *            The key-value map to generate the signature from.
	 * @param secretToken
	 *            The secret_token to append at the end of the URL-encoded string before hashing it.
	 * @return The SHA1 hash or {@link #NO_SHA1_RESULT} if no SHA1 algorithm is available.
	 */
	public static String generateSignatureForParameters(Map<String, String> parameters,
			String secretToken) {
		TreeSet<String> orderedKeys = new TreeSet<String>();
		orderedKeys.addAll(parameters.keySet());

		Iterator<String> orderedKeysIterator = orderedKeys.iterator();

		String concatenatedOrderedParams = StringUtils.EMPTY_STRING;

		while (orderedKeysIterator.hasNext()) {
			String key = orderedKeysIterator.next();
			String value = parameters.get(key);

			concatenatedOrderedParams += String.format("%s=%s&", key, value);
		}

		return generateSignatureForString(concatenatedOrderedParams, secretToken);
	}

	/**
	 * Appends the provided secret token to the provided clear text and returns the SHA1 hash of the
	 * result.
	 * 
	 * @param text
	 *            The clear text.
	 * @param secretToken
	 *            The secret token.
	 * @return The SHA1 hash of text + secret token or {@link #NO_SHA1_RESULT} if no SHA1 algorithm
	 *         is available.
	 */
	public static String generateSignatureForString(String text, String secretToken) {
		String textPlusKey = text + secretToken;
		
		return generateSHA1ForString(textPlusKey);
	}

	public static String generateSHA1ForString(String text) {
		String digestString = NO_SHA1_RESULT;
		
		try {
			MessageDigest sha1 = MessageDigest.getInstance(SHA1_ALGORITHM);
			byte[] digestBytes = sha1.digest(text.getBytes());
			digestString = byteArray2Hex(digestBytes);
		} catch (NoSuchAlgorithmException e) {
			SponsorPayLogger.e("UrlBuilder", "SHA1 algorithm not available.", e);
		}
		
		return digestString;
	}
	
	public static String byteArray2Hex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String hexValue = formatter.toString();
		formatter.close();
		return hexValue;
	}
	
	public static String serialize(Serializable obj) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
			return new String(Base64.encode(baos.toByteArray()));
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, "IOException when serialized the Successful VCS response: " + e.getMessage());
		}
		return null;
        
        
//	    ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
//	    ObjectOutputStream objectOS;
//		try {
//			objectOS = new ObjectOutputStream(byteArrayOS);
//			objectOS.writeObject(obj);
//		    objectOS.flush();
//		   
//		    String encodedListToString = byteArrayOS.toString();
//		    
//		    objectOS.close();
//		    byteArrayOS.close();
//		   
//		    return encodedListToString;
//		} catch (IOException e) {
//			SponsorPayLogger.e(TAG, "IOException when serialized the Successful VCS response: " + e.getMessage());
//		}
//		return null;
	}
	
	public static Object deserialize(String savedObject) {
		
		byte [] data = Base64.decode(savedObject);
        ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
	        
		} catch (StreamCorruptedException e) {
			SponsorPayLogger.e(TAG, "StreamCorruptedException when deserialized the Successful VCS response from the: "
					+ "SharedPreferences: " + e.getMessage());
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, "IOException when deserialized the Successful VCS response from the: "
					+ "SharedPreferences: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			SponsorPayLogger.e(TAG, "ClassNotFoundException when deserialized the Successful VCS response from the: " 
					+ "SharedPreferences: " + e.getMessage());
		}

        return null;
        
//		byte[] savedObjectAsByteArray = savedObject.getBytes();
//	    ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(savedObjectAsByteArray);
//	    ObjectInputStream objectIS;
//		try {
//			objectIS = new ObjectInputStream(byteArrayIS);
//			Object desirializedObject = objectIS.readObject();
//			objectIS.close();
//			
//			return desirializedObject;
//		} catch (StreamCorruptedException e) {
//			SponsorPayLogger.e(TAG, "StreamCorruptedException when deserialized the Successful VCS response from the: "
//					+ "SharedPreferences: " + e.getMessage());
//		} catch (IOException e) {
//			SponsorPayLogger.e(TAG, "IOException when deserialized the Successful VCS response from the: "
//					+ "SharedPreferences: " + e.getMessage());
//		} catch (ClassNotFoundException e) {
//			SponsorPayLogger.e(TAG, "ClassNotFoundException when deserialized the Successful VCS response from the: " 
//					+ "SharedPreferences: " + e.getMessage());
//		}
//		return null;
	}
}
