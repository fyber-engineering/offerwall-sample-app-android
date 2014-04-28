
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
		EbzInterstitial.EbzInterstitialListener { 
	
	
	private static final String TARGETING_KEYWORDS = "target";
	private static final String TAG                = "EbuzzingVideoMediationAdapter";
	
	
	private EbzInterstitial mEbzInterstitialRewarded;

    /**
     * Instantiate the EbzInterstitial with the provided tag.
     * Set the target words, if any.
     */
	public EbuzzingVideoMediationAdapter(EbuzzingMediationAdapter adapter, final String tag, final Activity activity) {
		super(adapter);
				
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				//instantiate the EbzInterstitial with the provided tag and set the rewarded as true
				mEbzInterstitialRewarded = new EbzInterstitial(activity, tag, true);
				
				//set the listener for the eBuzzing callback methods
				mEbzInterstitialRewarded.setListener(EbuzzingVideoMediationAdapter.this);
				
				//set the target keywords if these have been set in the config file
				setKeywordsFromConfig();
			}
		});
	}

    /**
     * Load the video.
     */
	@Override
	public void videosAvailable(Context context) {

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
			
		SponsorPayLogger.i(TAG, "Showing video");		
			
		//show video on main thread		
		parentActivity.runOnUiThread(new Runnable() {			
			@Override			
			public void run() {				
				mEbzInterstitialRewarded.show();
				notifyVideoStarted();
			}	
		});
			
	}

	
    /**
	 * @return An array with all the target words that have been declared in the config file
	 * 
	 * @throws JSONException
	 * @throws NullPointerException - if the target field in the config file doesn't exist 
	 */
	private String[] getTargetingKeywordsFromConfig() throws JSONException, NullPointerException {
		
		//get the jsonArray which contains all the target words
		JSONArray  keywordsForAdapter = SPMediationConfigurator.getConfiguration(getName(), TARGETING_KEYWORDS, JSONArray.class);

		ArrayList<String> listOfTargetWords = new ArrayList<String>();
		
		//assign each one of these into the arraylist
		for(int index = 0; index < keywordsForAdapter.length(); index++){
			listOfTargetWords.add(keywordsForAdapter.getString(index));
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
			String[] targetting_keywords = getTargetingKeywordsFromConfig();
			
			if (targetting_keywords.length > 0){
				
				//set the keywords specified in config file
				mEbzInterstitialRewarded.setKeywords(targetting_keywords);
				
				SponsorPayLogger.i(TAG, "Targetting keywords have been set");
			}
		} catch (JSONException jsonEx) {
			SponsorPayLogger.e(TAG, "Getting target keywords error: " + jsonEx.toString());
			
		} catch (NullPointerException npe) {
			SponsorPayLogger.e(TAG, "Getting target keywords error: " + npe.toString() 
					+ " , target keywords don't exist in config file");
		}
	}
	

    /**
	 * ---------------------------------------------------
	 * Callback methods provided by eBuzzing SDK.
	 * There's no documentation about what should 
	 * return and eBuzzing's test app returns false
	 * for all of them.
	 * ---------------------------------------------------
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
		return false;
	}

	@Override
	public boolean onRewardUnlocked() {
		SponsorPayLogger.i(TAG, "Rewarded");
		setVideoPlayed();
		return false;
	}

}