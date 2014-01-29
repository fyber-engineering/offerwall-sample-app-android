package com.sponsorpay.mediation.mbe;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.sponsorpay.mediation.AppLovinMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.utils.SponsorPayLogger;

public class AppLovinVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<AppLovinMediationAdapter> implements
		AppLovinAdLoadListener, AppLovinAdRewardListener,
		AppLovinAdVideoPlaybackListener, AppLovinAdDisplayListener {

	private AppLovinIncentivizedInterstitial mIncentivizedAd;

	public AppLovinVideoMediationAdapter(
			AppLovinMediationAdapter appLovinMediationAdapter, Activity activity) {
		super(appLovinMediationAdapter);
		mIncentivizedAd = AppLovinIncentivizedInterstitial.create( activity );
	}

	@Override
	public void videosAvailable(Context context) {
		mIncentivizedAd.preload(this);
	}

	@Override
	public void startVideo(Activity parentActivity) {
		mIncentivizedAd.show(parentActivity, this, this, this);
		// we need to notify here because of the watch dialog
		notifyVideoStarted();
	}

	// AppLovinAdLoadListener
	@Override
	public void adReceived(AppLovinAd ad) {
		sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationSuccess);
	}

	@Override
	public void failedToReceiveAd(int errorCode) {
		if (errorCode == 202 || errorCode == 204) {
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);
		} else {
			SponsorPayLogger.d(AppLovinMediationAdapter.TAG, "failedToReceiveAd with errorCode - " + errorCode);
			sendValidationEvent(SPTPNVideoValidationResult.SPTPNValidationError);
		}
	}

	
	//AppLovinAdRewardListener
	@SuppressWarnings("rawtypes")
	@Override
	public void userOverQuota(AppLovinAd ad, Map response) {
		//public void userOverQuota(AppLovinAd ad, Map<String, String> response) {
		// User watched video but has already earned the maximum number of coins you specified in the UI.
	}
		

	@SuppressWarnings("rawtypes")
	@Override
	public void userRewardRejected(AppLovinAd ad, Map response) {
		//public void userRewardRejected(AppLovinAd ad, Map<String, String> response) {
		// The user's reward was marked as fraudulent, they are most likely trying to modify their balance illicitly.	
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void userRewardVerified(AppLovinAd ad, Map response) {
		//public void userRewardVerified(AppLovinAd ad, Map<String, String> response) {
		// AppLovin servers validated the reward. Refresh user balance from your server.
		setVideoPlayed();
	}
	
	@Override
	public void validationRequestFailed(AppLovinAd ad, int errorCode) {
		// We were unable to contact the server. Grant the reward, or don't, as you see fit.
	}
	
	@Override
	public void userDeclinedToViewAd(AppLovinAd ad) {
		notifyCloseEngagement();
	}

	//AppLovinAdVideoPlaybackListener
	@Override
	public void videoPlaybackBegan(final AppLovinAd ad) {
		//the watch dialog can lead to timeout
//		notifyVideoStarted();
	}

	@Override
	public void videoPlaybackEnded(final AppLovinAd ad,
			final double percentViewed, final boolean fullyWatched) {
//		if (fullyWatched) {
//			setVideoPlayed();
//		}
	}

	//AppLovinAdDisplayListener
	@Override
	public void adDisplayed(AppLovinAd ad) {
		//the watch dialog can lead to timeout
	}

	@Override
	public void adHidden(AppLovinAd arg0) {
		notifyCloseEngagement();
	}

}
