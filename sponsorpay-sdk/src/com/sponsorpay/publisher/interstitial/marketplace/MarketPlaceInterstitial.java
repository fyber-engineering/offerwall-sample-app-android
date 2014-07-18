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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.sponsorpay.mediation.marketplace.MarketPlaceAdapter;
import com.sponsorpay.publisher.interstitial.SPInterstitialActivity;
import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.interstitial.marketplace.view.InterstitialCloseButtonRelativeLayout;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SPWebClient;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

public class MarketPlaceInterstitial extends
		SPInterstitialMediationAdapter<MarketPlaceAdapter> implements MarketPlaceInterstitialActivityListener, OnClickListener{
	
	private static final String TAG = "MarketPlaceInterstitial";
	
	protected static final int CREATE_WEBVIEW = 0;
	protected static final int LOAD_HTML = 1;
	
	private Handler       mMainHandler;
	private WebView       mWebView;
	private WebViewClient mWebClient;
	private FrameLayout   mainLayout;
	private Activity      mActivity;
	private InterstitialCloseButtonRelativeLayout closeButtonLayout;

	public MarketPlaceInterstitial(MarketPlaceAdapter adapter) {
		super(adapter);
		
		mMainHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CREATE_WEBVIEW:
					
					MessageInfoHolder holder = (MessageInfoHolder) msg.obj;
										
					mWebView = new WebView(holder.mContext);
					
					createCloseButton(holder.mContext);
					
					mWebView.getSettings().setJavaScriptEnabled(true);
					mWebView.setWebViewClient(getWebClient());	
					
					msg.obj = holder.mHtml;
					
				case LOAD_HTML:
					
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
		removeAttachedLayout();
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
			fireImpressionEvent();
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
		
		if(mActivity instanceof SPInterstitialActivity) {
			mActivity = parentActivity;
			((SPInterstitialActivity) mActivity).setMarketPlaceInterstitialListener(MarketPlaceInterstitial.this);
		}
		
		FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);            
		parentActivity.setContentView(mainLayout, layoutparams);
		
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
				protected void onSponsorPayExitScheme(int resultCode, String targetUrl) {
					Activity hostActivity = getHostActivity();

					if (null == hostActivity) {
						return;
					}

					hostActivity.setResult(resultCode);
					fireClickEvent();
					launchActivityWithUrl(targetUrl);
				}

				@Override
				protected Activity getHostActivity() {
					return mActivity;
				}
				
				@Override
				protected void processSponsorPayScheme(String host, Uri uri) {
					// nothing more to do, everything is done by super class
				}

				@Override
				protected void onTargetActivityStart(String targetUrl) {
					// nothing to do 
				}
				
				
				@Override
				public void onReceivedError(WebView view, int errorCode, String description,
						String failingUrl) {
					
					String errorMessage = String.format(
							"Interstitials WebView triggered an error. "
									+ "Error code: %d, error description: %s. Failing URL: %s",
							errorCode, description, failingUrl);
					
					SponsorPayLogger.e(TAG, errorMessage);

					fireShowErrorEvent(errorMessage);
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
	
	private void createCloseButton(Context context){

		//main FrameLayout which will host the webview and the close button
		mainLayout  = new FrameLayout(context);
		
		//Instance of the close button relative layout, which will be generated dynamically
		closeButtonLayout = new InterstitialCloseButtonRelativeLayout(context);
		
		
		//the webview
		mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		//attach on the main layout the webview and the close button.
		mainLayout.addView(mWebView);
		mainLayout.addView(closeButtonLayout);
  
	     
	    // Set a listener for the close button
		closeButtonLayout.setOnClickListener(this);
	 
	}
	
	/**
	 * Is removing all the views from the dynamically generated marketplace
	 * interstitials.
	 */
	private void removeAttachedLayout(){
		if (mainLayout != null) {
            ViewGroup parentViewGroup = (ViewGroup) mainLayout.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
	}

	/**
	 * Callback methods overriding the functionality of the back and
	 * home buttons.
	 */
	@Override
	public void notifyOnBackPressed() {
		fireCloseEvent();
		removeAttachedLayout();
	}

	@Override
	public void notifyOnHomePressed() {
		fireCloseEvent();
		removeAttachedLayout();
	}

	/**
	 * Listener which will be called when the close button will be clicked.
	 * It will fire the close event and remove the interstitial layout.
	 */
	@Override
	public void onClick(View v) {
		fireCloseEvent();
		removeAttachedLayout();
	}

}
