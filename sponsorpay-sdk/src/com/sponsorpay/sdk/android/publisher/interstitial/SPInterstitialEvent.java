package com.sponsorpay.sdk.android.publisher.interstitial;

public enum SPInterstitialEvent {
	
	ValidationRequest("request"),
	ValidationFill("fill"),
	ValidationNoFill("no_fill"),
	ShowImpression("impression"),
	ShowClick("click"),
	ShowClose("close"),
	Error("error"),
	NotIntegrated("no_sdk");
	
    private final String text;
	
	private SPInterstitialEvent(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
    
}
