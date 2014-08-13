/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial.marketplace;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.sponsorpay.mediation.marketplace.MarketPlaceAdapter;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.utils.SPWebClient;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

public class MarketPlaceInterstitial extends
		SPInterstitialMediationAdapter<MarketPlaceAdapter> {
	
	private static final String TAG = "MarketPlaceInterstitial";
    private static final int closeButtonGreyColor = Color.parseColor("#7F7F7F");
	
	protected static final int CREATE_WEBVIEW = 0;
	protected static final int LOAD_HTML = 1;
	private Handler mMainHandler;
	private WebView mWebView;
	private WebViewClient mWebClient;
	private FrameLayout mainLayout;
	private DisplayMetrics metrics;

	public MarketPlaceInterstitial(MarketPlaceAdapter adapter) {
		super(adapter);
		mMainHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CREATE_WEBVIEW:
					MessageInfoHolder holder = (MessageInfoHolder) msg.obj;
					
					mWebView = new WebView(holder.mContext);
					
					metrics = holder.mContext.getResources().getDisplayMetrics();
					createCloseButton(holder.mContext);
					
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
		//mActivity = parentActivity;

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
					launchActivityWithUrl(targetUrl);
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
					SponsorPayLogger.e(TAG, String.format(
							"Interstitials WebView triggered an error. "
									+ "Error code: %d, error description: %s. Failing URL: %s",
							errorCode, description, failingUrl));

					UIStringIdentifier error;

					switch (errorCode) {
					case ERROR_HOST_LOOKUP:
					case ERROR_IO:
						error = UIStringIdentifier.ERROR_LOADING_OFFERWALL_NO_INTERNET_CONNECTION;
						break;
					default:
						error = UIStringIdentifier.ERROR_LOADING_OFFERWALL;
						break;
					}
					showDialog(SponsorPayPublisher.getUIString(error));
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

		mainLayout = new FrameLayout(context);
		
		RelativeLayout childLayout = new RelativeLayout(context);
		
		int fifteenDip = getPixelsFromDip(15);
		int thirtyDip  = getPixelsFromDip(30);
		int sixtyDip   = getPixelsFromDip(60);

		//Image with drawable
		final ImageView imageView;
		imageView = new ImageView(context);
		
		// create a circle with diameter 40X40 dip and set background color
		ShapeDrawable circle = new ShapeDrawable(new OvalShape());
		circle.setIntrinsicHeight(thirtyDip);
	    circle.setIntrinsicWidth(thirtyDip);
		circle.getPaint().setColor(closeButtonGreyColor);
		
		// set the drawable into the center of the imageview
		//and set 5 dip padding on each side.
		imageView.setImageDrawable(circle);
		imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ScaleType.CENTER);
		imageView.setPadding(fifteenDip, fifteenDip, fifteenDip, fifteenDip);
		
		

		DrawCloseXView drawView = new DrawCloseXView(context);
		drawView.setBackgroundColor(closeButtonGreyColor);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(fifteenDip, fifteenDip);
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		drawView.setLayoutParams(params);
		
		
		childLayout.setLayoutParams(new FrameLayout.LayoutParams(sixtyDip, sixtyDip, Gravity.TOP|Gravity.RIGHT));
		childLayout.addView(imageView);
		childLayout.addView(drawView);
		
		
		//the webview
		mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		mainLayout.addView(mWebView);
		mainLayout.addView(childLayout);
	     
	     
	     /**
	      * Listeners for calling the close and click events.
	      * 
	      */
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				fireCloseEvent();
				
				if (mainLayout != null) {
		            ViewGroup parentViewGroup = (ViewGroup) mainLayout.getParent();
		            if (parentViewGroup != null) {
		                parentViewGroup.removeAllViews();
		            }
		        }
			}
		});
	 
	}
	
	public int getPixelsFromDip(int dip) {
		return  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
	}
}
