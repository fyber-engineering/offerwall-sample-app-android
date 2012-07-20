package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sponsorpay.sdk.android.publisher.OfferBanner;
import com.sponsorpay.sdk.android.publisher.OfferBannerRequest;
import com.sponsorpay.sdk.android.publisher.SPOfferBannerListener;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class BannersSettingsFragment extends AbstractSettingsFragment implements
		SPOfferBannerListener {

	private LinearLayout mBannerContainer;
	private ScrollView mRootScrollView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_settings_banners, container, false);
	}

	@Override
	protected String getFragmentTitle() {
		return getResources().getString(R.string.banners);
	}

	@Override
	protected void setValuesInFields() {
	}

	@Override
	protected void bindViews() {
		mBannerContainer = (LinearLayout) findViewById(R.id.banner_container);
		mRootScrollView = (ScrollView) findViewById(R.id.root_scroll_view);
	}

	@Override
	protected void fetchValuesFromFields() {
	}

	@Override
	protected void readPreferences(SharedPreferences prefs) {
	}

	@Override
	protected void storePreferences(Editor prefsEditor) {
	}

	@Override
	public void onSPOfferBannerAvailable(OfferBanner banner) {
		Log.i(OfferBanner.LOG_TAG, "onOfferBannerAvailable called");
		mBannerContainer.removeAllViews();
		mBannerContainer.addView(banner.getBannerView(getActivity()));
	}

	@Override
	public void onSPOfferBannerNotAvailable(OfferBannerRequest bannerRequest) {
		Log.i(OfferBanner.LOG_TAG, "onOfferBannerNotAvailable called");
		mBannerContainer.removeAllViews();
		TextView mMessageView = new TextView(getApplicationContext());
		mMessageView.setText(String.format(
				getString(R.string.banner_not_available),
				bannerRequest.getHttpStatusCode()));
		mBannerContainer.addView(mMessageView);
	}

	@Override
	public void onSPOfferBannerRequestError(OfferBannerRequest bannerRequest) {
		Log.i(OfferBanner.LOG_TAG,
				"onOfferBannerRequestError called. HTTP status code="
						+ bannerRequest.getHttpStatusCode());
		mBannerContainer.removeAllViews();
		TextView mMessageView = new TextView(getApplicationContext());

		Throwable requestException = bannerRequest.getRequestThrownError();
		String errorDescription = StringUtils.EMPTY_STRING;
		if (requestException != null) {
			if (requestException.getClass().isInstance(
					new java.net.UnknownHostException())) {
				errorDescription = getString(R.string.banner_request_error_unknown_host);
			} else {
				errorDescription = String.format("%s",
						requestException.toString());
			}
		}

		mMessageView.setText(String.format(
				getString(R.string.banner_request_error), errorDescription));
		mBannerContainer.addView(mMessageView);
	}

	public void requestBanner(String userId, String currencyName,
			String overridingAppId) {
		fetchValuesFromFields();
		try {

			SponsorPayPublisher.requestOfferBanner(getApplicationContext(), userId, this, null, currencyName,
					overridingAppId);
			scrollToBottom();
			// scrollToBottom();
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(),
					"SponsorPay SDK Exception: ", ex);
		}
	}

	private void scrollToBottom() {
		mRootScrollView.fullScroll(View.FOCUS_DOWN);
	}
}
