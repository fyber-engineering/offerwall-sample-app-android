/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.mbe;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;

import com.ebuzzing.sdk.interfaces.EbzError;
import com.ebuzzing.sdk.interfaces.EbzInterstitial;
import com.sponsorpay.mediation.EbuzzingMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.utils.SponsorPayLogger;

public class EbuzzingVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<EbuzzingMediationAdapter> implements
		EbzInterstitial.EbzInterstitialListener { //,EbzVideoListener {
	
	
	private static final String TARGETTING_KEYWORDS = "target";
	private static final String TAG_INTERSTITIAL    = "TAG_ANDROID_SP1_INTERSTITIAL";
	private static final String TAG                 = "EbuzzingVideoMediationAdapter";
	
	
	private EbzInterstitial mEbzInterstitialRewarded;

	public EbuzzingVideoMediationAdapter(EbuzzingMediationAdapter adapter) {
		super(adapter);
	}

	/**
     * Instantiate the EbzInterstitial with the provided tag.
     * Set the target words, if any.
     * Load the video.
     */
	@Override
	public void videosAvailable(Context context) {
		//instantiate the EbzInterstitial with the provided tag and set the rewarded as true
		mEbzInterstitialRewarded = new EbzInterstitial(context, TAG_INTERSTITIAL, true);
		
		//set the listener for the eBuzzing callback methods
		mEbzInterstitialRewarded.setListener(this);
		
		//set the target keywords if these have been set in the config file
		setKeywordsFromConfig();
		
		SponsorPayLogger.i(TAG, "Loading video");
		
		//load the video
		mEbzInterstitialRewarded.load();
	}

	/**
	 * Start playing the video.
	 * Notify that the video has started playing.
	 */
	@Override
	public void startVideo(final Activity parentActivity) {
		
		if(mEbzInterstitialRewarded.isLoaded()){
		
			SponsorPayLogger.i(TAG, "Showing video");
			
			//show the video
			mEbzInterstitialRewarded.show();
			notifyVideoStarted();
			
		}else{
			
			SponsorPayLogger.i(TAG, "Video not loaded");
			
			sendVideoEvent(SPTPNVideoEvent.SPTPNVideoEventError);
			clearVideoEvent();
			
		}
		
	}

	
	/**
	 * @return An array with all the target words that have been declared in the config file
	 * 
	 * @throws JSONException
	 * @throws NullPointerException - if the target field in the config file doesn't exist 
	 */
	private String[]  getConfigurationMetadata() throws JSONException, NullPointerException {
		
		//get the jsonArray which contains all the target words
		JSONArray  configurationForAdapter = SPMediationConfigurator.getConfiguration(getName(), TARGETTING_KEYWORDS, JSONArray .class);

		ArrayList<String> listOfTargetWords = new ArrayList<String>();
		
		//assign each one of these into the arraylist
		for(int index = 0; index < configurationForAdapter.length(); index++){
			listOfTargetWords.add(configurationForAdapter.getString(index));
		}
		
        //convert the arraylist to an array of Strings
		return listOfTargetWords.toArray(new String[listOfTargetWords.size()]);
	}
	
	/**
	 * Assign all the target words of the configuration file 
	 * to the EbzInterstitial object that has been instantiated
	 * into the videosAvailable().
	 */
	private void setKeywordsFromConfig(){
		try {
			String[] targetting_keywords = getConfigurationMetadata();
			
			if (targetting_keywords.length > 0){
				
				//set the keywords specified in config file
				mEbzInterstitialRewarded.setKeywords(targetting_keywords);
				
				SponsorPayLogger.i(TAG, "Targetting keywords have been set");
			}
		} catch (JSONException jsonEx) {
			SponsorPayLogger.e(TAG, "Getting target keywords: " + jsonEx.toString());
			
		} catch (NullPointerException npe) {
			SponsorPayLogger.e(TAG, "Getting target keywords: " + npe.toString() 
					+ " , target keywords doesn't exist in config file");
		}
	}
	

	/**
	 * -----------------------------------------
	 * Callback methods provided by eBuzzing SDK
	 * -----------------------------------------
	 */

	@Override
	public boolean onCloseFullscreen() {
		SponsorPayLogger.i(TAG, "Closing video");
		notifyCloseEngagement();
		return false;
	}

	@Override
	public boolean onDisplayFullscreen() {
		SponsorPayLogger.i(TAG, "Set full screen video");
		return false;
	}

	@Override
	public boolean onLoadFail(EbzError errorCode) {
		
		SponsorPayLogger.e(TAG, "Error: " + errorCode.getMessage());

		if(errorCode.equals(EbzError.EbzNoAdsAvailable)){
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
			
		}else if(errorCode.equals(EbzError.EbzNetworkError)){
			
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNetworkError);
						
		}else {
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationError);
		}
		
		return false;
	}

	@Override
	public boolean onLoadSuccess() {
		SponsorPayLogger.i(TAG, "Video loaded successfully");
		sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationSuccess);
		return true;
	}

	@Override
	public boolean onRewardUnlocked() {
		SponsorPayLogger.i(TAG, "Rewarded");
		setVideoPlayed();
		return false;
	}

}