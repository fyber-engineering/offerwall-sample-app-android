package com.sponsorpay.mediation.helper;

import java.util.Map;

import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.mediation.TremorMediationAdapter;
import com.sponsorpay.mediation.interstitial.TremorInterstitialMediationAdapter;

public class TremorInterstitialAdapterHelper {

	private static final String TREMOR_INTERSTITIALS_ADAPTER = "TremorInterstitialsAdapter";

	public static TremorInterstitialMediationAdapter getTremorInterstitialMediationAdapter() {
		return SPMediationConfigurator.getConfiguration(getName(), TREMOR_INTERSTITIALS_ADAPTER,
				TremorInterstitialMediationAdapter.class);
	}

	private static String getName() {
		return TremorMediationAdapter.ADAPTER_NAME;
	}

	public static void setTremorInterstitialMediationAdapter(TremorInterstitialMediationAdapter videoAdapter) {
		Map<String, Object> configs = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(getName());
		configs.put(TREMOR_INTERSTITIALS_ADAPTER, videoAdapter);
	}

}
