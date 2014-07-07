package com.sponsorpay.mediation.mbe;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.sponsorpay.mediation.AppLovinMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;

public class AppLovinVideoMediationAdapter extends
		SPBrandEngageMediationAdapter<AppLovinMediationAdapter> implements
		AppLovinAdRewardListener,
		AppLovinAdVideoPlaybackListener, AppLovinAdDisplayListener {

	private AppLovinIncentivizedInterstitial mIncentivizedAd;
	private boolean mRewardVerified = false;

	public AppLovinVideoMediationAdapter(
			AppLovinMediationAdapter appLovinMediationAdapter, Activity activity) {
		super(appLovinMediationAdapter);
		mIncentivizedAd = AppLovinIncentivizedInterstitial.create( activity );
		mIncentivizedAd.preload(null);
	}

	@Override
	public void videosAvailable(Context context) {
				
		sendValidationEvent(mIncentivizedAd.isAdReadyToDisplay() ? SPTPNVideoValidationResult.SPTPNValidationSuccess
				: SPTPNVideoValidationResult.SPTPNValidationNoVideoAvailable);

		mRewardVerified = false;
	}

	@Override
	public void startVideo(Activity parentActivity) {
		if(mIncentivizedAd.isAdReadyToDisplay()){
			mIncentivizedAd.show(parentActivity, this, this, this);
			// we need to notify here because of the watch dialog
			notifyVideoStarted();
		}
	}

	//AppLovinAdRewardListener
	@SuppressWarnings("rawtypes")
	@Override
	public void userOverQuota(AppLovinAd ad, Map response) {
		// User watched video but has already earned the maximum number of coins you specified in the UI.
		mRewardVerified = false;
	}
		

	@SuppressWarnings("rawtypes")
	@Override
	public void userRewardRejected(AppLovinAd ad, Map response) {
		// The user's reward was marked as fraudulent, they are most likely trying to modify their balance illicitly.	
		mRewardVerified = false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void userRewardVerified(AppLovinAd ad, Map response) {
		// AppLovin servers validated the reward. Refresh user balance from your server.
//		setVideoPlayed();
		mRewardVerified = true;
	}
	
	@Override
	public void validationRequestFailed(AppLovinAd ad, int errorCode) {
		// We were unable to contact the server. Grant the reward, or don't, as you see fit.
		mRewardVerified = false;
	}
	
	@Override
	public void userDeclinedToViewAd(AppLovinAd ad) {
		notifyCloseEngagement();
	}

	//AppLovinAdVideoPlaybackListener
	@Override
	public void videoPlaybackBegan(final AppLovinAd ad) {
		//the watch dialog can lead to timeout -> notify video start before
	}

	@Override
	public void videoPlaybackEnded(final AppLovinAd ad,
			final double percentViewed, final boolean fullyWatched) {
		if (fullyWatched && mRewardVerified) {
			mRewardVerified = false;
			setVideoPlayed();
		}
	}

	//AppLovinAdDisplayListener
	@Override
	public void adDisplayed(AppLovinAd ad) {
		//the watch dialog can lead to timeout -> notify video start before
	}

	@Override
	public void adHidden(AppLovinAd arg0) {
		mIncentivizedAd.preload(null);
		notifyCloseEngagement();
	}

}
