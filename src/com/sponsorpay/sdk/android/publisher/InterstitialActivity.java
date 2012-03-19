/**
 * SponsorPay Android Publisher SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.LinearLayout;

/**
 * <p>
 * Shows the SponsorPay mobile interstitial embedded in a WebView.
 * </p>
 * <p>
 * Will retrieve the interstitial's initial html content from the {@link #EXTRA_INITIAL_CONTENT_KEY}
 * encoded into the calling intent, and will load dependent content referenced with relative links
 * using the base URL encoded as {@link #EXTRA_BASE_URL_KEY} into the calling intent. The http
 * cookie(s) to use will be retrieved from the String array encoded as extra
 * {@link #EXTRA_COOKIESTRINGS_KEY}
 * </p>
 * <p>
 * The boolean value encoded as {@link #EXTRA_SHOULD_STAY_OPEN_KEY} into the calling intent will
 * determine the activity's behavior when the user is redirected outside the application. The
 * default behavior is to close the interstitial.
 * </p>
 */
public class InterstitialActivity extends Activity {
	private static final int INTERSTITIAL_BORDER_SMALLER_DEVICE = 3;
	private static final int INTERSTITIAL_BORDER_BIGGER_DEVICE = 12;

	private static final int BACKGROUND_DRAWABLE_CORNER_RADIUS = 10;
	private static final int BACKGROUND_DRAWABLE_ALPHA = 196;

	public static final String EXTRA_SHOULD_STAY_OPEN_KEY = "EXTRA_SHOULD_REMAIN_OPEN_KEY";
	public static final String EXTRA_INITIAL_CONTENT_KEY = "EXTRA_INITIAL_CONTENT_KEY";

	public static final String EXTRA_BASE_URL_KEY = "EXTRA_INITIAL_BASE_URL";
	public static final String EXTRA_COOKIESTRINGS_KEY = "EXTRA_COOKIESTRINGS";

	private static final int BIGGER_SCREEN_SHORT_SIDE_RESOLUTION_DP = 480;
	private static final int BIGGER_SCREEN_LONG_SIDE_RESOLUTION_DP = 800;

	private static final int SHORT_SIDE_SIZE_FOR_SMALLER_SCREEN_DP = 280;
	private static final int LONG_SIDE_SIZE_FOR_SMALLER_SCREEN_DP = 430;

	private static final int SHORT_SIDE_SIZE_FOR_BIGGER_SCREEN_DP = 380;
	private static final int LONG_SIDE_SIZE_FOR_BIGGER_SCREEN_DP = 690;

	private WebView mWebView;
	private boolean mShouldStayOpen;
	private LinearLayout mWebViewContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String initialInterstitialContent = getIntent().getStringExtra(EXTRA_INITIAL_CONTENT_KEY);
		String baseUrl = getIntent().getStringExtra(EXTRA_BASE_URL_KEY);
		String[] cookieStrings = getIntent().getStringArrayExtra(EXTRA_COOKIESTRINGS_KEY);

		/*
		 * If the Interstitial Activity is launched but no cookies have been passed to it, silently
		 * fail and close the Interstitial instead of letting an exception be thrown.
		 */
		if (cookieStrings == null || cookieStrings.length == 0) {
			finish();
		}
		
		mShouldStayOpen = getIntent().getBooleanExtra(EXTRA_SHOULD_STAY_OPEN_KEY, mShouldStayOpen);
		mWebView = new WebView(InterstitialActivity.this);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

		ViewGroup.LayoutParams interstitialSize = generateLayoutParamsForCurrentDisplay();
		mWebView.setLayoutParams(interstitialSize);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

		mWebViewContainer = new LinearLayout(this);

		int borderWidth = determineInterstitialBorderWidth();
		mWebViewContainer.setPadding(borderWidth, borderWidth, borderWidth, borderWidth);

		mWebView.setBackgroundColor(Color.TRANSPARENT);

