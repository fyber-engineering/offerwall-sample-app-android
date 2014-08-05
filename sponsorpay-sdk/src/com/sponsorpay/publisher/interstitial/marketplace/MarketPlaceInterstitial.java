/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial.marketplace;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;

import com.sponsorpay.mediation.marketplace.MarketPlaceAdapter;
import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SPWebClient;
import com.sponsorpay.utils.StringUtils;

public class MarketPlaceInterstitial extends
		SPInterstitialMediationAdapter<MarketPlaceAdapter> {

	protected static final int CREATE_WEBVIEW = 0;
	protected static final int LOAD_HTML = 1;
	private Handler mMainHandler;
	private WebView mWebView;
	private WebViewClient mWebClient;

	private Activity mActivity;

	public MarketPlaceInterstitial(MarketPlaceAdapter adapter) {
		super(adapter);
		mMainHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CREATE_WEBVIEW:
					MessageInfoHolder holder = (MessageInfoHolder) msg.obj;
					mWebView = new WebView(holder.mContext);
					mWebView.getSettings().setJavaScriptEnabled(true);
					mWebView.setWebViewClient(getWebClient());	
//					loadHtml(holder.mHtml);
					msg.obj = holder.mHtml;
//					break;
				case LOAD_HTML:
//					mWebView.loadData(msg.obj.toString(), "text/html", "UTF-8");
					mWebView.loadDataWithBaseURL("http://engine.sponsorpay.com", msg.obj.toString(), null, "UTF-8", null);
					break;
				default:
					break;
				}
			}
		};
	}

	@Override
	public boolean isAdAvailable(Context context, SPInterstitialAd ad) {
		String htmlContent = ad.getContextData().get("html");
		boolean hasHtml = StringUtils.notNullNorEmpty(htmlContent);
		if (hasHtml) {
			if (mWebView == null) {
				Message msg = Message.obtain(mMainHandler);
				msg.what = CREATE_WEBVIEW;
				msg.obj = new MessageInfoHolder(context, htmlContent);
				msg.sendToTarget();
			} else {
				loadHtml(htmlContent);
			}
			setAdAvailable();
		}
		return hasHtml;
	}
	
	private void loadHtml(String html) {
		Message msg = Message.obtain(mMainHandler);
		msg.what = LOAD_HTML;
		msg.obj = html;
		msg.sendToTarget();
	}
	
	@Override
	protected boolean show(Activity parentActivity) {
		mActivity = parentActivity;
//		FrameLayout frameLayout = new FrameLayout(parentActivity);
//		frameLayout.setBackgroundColor(Color.MAGENTA);
//		parentActivity.setContentView(frameLayout, new LayoutParams(
//				LayoutParams.FILL_PARENT,
//				LayoutParams.FILL_PARENT));
		parentActivity.addContentView(mWebView, new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		return true;
	}

	@Override
	protected void checkForAds(Context context) {
		//do nothing
	}
	
	
	private WebViewClient getWebClient() {
		if (mWebClient == null) {
				
			mWebClient = new SPWebClient(null) {

				@Override
				protected void processSponsorPayScheme(String host, Uri uri) {
				}

				@Override
				protected void onSponsorPayExitScheme(int resultCode,
						String targetUrl) {
				}

				@Override
				protected void onTargetActivityStart(String targetUrl) {
				}
				
				@Override
				protected Activity getHostActivity() {
					return mActivity;
				}
				
			};
		
		}
		return mWebClient;
	}

	private class MessageInfoHolder {
		private Context mContext;
		private String mHtml;

		private MessageInfoHolder(Context context, String html) {
			this.mContext = context;
			this.mHtml = html;
			
		}
	}
}
