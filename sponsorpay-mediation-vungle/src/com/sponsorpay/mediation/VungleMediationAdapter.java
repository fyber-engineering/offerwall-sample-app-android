/**
 * SponsorPay Android SDK
 * 
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;

import com.sponsorpay.mediation.mbe.VungleVideoMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.vungle.publisher.AdConfig;
import com.vungle.publisher.EventListener;
import com.vungle.publisher.Orientation;
import com.vungle.publisher.VunglePub;

public class VungleMediationAdapter extends SPMediationAdapter implements EventListener {

	private static final String TAG = "VungleAdapter";

	// Supported Vungle SDK version: 3.2.1
	private static final String ADAPTER_VERSION = "2.1.1";

	private static final String ADAPTER_NAME = "Vungle";

	private static final String APP_ID = "app.id";

	private static final String SOUND_ENABLED = "sound.enabled";
	private static final String AUTO_ROTATION_ENABLED = "auto.rotation.enabled";
	private static final String BACK_BUTTON_ENABLED = "back.button.enabled";
	private static final String INCENTIVIZED_MODE = "incentivized.mode";
	private static final String INCENTIVIZED_USER_ID = "incentivized.user.id";
	private static final String INCENTIVIZED_CANCEL_DIALOG_TITLE = "cancel.dialog.title";
	private static final String INCENTIVIZED_CANCEL_DIALOG_TEXT = "cancel.dialog.text";
	private static final String INCENTIVIZED_CANCEL_DIALOG_BUTTON = "cancel.dialog.button";
	private static final String INCENTIVIZED_KEEP_WATCHING = "keep.watching.text";

	private VungleVideoMediationAdapter mVideoMediationAdapter = new VungleVideoMediationAdapter(this);

	private HashSet<EventListener> mVungleListeners = new HashSet<EventListener>();

	@Override
	public boolean startAdapter(Activity activity) {
		SponsorPayLogger.d(TAG, "Starting Vungle adapter - SDK version " + VunglePub.VERSION);
		String appId = SPMediationConfigurator.getConfiguration(ADAPTER_NAME, APP_ID, String.class);
		if (StringUtils.notNullNorEmpty(appId)) {
			SponsorPayLogger.i(TAG, "Using App ID = " + appId);

			VunglePub vunglePub = VunglePub.getInstance();
			vunglePub.init(activity, appId);
			vunglePub.setEventListener(this);

			setVungleSetting(vunglePub.getGlobalAdConfig());

			mVungleListeners.add(mVideoMediationAdapter);
			return true;
		}
		SponsorPayLogger.d(TAG, "App Id  must have a valid value!");
		return false;
	}

	@Override
	public String getName() {
		return ADAPTER_NAME;
	}

	@Override
	public String getVersion() {
		return ADAPTER_VERSION;
	}

	@Override
	public VungleVideoMediationAdapter getVideoMediationAdapter() {
		return mVideoMediationAdapter;
	}

	@Override
	public SPInterstitialMediationAdapter<SPMediationAdapter> getInterstitialMediationAdapter() {
		return null;
	}

	@Override
	protected Set<EventListener> getListeners() {
		return mVungleListeners;
	}

	// Vungle EventListener interface
	@Override
	public void onAdEnd(boolean wasCallToActionClicked) {
		notifyListeners((Object[]) null);
	}

	@Override
	public void onAdStart() {
		notifyListeners((Object[]) null);
	}

	@Override
	public void onCachedAdAvailable() {
		notifyListeners((Object[]) null);
	}

	@Override
	public void onVideoView(boolean isCompletedView, int watchedMillis, int videoDurationMillis) {
		notifyListeners(new Object[] { isCompletedView, watchedMillis, videoDurationMillis },
				new Class[] { boolean.class, int.class, int.class });
	}

	@Override
	public void onAdUnavailable(String arg0) {
		notifyListeners(new Object[] { arg0 }, new Class[] { String.class });
	}

	// Helper methods for additional settings
	private void setVungleSetting(AdConfig adConfig) {
		setAutoOrientation(adConfig);
		setSoundEnabled(adConfig);
		setBackButtonEnabled(adConfig);
		setIncentivizedMode(adConfig);
		setIncentivizedUserId(adConfig);
		setIncentivizedCancelDialogTitle(adConfig);
		setIncentivizedCancelDialogBodyText(adConfig);
		setIncentivizedCancelDialogCloseButtonText(adConfig);
		setIncentivizedCancelDialogKeepWatchingButtonText(adConfig);
	}

	private void setAutoOrientation(AdConfig adConfig) {
		Boolean enabled = SPMediationConfigurator.getConfiguration(
				ADAPTER_NAME, AUTO_ROTATION_ENABLED, Boolean.class);
		if (enabled != null) {
			adConfig.setOrientation(enabled ? Orientation.autoRotate : Orientation.matchVideo);
		}
	}

	private void setSoundEnabled(AdConfig adConfig) {
		Boolean enabled = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				SOUND_ENABLED, Boolean.class);
		if (enabled != null) {
			adConfig.setSoundEnabled(enabled);
		}
	}

	private void setBackButtonEnabled(AdConfig adConfig) {
		Boolean enabled = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				BACK_BUTTON_ENABLED, Boolean.class);
		if (enabled != null) {
			adConfig.setBackButtonImmediatelyEnabled(enabled);
		}
	}

	private void setIncentivizedMode(AdConfig adConfig) {
		Boolean isIncentivized = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				INCENTIVIZED_MODE, Boolean.class);
		if (isIncentivized != null) {
			adConfig.setIncentivized(isIncentivized);
		}
	}

	private void setIncentivizedUserId(AdConfig adConfig) {
		String userId = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				INCENTIVIZED_USER_ID, String.class);
		if (StringUtils.notNullNorEmpty(userId)) {
			adConfig.setIncentivizedUserId(userId);
		}
	}

	private void setIncentivizedCancelDialogTitle(AdConfig adConfig) {
		String text = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				INCENTIVIZED_CANCEL_DIALOG_TITLE, String.class);
		if (StringUtils.notNullNorEmpty(text)) {
			adConfig.setIncentivizedCancelDialogTitle(text);
		}
	}

	private void setIncentivizedCancelDialogBodyText(AdConfig adConfig) {
		String text = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				INCENTIVIZED_CANCEL_DIALOG_TEXT, String.class);
		if (StringUtils.notNullNorEmpty(text)) {
			adConfig.setIncentivizedCancelDialogBodyText(text);
		}
	}

	private void setIncentivizedCancelDialogCloseButtonText(AdConfig adConfig) {
		String text = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				INCENTIVIZED_CANCEL_DIALOG_BUTTON, String.class);
		if (StringUtils.notNullNorEmpty(text)) {
			adConfig.setIncentivizedCancelDialogCloseButtonText(text);
		}
	}

	private void setIncentivizedCancelDialogKeepWatchingButtonText(AdConfig adConfig) {
		String text = SPMediationConfigurator.getConfiguration(ADAPTER_NAME,
				INCENTIVIZED_KEEP_WATCHING, String.class);
		if (StringUtils.notNullNorEmpty(text)) {
			adConfig.setIncentivizedCancelDialogKeepWatchingButtonText(text);
		}
	}

}
