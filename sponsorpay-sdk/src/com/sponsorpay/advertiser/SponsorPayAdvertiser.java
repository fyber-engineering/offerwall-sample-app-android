/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.advertiser;

import java.util.Map;

import android.content.Context;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.SPIdException;
import com.sponsorpay.utils.SPIdValidator;

/**
 * <p>
 * Provides convenience calls to run the Advertiser callback request. Manages the state of the SDK
 * determining whether a successful response to the callback request has been already received since
 * the application was installed in the host device.
 * </p>
 * 
 * <p>
 * It's implemented as a singleton, and its public methods are static.
 * </p>
 */
public class SponsorPayAdvertiser {

	/**
	 * Keep track of the persisted state of the Advertiser part of the SDK
	 */
	private SponsorPayAdvertiserState mPersistedState;

	/**
	 * Singleton instance.
	 */
	private static SponsorPayAdvertiser mInstance;

	/**
	 * Constructor. Stores the received application context and loads up the shared preferences.
	 * 
	 * @param context
	 *            The host application context.
	 */
	private SponsorPayAdvertiser(Context context) {
		if (context == null) {
			throw new RuntimeException("The SDK was not initialized yet. You should call SponsorPay.start method");
		}
		mPersistedState = new SponsorPayAdvertiserState(context);
	}
	
	private static SponsorPayAdvertiser getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SponsorPayAdvertiser(context);
		}
		return mInstance;
	}
	
	
	/**
	 * This method does the actual registration at the SponsorPay backend, performing the advertiser
	 * callback, and including in it a parameter to signal if a successful response has been
	 * received yet.
	 * 
	 * @param credentialsToken
	 *            The token id of the credentials to be used.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	private void register(String credentialsToken, Map<String, String> customParams) {
		SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);
		
		/* Send asynchronous call to SponsorPay's API */
		InstallCallbackSender callback = new InstallCallbackSender(credentials, mPersistedState);
		callback.setCustomParams(customParams);
		callback.trigger();
	}
	
	private void notitfyActionCompletion(String credentialsToken, String actionId, Map<String, String> customParams) {
		
		SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);
		
		/* Send asynchronous call to SponsorPay's API */
		ActionCallbackSender callback = new ActionCallbackSender(
				actionId, credentials, mPersistedState);
		callback.setCustomParams(customParams);
		callback.trigger();
	}
	
	//================================================================================
	// Actions
	//================================================================================
	
	
	/**
	 * Report an Action completion. It will use the values hold on the current credentials.
	 * 
	 * @param actionId
	 *            the id of the action
	 */
	public static void reportActionCompletion(String actionId) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		reportActionCompletion(credentialsToken, actionId);
	}
	

	/**
	 * Report an Action completion.
	 * 
	 * @param credentialsToken
	 * 			  the token id of credentials
	 * @param actionId
	 *            the id of the action
	 */
	public static void reportActionCompletion(String credentialsToken, String actionId) {
		reportActionCompletion(credentialsToken, actionId, null);
	}
	
	/**
	 * Report an Action completion.
	 * 
	 * @param credentialsToken
	 * 			  the token id of credentials
	 * @param actionId
	 *            the id of the action
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void reportActionCompletion(String credentialsToken, String actionId, Map<String, String> customParams) {
		try {
			SPIdValidator.validate(actionId);
		} catch (SPIdException e) {
			throw new RuntimeException("The provided Action ID is not valid. "
					+ e.getLocalizedMessage());
		}
		// The actual work is performed by the notitfyActionCompletion() instance method.
		//mInstance has to exist so we can have a credentialsToken, anyway, shielding it
		if (mInstance == null) {
			throw new RuntimeException("No valid credentials object was created yet.\n" +
					"You have to execute SponsorPay.start method first.");
		}
		mInstance.notitfyActionCompletion(credentialsToken, actionId, customParams);
	}
	
	//================================================================================
	// Callbacks
	//================================================================================

	/**
	 * Triggers the Advertiser callback. It will use the values hold on the current credentials.
	 * 
	 * @param context
	 *            Host application context.
	 */
	public static void register(Context context) {
		register(context, (Map<String, String>)null);
	}
	
	/**
	 * Triggers the Advertiser callback. It will use the values hold on the current credentials..
	 * 
	 * @param context
	 *            Host application context.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void register(Context context, Map<String, String> customParams) {
		String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
		register(credentialsToken, context, customParams);
	}

	/**
	 * Triggers the Advertiser callback.
	 * 
	 * @param credentialsToken
	 * 			  the token id of credentials
	 * @param context
	 *            Host application context.
	 * @param customParams
	 *            A map of extra key/value pairs to add to the request URL.
	 */
	public static void register(String credentialsToken, Context context, Map<String, String> customParams) {
		getInstance(context);
		
		// The actual work is performed by the register() instance method.
		mInstance.register(credentialsToken, customParams);
	}
	
}
