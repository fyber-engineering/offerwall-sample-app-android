package com.sponsorpay.mediation.interstitial;

public interface ITremorAdapterHelperInterface {
	public void processActivityResult(int pResultCode);
	public void requestAdValidationError(Throwable thr);
}
