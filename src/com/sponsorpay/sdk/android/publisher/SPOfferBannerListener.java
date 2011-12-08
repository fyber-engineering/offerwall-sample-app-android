package com.sponsorpay.sdk.android.publisher;

public interface SPOfferBannerListener {
	public void onSPOfferBannerAvailable(OfferBanner banner);

	public void onSPOfferBannerNotAvailable(OfferBannerRequest request);

	public void onSPOfferBannerRequestError(OfferBannerRequest request);
}
