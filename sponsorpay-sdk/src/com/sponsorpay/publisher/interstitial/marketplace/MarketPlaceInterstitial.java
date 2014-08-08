/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.interstitial.marketplace;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;


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
	private FrameLayout mainLayout;

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

//		FrameLayout frameLayout = new FrameLayout(parentActivity);
//		frameLayout.setBackgroundColor(Color.MAGENTA);
//		parentActivity.setContentView(frameLayout, new LayoutParams(
//				LayoutParams.FILL_PARENT,
//				LayoutParams.FILL_PARENT));
		FrameLayout.LayoutParams layoutparams= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);            
		parentActivity.addContentView(mainLayout, layoutparams);

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

		mainLayout = new FrameLayout(context);
		
		//RelativeLayout childLayout = new RelativeLayout(context);
		RelativeLayout childLayout = new RelativeLayout(context);

		//Image with drawable
		final ImageView imageView;
		imageView = new ImageView(context);
		// create a drawable
		ShapeDrawable circle = new ShapeDrawable(new OvalShape());
		circle.setIntrinsicHeight(80);
		circle.setIntrinsicWidth(80);
		circle.setBounds(new Rect(0, 0, 40, 40));
		circle.getPaint().setColor(Color.DKGRAY);
		// set the drawable into the imageviw
		imageView.setImageDrawable(circle);
		imageView.setAdjustViewBounds(true);
		imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		imageView.setPadding(5, 5, 5, 5);
		imageView.setScaleType(ScaleType.FIT_XY);
		
		

		// X close text
		TextView text = new TextView(context);
		text.setText("X");
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		text.setTextColor(Color.WHITE);
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		text.setLayoutParams(params);
		
		
		childLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP|Gravity.RIGHT));
		childLayout.addView(imageView);
		childLayout.addView(text);
		
		//the webview
		mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		mainLayout.addView(mWebView);
		mainLayout.addView(childLayout);
		//mainLayout.addView(text);

	     
	     
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
	     
	     
		mWebView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
                fireClickEvent();
				
				if (mainLayout != null) {
		            ViewGroup parentViewGroup = (ViewGroup) mainLayout.getParent();
		            if (parentViewGroup != null) {
		                parentViewGroup.removeAllViews();
		            }
		        }
				return true;
			}
		});
		
		 
	}

}
