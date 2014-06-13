package com.sponsorpay.mediation.helper;

import java.util.Map;

import com.sponsorpay.mediation.HyprMXMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.mediation.mbe.HyprMXVideoMediationAdapter;

public class HyprMXVideoAdapterHelper {

	private static final String HYPRMX_VIDEO_ADAPTER = "HyprMXVideoAdapter";
	
	public static HyprMXVideoMediationAdapter getHyprMXVideoMediationAdapter(){
		return SPMediationConfigurator.getConfiguration(getName(),
				HYPRMX_VIDEO_ADAPTER, HyprMXVideoMediationAdapter.class);
	}
	
	private static String getName(){
		return HyprMXMediationAdapter.ADAPTER_NAME;
	}
	
	public static void setHyprMXVideoMediationAdapter(HyprMXVideoMediationAdapter videoAdapter) {
		Map<String, Object> configs = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(getName());
		configs.put(HYPRMX_VIDEO_ADAPTER, videoAdapter);
	}
	
//	public static YuMeInterstitialMediationAdapter getYuMeInterstitialAdapter() {
//		return SPMediationConfigurator.getConfiguration(getName(),
//				YUME_INTERSTITIAL_ADAPTER, YuMeInterstitialMediationAdapter.class);
//	}
	
}
