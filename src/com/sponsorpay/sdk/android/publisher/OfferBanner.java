/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

/**
 * Encloses an offer advertisement banner returned by the SponsorPay server. Contains the data
 * returned by the server and can generate a banner view to be added to a view hierarchy.
 * 
 */
public class OfferBanner {
	/**
	 * Encloses a shape for an advertisement banner: width, height and string description.
	 */
	public static class AdShape {
		private int mWidth, mHeight;
		private String mDescription;

		protected AdShape(int w, int h, String description) {
			mWidth = w;
			mHeight = h;
			mDescription = description;
		}

		public int getWidth() {
			return mWidth;
		}

		public int getHeight() {
			return mHeight;
		}

		public String getDescription() {
			return mDescription;
		}

		@Override
		public String toString() {
			return getDescription();
		}
	}

	public static final String LOG_TAG = "SPOfferBanner";

	/**
	 * {@link AdShape} defining a 320 x 50 banner shape.
	 */
	public static final AdShape SP_AD_SHAPE_320X50 = new AdShape(320, 50, "SP_AD_SHAPE_320X50");

	/**
	 * Android application context.
	 */
	private Context mContext;

	/**
	 * Base URL used by the banner WebView to load resources referenced from its HTML content.
	 */
	private String mBaseUrl;

	/**
	 * HTML content of the banner.
	 */
	private String mHtmlContent;

	/**
	 * Cookies used by the WebView when requesting referenced resources to the server.
	 */
	private String[] mCookies;

	/**
	 * View responsible for displaying the banner.
	 */
	private View mOfferBannerView;

	/**
	 * {@link AdShape} defining the dimensions of this banner.
	 */
	private AdShape mShape;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Android application context.
	 * @param baseUrl
	 *            Base URL used by the banner WebView to load resources referenced from its HTML
	 *            content.
	 * @param htmlContent
	 *            HTML content of the banner.
	 * @param cookies
	 *            Cookies used by the WebView when requesting referenced resources to the server.
	 * @param shape
	 *            {@link AdShape} defining the dimensions of this banner.
	 */
	public OfferBanner(Context context, String baseUrl, String htmlContent, String[] cookies,
			AdShape shape) {
		mContext = context;
		mBaseUrl = baseUrl;
		mHtmlContent = htmlContent;
		mCookies = cookies;
		mShape = shape;
	}

	/**
	 * Initializes and returns an Android View containing the banner which can be added to a view
	 * hierarchy.
	 * 
	 * @param hostActivity
	 *            The Android activity which contains the view hierarchy to which the banner will be
	 *            added. This activity will be used to launch a new activity if the user taps on the
	 *            banner.
	 * @return A View which can be added to a view hierarchy, contains the banner, and can be tapped
	 *         by the user to launch a new activity.
	 */
	public View getBannerView(final Activity hostActivity) {
		if (mOfferBannerView == null) {
			WebView webView = new WebView(mContext);
			webView.loadDataWithBaseURL(mBaseUrl, mHtmlContent, "text/html", "utf-8", null);
			int width = SponsorPayPublisher.convertDevicePixelsIntoPixelsMeasurement(mShape.getWidth(), mContext);
			int height = SponsorPayPublisher.convertDevicePixelsIntoPixelsMeasurement(mShape.getHeight(), mContext);
			webView.setLayoutParams(new LayoutParams(width, height));

			webView.setWebViewClient(new OfferWebClient() {
				@Override
				protected void onSponsorPayExitScheme(int resultCode, String targetUrl) {
					launchActivityWithUrl(hostActivity, targetUrl);
				}
			});

			SponsorPayPublisher.setCookiesIntoCookieManagerInstance(mCookies, mBaseUrl, mContext);

			mOfferBannerView = webView;
		}
		return mOfferBannerView;
	}
}
