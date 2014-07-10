/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SponsorPayParametersProvider {

	private static SponsorPayParametersProvider INSTANCE = new SponsorPayParametersProvider();
	
	private Set<SPParametersProvider> providers  = new HashSet<SPParametersProvider>();
	
	private SponsorPayParametersProvider() {
	}
	
	public static boolean addParametersProvider(SPParametersProvider newProvider) {
		synchronized (INSTANCE) {
			return INSTANCE.addNewProvider(newProvider);
		}
	}

	public static boolean removeParametersProvider(SPParametersProvider provider) {
		synchronized (INSTANCE) {
			return INSTANCE.removeProvider(provider);
		}
	}
	
	public static Map<String, String> getParameters() {
		synchronized (INSTANCE) {
			Set<SPParametersProvider> providers = INSTANCE.getProviders();
			if (providers.size() == 0) {
				return Collections.emptyMap();
			} else {
				HashMap<String, String> map = new HashMap<String, String>();
				for (SPParametersProvider provider : providers) {
					map.putAll(provider.getParameters());
				}
				return map;
			}
		}
	}
	
	private Set<SPParametersProvider> getProviders() {
		return providers;
	}

	private boolean addNewProvider(SPParametersProvider newProvider) {
		return providers.add(newProvider);
	}

	private boolean removeProvider(SPParametersProvider provider) {
		return providers.remove(provider);
	}
}
