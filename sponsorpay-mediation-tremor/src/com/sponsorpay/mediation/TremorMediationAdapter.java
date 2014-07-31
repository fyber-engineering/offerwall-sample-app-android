package com.sponsorpay.mediation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;

import com.sponsorpay.mediation.interstitial.TremorInterstitialMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationAdapter;
import com.sponsorpay.utils.SponsorPayLogger;
import com.tremorvideo.sdk.android.videoad.TremorVideo;

public class TremorMediationAdapter extends SPMediationAdapter {

	private static final String TAG = "Tremor";

	private static final String ADAPTER_VERSION = "1.0.0";

	public static final String ADAPTER_NAME = "Tremor";
	
	private static final String APP_IDS = "appIds";

	private TremorInterstitialMediationAdapter mInterstitialMediationAdapter;
	
	@Override
	public boolean startAdapter(final Activity activity) {
		JSONArray appIds = getConfigListOfAppIds();
		
		if(null == appIds){
			SponsorPayLogger.e(TAG, "The appIds list hasn't been provided properly in adapters.config file. appIds is null");
			return false;
		}
		
		final List<String> tList = new ArrayList<String>();
		for(int i = 0; i < appIds.length(); i++){
		    try {
				tList.add(appIds.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
				SponsorPayLogger.e(TAG, "The appIds list hasn't been provided properly in adapters.config file. Error while processing JSONArray.");
				return false;
			}
		}
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TremorVideo.initialize(activity, tList.toArray(new String[tList.size()]));
			}
		});
		
		mInterstitialMediationAdapter = new TremorInterstitialMediationAdapter(TremorMediationAdapter.this);
		return true;
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
	public SPBrandEngageMediationAdapter<? extends SPMediationAdapter> getVideoMediationAdapter() {
		// no mBE mediation adapter - return null
		return null;
	}

	@Override
	public SPInterstitialMediationAdapter<? extends SPMediationAdapter> getInterstitialMediationAdapter() {
		return mInterstitialMediationAdapter;
	}

	@Override
	protected Set<? extends Object> getListeners() {
		return null;
	}

	private JSONArray getConfigListOfAppIds(){
		JSONArray metadata = SPMediationConfigurator.getConfiguration(getName(), APP_IDS, JSONArray.class);
		return metadata;
	}
	
}
