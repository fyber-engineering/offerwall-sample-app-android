package com.sponsorpay.sdk.android.publisher.mbe;

import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClientStatusListener.SPBrandEngageClientStatus;
import com.sponsorpay.sdk.android.utils.IntentHelper;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPBrandEngageClient {
	
	private enum SPBrandEngageOffersStatus {
	    MUST_QUERY_SERVER_FOR_OFFERS,
	    QUERYING_SERVER_FOR_OFFERS,
	    READY_TO_SHOW_OFFERS,
	    SHOWING_OFFERS,
	    USER_ENGAGED
	} 
	
	private static final String TAG = "SPBrandEngageClient";
	public static final SPBrandEngageClient INSTANCE = new SPBrandEngageClient();

	private static final String MBE_BASE_URL = "https://iframe.sponsorpay.com/mbe";
	private static final String MBE_STAGING_BASE_URL = "https://staging-iframe.sponsorpay.com/mbe";
	
	private static final String SP_START_ENGAGEMENT = "javascript:Sponsorpay.MBE.SDKInterface.do_start()";
	
	private static final String ABOUT_BLANK = "about:blank";
	
	private static final String SP_REQUEST_OFFER_ANSWER = "requestOffers";
	private static final String SP_NUMEBER_OF_OFFERS_PARAMETER_KEY = "n";
                                             
	private static final String SP_REQUEST_START_STATUS = "start";
	private static final String SP_REQUEST_STATUS_PARAMETER_KEY = "status";
	private static final String SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE = "STARTED";
	private static final String SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE = "CLOSE_FINISHED";
	private static final String SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE = "CLOSE_ABORTED";
	private static final String SP_REQUEST_STATUS_PARAMETER_ERROR = "ERROR";
	private static final String SP_REQUEST_STATUS_PARAMETER_ENGAGED = "USER_ENGAGED";
	
	private static final String SP_REQUEST_EXIT = "exit";
	private static final String SP_REQUEST_URL_PARAMETER_KEY = "url";
	
	private static final int TIMEOUT = 10000 ;
	
	private SPBrandEngageOffersStatus mStatus = SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS;

	private SPBrandEngageClientStatusListener mStatusListener;

	private Context mContext;
	private WebView mWebView;
	private Handler mHandler;
	
	private String mCurrency;
	
	private boolean mShowRewardsNotification = true;
	
	private SPCurrencyServerListener mVCSListener;
	
	private Map<String, String> mCustomParameters;
//	private FrameLayout mLayout;
	private boolean mShowingDialog = false;
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
	private Activity mActivity;

	
	private SPBrandEngageClient() {
		mHandler = new Handler();
	}

	public boolean requestOffers(SPCredentials credentials, Activity activity) {
		if (canRequestOffers()) {
			if (mWebView == null) {
				setupWebView(activity);
			}
			
			startQueryingOffers(credentials);
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
		setStatusClient(SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS);
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

	public boolean canRequestOffers() {
		return mStatus != SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS &&
				mStatus != SPBrandEngageOffersStatus.SHOWING_OFFERS;
	}
	
	public void closeEngagement() {
		if (mStatus == SPBrandEngageOffersStatus.USER_ENGAGED) {
			changeStatus(SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE);
		} else {
			changeStatus(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE);
		}
//		clearWebViewPage();
	}
	
	private void processQueryOffersResponse(int numOFfers) {
		boolean areOffersAvaliable = numOFfers > 0;
		if (areOffersAvaliable) {
			setStatusClient(SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS);
		} else {
			setStatusClient(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
		}
		if (mStatusListener != null) {
			mStatusListener.didReceiveOffers(areOffersAvaliable);
		}
	}

	private void changeStatus(String status) {
		if (status.equals(SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE)) {
			setStatusClient(SPBrandEngageOffersStatus.SHOWING_OFFERS);
			notitfyListener(SPBrandEngageClientStatus.STARTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE)) {
			clearWebViewPage();
			notitfyListener(SPBrandEngageClientStatus.CLOSE_FINISHED);
			checkForCoins();
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE)) {
			clearWebViewPage();
			notitfyListener(SPBrandEngageClientStatus.CLOSE_ABORTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ERROR)) {
			showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ENGAGED)) {
			setStatusClient(SPBrandEngageOffersStatus.USER_ENGAGED);
		}
	}

	private void notitfyListener(SPBrandEngageClientStatus status) {
		if (mStatusListener != null) {
			mStatusListener.didChangeStatus(status);
		}
	}
	
	private void clearWebViewPage() {
		if (mWebView != null) {
			mWebView.loadUrl(ABOUT_BLANK);
		}
		if (mStatus == SPBrandEngageOffersStatus.SHOWING_OFFERS
				|| mStatus == SPBrandEngageOffersStatus.USER_ENGAGED) {
			mContext.unregisterReceiver(mNetworkStateReceiver);
			mWebView = null;
		}
//		mLayout = null;
		mActivity = null;
		setStatusClient(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
	}

	
	public boolean startEngament(Activity activity) {
		if (canStartEngagement()) {
//			mLayout = layout;
//			
//			layout.addView(mWebView);
//			mWebView.setLayoutParams(new LayoutParams(
//	            LayoutParams.FILL_PARENT,
//	            LayoutParams.FILL_PARENT));
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

	private void checkEngagementStarted() {
		Runnable r = new Runnable() {		
			@Override
			public void run() {
				if (mStatus != SPBrandEngageOffersStatus.SHOWING_OFFERS) {
					//something went wrong, show close button
					showCloseButton();
				}
			}

		};
		
		mHandler.postDelayed(r, TIMEOUT);
	}

	private void showCloseButton() {
		showErrorDialog(SponsorPayPublisher
				.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
	}

	public boolean canStartEngagement() {
		return mStatus == SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS;
	}
	
	private boolean canChangeParameters() {
		return mStatus == SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS
				|| mStatus == SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS;
	}

	public boolean isShowRewardsNotification() {
		return mShowRewardsNotification;
	}

	public void setShowRewardsNotification(boolean mShowRewardsNotification) {
		this.mShowRewardsNotification = mShowRewardsNotification;
	}
	
	public boolean setCurrencyName(String currencyName) {
		if (canChangeParameters()) {
			mCurrency = currencyName;
			setStatusClient(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
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
			setStatusClient(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
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
			setStatusClient(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
			return true;
		} else {
			SponsorPayLogger.d(TAG, "Cannot change the currency listener while a request to the " +
					"server is going on or an offer is being presented to the user.");
			return false;
		}
	}
	
	
	// Status Listener
	
	public void setStatusListener(SPBrandEngageClientStatusListener listener) {
		if (canChangeParameters()) {
			this.mStatusListener = listener;
		}
	}
	
	// Helper methods
	
	private void setupWebView(Activity activity) {
		//TODO check this for android version!!
		mContext = Build.VERSION.SDK_INT < 11 ? activity : activity.getApplicationContext();
//		mContext = activity;
		
		mWebView = new WebView(mContext);

//		mWebView = new WebView(mContext) {
//			
//		@Override
//		public void addView(View child) {
//			Log.e(TAG, "addView 1- "+ child.getClass().getCanonicalName());
//			super.addView(child);
//		}
//		
//		@Override
//		public void addView(View child, int index) {
//			Log.e(TAG, "addView 2- "+ child.getClass().getCanonicalName());
//			super.addView(child, index);
//		}
//		@Override
//		public void addView(View child, int index,
//				android.view.ViewGroup.LayoutParams params) {
//			Log.e(TAG, "addView 3- "+ child.getClass().getCanonicalName());
//			super.addView(child, index, params);
//		}
//		@Override
//		public void addView(View child, int width, int height) {
//			Log.e(TAG, "addView 4- "+ child.getClass().getCanonicalName());
//			super.addView(child, width, height);
//		}
//		@Override
//		public void addView(View child,
//				android.view.ViewGroup.LayoutParams params) {
//			Log.e(TAG, "addView 5- "+ child.getClass().getCanonicalName());
//			super.addView(child, params);
//		}
//		
//		@Override
//		protected boolean addViewInLayout(View child, int index,
//				android.view.ViewGroup.LayoutParams params) {
//			Log.e(TAG, "addViewInLayout - "+ child.getClass().getCanonicalName());
//			return super.addViewInLayout(child, index, params);
//		}
//		
//		@Override
//		protected boolean addViewInLayout(View child, int index,
//				android.view.ViewGroup.LayoutParams params,
//				boolean preventRequestLayout) {
//			Log.e(TAG, "addViewInLayout - "+ child.getClass().getCanonicalName());
//			return super.addViewInLayout(child, index, params, preventRequestLayout);
//		}
//		
//		@Override
//		protected void attachViewToParent(View child, int index,
//				android.view.ViewGroup.LayoutParams params) {
//			Log.e(TAG, "attachViewToParent - "+ child.getClass().getCanonicalName());
//			super.attachViewToParent(child, index, params);
//		}
//		
//		@Override
//		protected boolean drawChild(Canvas canvas, View child,
//				long drawingTime) {
//			Log.e(TAG, "drawChild - "+ child.getClass().getCanonicalName());
//			return super.drawChild(canvas, child, drawingTime);
//		}
//	};
		
		
		
		mWebView.setId(new Random().nextInt());
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);
		
		//TODO check this
		if (Build.VERSION.SDK_INT < 14) {
			mWebView.getSettings().setUserAgent(1);
		}
		
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		
		mWebView.setWebChromeClient(new WebChromeClient() {
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
//					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mLayout == null ? mContext: mLayout.getContext());
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity == null ? mContext: mActivity);
					dialogBuilder.setTitle(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_FORFEIT_DIALOG_TITLE)).setMessage(message).
						setPositiveButton("OK", 
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
//								notitfyListener(SPBrandEngageClientStatus.CLOSE_ABORTED);
//								clearWebViewPage();
								changeStatus(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE);
								mShowingDialog = false;
							}
						}).setNegativeButton("Cancel", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mShowingDialog = false;
						}
					});
					dialogBuilder.show();
				}
			}
		});
		
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				SponsorPayLogger.d(TAG, "URL -> " + url);
				if (url.startsWith("sponsorpay")) {
					Uri uri = Uri.parse(url);
					String host = uri.getHost();
					if (host.equals(SP_REQUEST_EXIT)) {
						String targetUrl = uri.getQueryParameter(SP_REQUEST_URL_PARAMETER_KEY);
						processExitUrl(targetUrl);
					} else if (host.equals(SP_REQUEST_OFFER_ANSWER)) {
						processQueryOffersResponse(Integer.parseInt(uri
								.getQueryParameter(SP_NUMEBER_OF_OFFERS_PARAMETER_KEY)));
					} else if (host.equals(SP_REQUEST_START_STATUS)) {
						changeStatus(uri.getQueryParameter(SP_REQUEST_STATUS_PARAMETER_KEY));
					}
					return true;
				}
				return false;
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				SponsorPayLogger.d(TAG, "onReceivedError url - " + failingUrl + " - " + description );
				// show error dialog
				showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
			
		});
		
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mNetworkStateReceiver, filter);
			
		
	}
	
	private void processExitUrl(String targetUrl) {
		if (targetUrl != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			Uri uri = Uri.parse(targetUrl);
			intent.setData(uri);
			try {
				mActivity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				if (uri.getScheme().equalsIgnoreCase("market") && !IntentHelper.isIntentAvailable(mContext,
						Intent.ACTION_VIEW, 
						// dummy search to validate Play Store application
						Uri.parse("market://search?q=pname:com.google"))) {
					SponsorPayLogger.e(TAG, "Play Store is not installed on this device...");
					showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.ERROR_PLAY_STORE_UNAVAILABLE));
				}
			}
		}
	}
	
	private void showErrorDialog(String message) {
		if (!mShowingDialog && mWebView != null) {
			mShowingDialog = true;
//			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mLayout == null ? mContext: mLayout.getContext());
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity == null ? mContext: mActivity);
			dialogBuilder.setTitle(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_TITLE)).setMessage(message).
				setNeutralButton(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_BUTTON_TITLE_DISMISS), 
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						notitfyListener(SPBrandEngageClientStatus.ERROR);
						clearWebViewPage();
						mShowingDialog = false;
					}
				});
			dialogBuilder.show();
		}
		
	}
	
	private String getBaseUrl() {
		return SponsorPayPublisher.shouldUseStagingUrls() ? MBE_STAGING_BASE_URL : MBE_BASE_URL;
	}
	
	private void setStatusClient(SPBrandEngageOffersStatus newStatus) {
		mStatus = newStatus;
		SponsorPayLogger.d(TAG, "SPBrandEngageClient mStatus -> " + newStatus.name());
	}
	
	private void checkForCoins() {
		if (mVCSListener != null) {
			if (mShowRewardsNotification) {
				Toast.makeText(mContext,
						SponsorPayPublisher
								.getUIString(UIStringIdentifier.MBE_REWARD_NOTIFICATION),
						Toast.LENGTH_LONG).show();
			}
			//delaying it for 10 seconds
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						SponsorPayPublisher.requestNewCoins(mContext, mVCSListener);
					} catch (RuntimeException e) {
						SponsorPayLogger.e(TAG, "Error in VCS request", e);
					}
				}
			}, TIMEOUT);
		}
	}
	
}