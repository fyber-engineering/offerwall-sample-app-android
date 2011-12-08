package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OfferBanner {
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
	
	public static final AdShape SP_AD_SHAPE_320X50 = new AdShape(320, 50, "SP_AD_SHAPE_320X50");
	
	private Context mContext;
	
	private String mBaseUrl;
	
	private String mHtmlContent;
	
	private String[] mCookies;
	
	private View mOfferBannerView;
	
	private AdShape mShape;
	
	public OfferBanner(Context context, String baseUrl, String htmlContent, String[] cookies, AdShape shape) {
		mContext = context;
		mBaseUrl = baseUrl;
		mHtmlContent = htmlContent;
		mCookies = cookies;
		mShape = shape;
	}
	
	public View getBannerView(final Activity hostActivity) {
		if (mOfferBannerView == null) {
			WebView webView = new WebView(mContext);
			webView.loadDataWithBaseURL(mBaseUrl, mHtmlContent, "text/html", "utf-8", null);
			int width = convertDevicePixelsIntoPixelsMeasurement(mShape.getWidth());
			int height = convertDevicePixelsIntoPixelsMeasurement(mShape.getHeight());
			webView.setLayoutParams(new LayoutParams(width, height));
			
			
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					Log.d(InterstitialActivity.class.getSimpleName(), "shouldOverrideUrlLoading: " + url);
					
					if (url.startsWith("sponsorpay://exit")) { // ?status=<statusCode>&url=<url>)url is optional)

						/*
						 * Checking scheme if URL has been provided. If yes, try calling an app that will respond to the
						 * given Uri (most likely, the Market App).
						 */
						String marketUrl = OfferBanner.this.parseURLForProvidedURL(url);
						Log.d(InterstitialActivity.class.getSimpleName(), "Provided (market) url is: " + marketUrl);
						if (marketUrl != null) {
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(marketUrl));
							hostActivity.startActivity(intent);
							// TODO: handle activity not found case (throws an ActivityNotFoundException)
						}

						return true;
					} else {
						return false;
					}
				}
			});
			
			if (mCookies != null && mCookies.length > 0) {
				for (String cookieString : mCookies) {
					getCookieManagerInstance().setCookie(mBaseUrl, cookieString);
				}
			}
			
			mOfferBannerView = webView;
		}
		return mOfferBannerView;
	}

	private CookieManager getCookieManagerInstance() {
		CookieManager instance;
		
		// CookieSyncManager.createInstance() has to be called before we get CookieManager's instance.
		try {
			CookieSyncManager.getInstance();
		} catch (IllegalStateException e) {
			CookieSyncManager.createInstance(mContext);
		}

		instance = CookieManager.getInstance();

		return instance;
	}
	
	private int convertDevicePixelsIntoPixelsMeasurement(float dps) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		int pixels = (int) (dps * scale + 0.5f);
		return pixels;
	}
	
	/**
	 * Extract the provided URL from the exit scheme
	 * 
	 * @param url
	 *            the exit scheme url to parse
	 * @return the extracted, provided & decoded URL
	 */
	private String parseURLForProvidedURL(String url) {
		Uri uri = Uri.parse(url);
		if (uri.getQueryParameter("url") != null) {
			return Uri.decode(uri.getQueryParameter("url"));
		}
		return null;
	}
}
