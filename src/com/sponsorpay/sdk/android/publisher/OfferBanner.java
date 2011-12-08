package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

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
			
			webView.setWebViewClient(new OfferWebClient() {
				@Override
				protected void onSponsorPayExitScheme(int resultCode, String targetUrl) {
					launchActivityWithUrl(hostActivity, targetUrl);
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
}