		mWebViewContainer.addView(mWebView);
		mWebViewContainer.setBackgroundColor(Color.TRANSPARENT);

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);

		mWebView.setWebViewClient(new ActivityOfferWebClient(InterstitialActivity.this,
				mShouldStayOpen));

		// Add into the CookieManager used by the web view the cookies received as encoded extras.
		SponsorPayPublisher.setCookiesIntoCookieManagerInstance(cookieStrings, baseUrl, this);

		mWebView.loadDataWithBaseURL(baseUrl, initialInterstitialContent, "text/html", "utf-8",
				null);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(mWebViewContainer);
		getWindow().getAttributes().dimAmount = 0.0F;
		getWindow().setBackgroundDrawable(generateBackgroundDrawable());
	}

	private static Drawable generateBackgroundDrawable() {
		float[] cornerRadii = new float[] { BACKGROUND_DRAWABLE_CORNER_RADIUS,
				BACKGROUND_DRAWABLE_CORNER_RADIUS, BACKGROUND_DRAWABLE_CORNER_RADIUS,
				BACKGROUND_DRAWABLE_CORNER_RADIUS, BACKGROUND_DRAWABLE_CORNER_RADIUS,
				BACKGROUND_DRAWABLE_CORNER_RADIUS, BACKGROUND_DRAWABLE_CORNER_RADIUS,
				BACKGROUND_DRAWABLE_CORNER_RADIUS };

		RoundRectShape roundRectBg = new RoundRectShape(cornerRadii, null, null);
		ShapeDrawable roundRectBgDrawable = new ShapeDrawable(roundRectBg);
		roundRectBgDrawable.setAlpha(BACKGROUND_DRAWABLE_ALPHA);

		return roundRectBgDrawable;
	}

	private ViewGroup.LayoutParams generateLayoutParamsForCurrentDisplay() {
		Context context = getApplicationContext();

		int shortInterstitialSideDp, longInterstitialSideDp;

		if (isHostBiggerDevice()) {
			shortInterstitialSideDp = SHORT_SIDE_SIZE_FOR_BIGGER_SCREEN_DP;
			longInterstitialSideDp = LONG_SIDE_SIZE_FOR_BIGGER_SCREEN_DP;
		} else {
			shortInterstitialSideDp = SHORT_SIDE_SIZE_FOR_SMALLER_SCREEN_DP;
			longInterstitialSideDp = LONG_SIDE_SIZE_FOR_SMALLER_SCREEN_DP;
		}

		int interstitialWidthDp, interstitialHeightDp;

		if (isCurrentOrientationPortrait()) {
			interstitialWidthDp = shortInterstitialSideDp;
			interstitialHeightDp = longInterstitialSideDp;
		} else {
			interstitialWidthDp = longInterstitialSideDp;
			interstitialHeightDp = shortInterstitialSideDp;
		}

		int widthPx = SponsorPayPublisher.convertDevicePixelsIntoPixelsMeasurement(
				interstitialWidthDp, context);
		int heightPx = SponsorPayPublisher.convertDevicePixelsIntoPixelsMeasurement(
				interstitialHeightDp, context);

		return new ViewGroup.LayoutParams(widthPx, heightPx);
	}

	/**
	 * Returns whether the host device has a display bigger than the thresholds defined by
	 * {@link #BIGGER_SCREEN_SHORT_SIDE_RESOLUTION_DP} and
	 * {@link #BIGGER_SCREEN_LONG_SIDE_RESOLUTION_DP}
	 */
	private boolean isHostBiggerDevice() {
		Context context = getApplicationContext();

		// Get the size of the display in pixels
		DisplayMetrics display = context.getResources().getDisplayMetrics();
		int displayLongSideResolutionPx = Math.max(display.widthPixels, display.heightPixels);
		int displayShortSideResolutionPx = Math.min(display.widthPixels, display.heightPixels);

		// convert the defined thresholds from device pixels to plain pixels
		int biggerDeviceShortSideResolutionPx = SponsorPayPublisher
				.convertDevicePixelsIntoPixelsMeasurement(BIGGER_SCREEN_SHORT_SIDE_RESOLUTION_DP,
						context);
		int biggerDeviceLongSideResolutionPx = SponsorPayPublisher
				.convertDevicePixelsIntoPixelsMeasurement(BIGGER_SCREEN_LONG_SIDE_RESOLUTION_DP,
						context);

		// compare plain pixels to plain pixels. Both thresholds have to match or exceed
		// for this method to return true.
		boolean shorterSidePasses = displayShortSideResolutionPx >= biggerDeviceShortSideResolutionPx;
		boolean longerSidePasses = displayLongSideResolutionPx >= biggerDeviceLongSideResolutionPx;

		return (shorterSidePasses && longerSidePasses);
	}

	private int determineInterstitialBorderWidth() {
		int worderWidthDP = isHostBiggerDevice() ? INTERSTITIAL_BORDER_BIGGER_DEVICE
				: INTERSTITIAL_BORDER_SMALLER_DEVICE;

		return SponsorPayPublisher.convertDevicePixelsIntoPixelsMeasurement(worderWidthDP,
				getApplicationContext());
	}

	/**
	 * <p>
	 * Returns true if the current orientation of the device is portrait.
	 * </p>
	 * 
	 * <p>
	 * <em>Implementation note:</em> This implementation assumes that DisplayMetrics.heightPixels
	 * and DisplayMetrics.widthPixels get swapped when the orientation changes. This avoids using
	 * the deprecated Display.getOrientation() method as well as the getRotation() method which is
	 * available only from API 8 on.
	 * </p>
	 * 
	 * @return
	 */
	private boolean isCurrentOrientationPortrait() {
		DisplayMetrics display = getApplicationContext().getResources().getDisplayMetrics();
		return display.heightPixels > display.widthPixels;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		ViewGroup.LayoutParams interstitialSize = generateLayoutParamsForCurrentDisplay();
		if (mWebView != null) {
			mWebViewContainer.removeView(mWebView);
			mWebView.setLayoutParams(interstitialSize);
			mWebViewContainer.addView(mWebView);
		}
	}
}
