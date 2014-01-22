package com.sponsorpay.mediation.interstitial;


public enum MockInterstitialSetting {

	ValidationAdsAvailable("Ads available"),
	ValidationNoAds("No Ads available"),
	ValidationError("Validation error"),
	ShowError("Show error");
	
    private final String text;
	
	private MockInterstitialSetting(final String text) {
		this.text = text;
	}

    @Override
    public String toString() {
        return text;
    }
	
}
