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

	@Override
	public void videosAvailable(Context context) {
		//instantiate the EbzInterstitial with the provided tag and set the rewarded as true
		mEbzInterstitialRewarded = new EbzInterstitial(context, TAG_INTERSTITIAL, true);
		//mEbzInterstitialRewarded.setListener(this);	
		
		//set the target keywords if these have been set in the config file
		setKeywordsFromConfig();
		
		SponsorPayLogger.i(TAG, "Loading video");
		
		//load the video
		mEbzInterstitialRewarded.load();
	}

	@Override
	public void startVideo(final Activity parentActivity) {
		
		SponsorPayLogger.i(TAG, "Showing video");
		
		//show the video
		mEbzInterstitialRewarded.show();
		notifyVideoStarted();
	}

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

	
	private String[]  getConfigurationMetadata() throws JSONException, NullPointerException {
		
		JSONArray  configurationForAdapter = SPMediationConfigurator.getConfiguration(getName(), TARGETTING_KEYWORDS, JSONArray .class);

		ArrayList<String> listOfTargetWords = new ArrayList<String>();
		
		for(int index = 0; index < configurationForAdapter.length(); index++){
			listOfTargetWords.add(configurationForAdapter.getString(index));
		}
		

		return listOfTargetWords.toArray(new String[listOfTargetWords.size()]);
	}
	
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
			SponsorPayLogger.e(TAG, "Getting target keywords: " + npe.toString());
		}
	}

}