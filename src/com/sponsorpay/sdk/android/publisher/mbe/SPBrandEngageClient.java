/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.OfferWebClient;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClientStatusListener.SPBrandEngageClientStatus;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class SPBrandEngageClient {
	
	private static final String TAG = "SPBrandEngageClient";
	public static final SPBrandEngageClient INSTANCE = new SPBrandEngageClient();

	private static final String MBE_BASE_URL = "https://iframe.sponsorpay.com/mbe";
	private static final String MBE_STAGING_BASE_URL = "https://staging-iframe.sponsorpay.com/mbe";
	
	private static final String SP_START_ENGAGEMENT = "javascript:Sponsorpay.MBE.SDKInterface.do_start()";
	
	private static final String ABOUT_BLANK = "about:blank";
	
	private static final String SP_REQUEST_OFFER_ANSWER = "requestOffers";
	private static final String SP_NUMBER_OF_OFFERS_PARAMETER_KEY = "n";
                                             
	private static final String SP_REQUEST_START_STATUS = "start";
	private static final String SP_REQUEST_STATUS_PARAMETER_KEY = "status";
	private static final String SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE = "STARTED";
	private static final String SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE = "CLOSE_FINISHED";
	private static final String SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE = "CLOSE_ABORTED";
	private static final String SP_REQUEST_STATUS_PARAMETER_ERROR = "ERROR";
	private static final String SP_REQUEST_STATUS_PARAMETER_ENGAGED = "USER_ENGAGED";
	
	private static final int TIMEOUT = 10000 ;

	private Activity mActivity;
	private Context mContext;
	private WebView mWebView;
	private Handler mHandler;
	
	private boolean mShowingDialog = false;

	private String mCurrency;
	private Map<String, String> mCustomParameters;
	
	private boolean mShowRewardsNotification = true;

	private SPBrandEngageOffersStatus mStatus = SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS;

	private SPCurrencyServerListener mVCSListener;
	private SPBrandEngageClientStatusListener mStatusListener;
	
	private WebViewClient mWebClient;
	private WebChromeClient mChromeClient;	
	private OnTouchListener mOnTouchListener;
	
	private BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isConnected = !intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			if (!isConnected && mStatus == SPBrandEngageOffersStatus.SHOWING_OFFERS) {
				// show error dialog
				SponsorPayLogger.e(TAG, "Connection has been lost");
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						showErrorDialog(SponsorPayPublisher
								.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_OFFLINE));
					}
				});
			}
		}
	};

	private String mOverridingUrl;
	
	private SPBrandEngageClient() {
		mHandler = new Handler();
	}

	public boolean requestOffers(SPCredentials credentials, Activity activity) {
		if (canRequestOffers()) {
			if (Build.VERSION.SDK_INT < 8) {
				//always return no offers
				processQueryOffersResponse(0);
			} else {
				if (mWebView == null) {
					setupWebView(activity);
				}
				startQueryingOffers(credentials);
			}
			return true;
		} else {
			SponsorPayLogger.d(TAG, "SPBrandEngageClient cannot request offers at this point. " +
					"It might be requesting offers right now or an offer might be currently " +
					"being presented to the user.");
			return false;
		}
	}
	
	private void startQueryingOffers(SPCredentials credentials) {
		String requestUrl = UrlBuilder.newBuilder(getBaseUrl(), credentials)
				.setCurrency(mCurrency).addExtraKeysValues(mCustomParameters)
				.addScreenMetrics().buildUrl();
		SponsorPayLogger.d(TAG, "Loading URL: " + requestUrl);
		mWebView.loadUrl(requestUrl);
		setClientStatus(SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS);
		Runnable timeout = new Runnable() {
			@Override
			public void run() {
				if (mStatus == SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS) {
					SponsorPayLogger.d(TAG, "Timeout reached, canceling request...");
					clearWebViewPage();
				}
			}
		};
		mHandler.postDelayed(timeout, TIMEOUT);
	}

	public boolean startEngagement(Activity activity) {
		if (canStartEngagement()) {

			mWebView.loadUrl(SP_START_ENGAGEMENT);

			mActivity = activity;
			mActivity.addContentView(mWebView, new LayoutParams(
		            LayoutParams.FILL_PARENT,
		            LayoutParams.FILL_PARENT));
		
			checkEngagementStarted();
			return true;
		} else {
			SponsorPayLogger.d(TAG,	"SPBrandEngageClient is not ready to show offers. " +
					"Call requestOffers() and wait until your listener is called with the" +
					" confirmation that offers have been received.");
			return false;
		}
	}
	
	public void closeEngagement() {
		if (mStatus == SPBrandEngageOffersStatus.USER_ENGAGED) {
			changeStatus(SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE);
		} else {
			changeStatus(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE);
		}
	}

	public boolean canRequestOffers() {
		return mStatus.canRequestOffers();
	}

	public boolean canStartEngagement() {
		return mStatus.canShowOffers();
	}
	
	private boolean canChangeParameters() {
		return mStatus.canChangeParameters();
	}

	public boolean shouldShowRewardsNotification() {
		return mShowRewardsNotification;
	}
	
	private void processQueryOffersResponse(int numOffers) {
		boolean areOffersAvailable = numOffers > 0;
		if (areOffersAvailable) {
			setClientStatus(SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS);
		} else {
			setClientStatus(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
		}
		if (mStatusListener != null) {
			mStatusListener.didReceiveOffers(areOffersAvailable);
		}
	}

	private void changeStatus(String status) {
		if (status.equals(SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE)) {
			setClientStatus(SPBrandEngageOffersStatus.SHOWING_OFFERS);
			notifyListener(SPBrandEngageClientStatus.STARTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE)) {
			clearWebViewPage();
			notifyListener(SPBrandEngageClientStatus.CLOSE_FINISHED);
			showRewardsNotification();
			checkForCoins();
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE)) {
			clearWebViewPage();
			notifyListener(SPBrandEngageClientStatus.CLOSE_ABORTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ERROR)) {
			showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ENGAGED)) {
			setClientStatus(SPBrandEngageOffersStatus.USER_ENGAGED);
		}
	}
	
	private void clearWebViewPage() {
		if (mWebView != null) {
			mWebView.loadUrl(ABOUT_BLANK);
		}
		if (mStatus == SPBrandEngageOffersStatus.SHOWING_OFFERS
				|| mStatus == SPBrandEngageOffersStatus.USER_ENGAGED
				|| mStatus == SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS) {
			mContext.unregisterReceiver(mNetworkStateReceiver);
		}
		mWebView = null;
		mActivity = null;
		setClientStatus(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
	}

	private void checkEngagementStarted() {
		Runnable r = new Runnable() {		
			@Override
			public void run() {
				if (mStatus != SPBrandEngageOffersStatus.SHOWING_OFFERS &&
						mStatus != SPBrandEngageOffersStatus.USER_ENGAGED) {
					//something went wrong, show error dialog message
					showErrorDialog(SponsorPayPublisher
							.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
				}
			}

		};
		mHandler.postDelayed(r, TIMEOUT);
	}

	public void setShowRewardsNotification(boolean mShowRewardsNotification) {
		this.mShowRewardsNotification = mShowRewardsNotification;
	}
	
	public boolean setCurrencyName(String currencyName) {
		if (canChangeParameters()) {
			mCurrency = currencyName;
			setClientStatus(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
			return true;
		} else {
			SponsorPayLogger.d(TAG, "Cannot change the currency while a request to the " +
					"server is going on or an offer is being presented to the user.");
			return false;
		}
	}
	
	public boolean setCustomParameters(Map<String, String> parameters) {
		if (canChangeParameters()) {
			mCustomParameters = parameters;
			setClientStatus(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
			return true;
		} else {
			SponsorPayLogger.d(TAG, "Cannot change custom parameters while a request to the " +
					"server is going on or an offer is being presented to the user.");
			return false;
		}
	}
	
	public boolean setCurrencyListener(SPCurrencyServerListener listener) {
		if (canChangeParameters()) {
			mVCSListener = listener;
			setClientStatus(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
			return true;
		} else {
			SponsorPayLogger.d(TAG, "Cannot change the currency listener while a request to the " +
					"server is going on or an offer is being presented to the user.");
			return false;
		}
	}
	
	// Status Listener
	public boolean setStatusListener(SPBrandEngageClientStatusListener listener) {
		boolean canChangeParameters = canChangeParameters();
		if (canChangeParameters) {
			this.mStatusListener = listener;
		} else {
			SponsorPayLogger.d(TAG, "Cannot change the status listener while a request to the " +
					"server is going on or an offer is being presented to the user.");
		}
		return canChangeParameters;
	}

	private void notifyListener(SPBrandEngageClientStatus status) {
		if (mStatusListener != null) {
			SponsorPayLogger.i(TAG, "SPBrandEngageClientStatus -> " + status);
			mStatusListener.didChangeStatus(status);
		}
	}
	
	// Helper methods
	private void setupWebView(Activity activity) {
		mContext = Build.VERSION.SDK_INT < 11 ? activity : activity.getApplicationContext();
		
		mWebView = new WebView(mContext);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);

		mWebView.getSettings().setUseWideViewPort(false);
		
		mWebView.setBackgroundColor(0);
		
		if (Build.VERSION.SDK_INT < 14) {
			mWebView.getSettings().setUserAgent(1);
		}
		
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		
		mWebView.setWebChromeClient(getWebChromeClient());
		
		mWebView.setWebViewClient(getWebClient());

		mWebView.setOnTouchListener(getOnTouchListener());
		
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mNetworkStateReceiver, filter);
	}

	private void showErrorDialog(String message) {
		if (!mShowingDialog && mWebView != null) {
			mShowingDialog = true;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity == null ? mContext: mActivity);
			dialogBuilder.setTitle(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_TITLE)).setMessage(message).
				setNeutralButton(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_BUTTON_TITLE_DISMISS), 
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						notifyListener(SPBrandEngageClientStatus.ERROR);
						clearWebViewPage();
						mShowingDialog = false;
					}
				});
			dialogBuilder.show();
		}
	}
	
	private String getBaseUrl() {
		if (StringUtils.notNullNorEmpty(mOverridingUrl)) {
			return mOverridingUrl;
		}
		return SponsorPayPublisher.shouldUseStagingUrls() ? MBE_STAGING_BASE_URL : MBE_BASE_URL;
	}
	
	private void setClientStatus(SPBrandEngageOffersStatus newStatus) {
		mStatus = newStatus;
		SponsorPayLogger.d(TAG, "SPBrandEngageClient mStatus -> " + newStatus.name());
	}
	
	private void showRewardsNotification() {
		if (mShowRewardsNotification) {
			Toast.makeText(mContext,
					SponsorPayPublisher
					.getUIString(UIStringIdentifier.MBE_REWARD_NOTIFICATION),
					Toast.LENGTH_LONG).show();
		}
	}
	
	private void checkForCoins() {
		if (mVCSListener != null) {
			//delaying it for 10 seconds
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						SponsorPayPublisher.requestNewCoins(SponsorPay
								.getCurrentCredentials().getCredentialsToken(),
								mContext, mVCSListener, null, null, mCurrency);
					} catch (RuntimeException e) {
						SponsorPayLogger.e(TAG, "Error in VCS request", e);
					}
				}
			}, TIMEOUT);
		}
	}
	

	private WebViewClient getWebClient() {
		if (mWebClient == null) {
				
			mWebClient = new OfferWebClient(mActivity) {
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.contains("youtube.com")) {
						SponsorPayLogger.d(TAG, "Preventin the opening of Youtube app");
						return true;
					} else {
						return super.shouldOverrideUrlLoading(view, url);
					}
				}
				
				@Override
				protected void processSponsorPayScheme(String host, Uri uri) {
					if (host.equals(SP_REQUEST_OFFER_ANSWER)) {
						processQueryOffersResponse(Integer.parseInt(uri
								.getQueryParameter(SP_NUMBER_OF_OFFERS_PARAMETER_KEY)));
					} else if (host.equals(SP_REQUEST_START_STATUS)) {
						changeStatus(uri.getQueryParameter(SP_REQUEST_STATUS_PARAMETER_KEY));
					}
				}
				
				@Override
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					SponsorPayLogger.d(TAG, "onReceivedError url - " + failingUrl + " - " + description );
					// show error dialog
					showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
					super.onReceivedError(view, errorCode, description, failingUrl);
				}
				
				@Override
				protected void onTargetActivityStart(String targetUrl) {
					changeStatus(SP_REQUEST_STATUS_PARAMETER_ENGAGED);
					notifyListener(SPBrandEngageClientStatus.PENDING_CLOSE);
				}
				
				@Override
				protected void onPlayStoreNotFound() {
					showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.ERROR_PLAY_STORE_UNAVAILABLE));
				}
				
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
				protected Activity getHostActivity() {
					return mActivity;
				}
			};
		
		}
		return mWebClient;
	}

	private WebChromeClient getWebChromeClient() {
		if (mChromeClient == null) {
			mChromeClient = new WebChromeClient() {
				@Override
				public boolean onJsConfirm(WebView view, String url,
						String message, JsResult result) {
					showJSDialog(url, message);
					result.cancel();
					return true;
				}
				
				private void showJSDialog(String url, String message) {
					if (!mShowingDialog ) {
						mShowingDialog = true;
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity == null ? mContext: mActivity);
						dialogBuilder.setTitle(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_FORFEIT_DIALOG_TITLE)).setMessage(message).
						setPositiveButton("OK", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								changeStatus(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE);
								mShowingDialog = false;
							}
						}).setNegativeButton("Cancel", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mShowingDialog = false;
							}
						}).setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								mShowingDialog = false;
							}
						});
						dialogBuilder.show();
					}
				}
			};
			
		}
		return mChromeClient;
	}
		
	private OnTouchListener getOnTouchListener() {
		if (mOnTouchListener == null) {
			OnDoubleTapListener doubleTapListener = new OnDoubleTapListener() {
				
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					return false; //Nothing
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					//consume the double tap
					SponsorPayLogger.d(TAG, "double tap event");
					return true;
				}

				@Override
				public boolean onDoubleTapEvent(MotionEvent e) {
					//consume the double tap
					SponsorPayLogger.d(TAG, "double tap event");
					return true;
				}
			};
			
			final GestureDetector gestureDetector = new GestureDetector(new OnGestureListener() {
				
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return false;
				}
				
				@Override
				public void onShowPress(MotionEvent e) {
				}
				
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
						float distanceY) {
					return false;
				}
				
				@Override
				public void onLongPress(MotionEvent e) {
				}
				
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						float velocityY) {
					return false;
				}
				
				@Override
				public boolean onDown(MotionEvent e) {
					return false;
				}
			});
			gestureDetector.setOnDoubleTapListener(doubleTapListener);
			
			mOnTouchListener = new OnTouchListener() {		
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return gestureDetector.onTouchEvent(event);
				}
			};
		}
		
		return mOnTouchListener;
	}

	public void setOverridingURl(String overridingUrl) {
		this.mOverridingUrl = overridingUrl;
	}
	
	//ÊHack section - don't shop around here
	
	public void onPause() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
			    if (mWebView != null) {
			    	try {
						Class.forName("android.webkit.WebView")
								.getMethod("onPause", (Class[]) null)
								.invoke(mWebView, (Object[]) null);
					} catch (Exception exception) {
						SponsorPayLogger.e(TAG, "onPause error", exception);
					}
			    }
			}
		});
	}
	
}