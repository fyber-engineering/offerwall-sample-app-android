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
import com.sponsorpay.sdk.android.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

/**
 * <p>
 * Provides methods to request and show BrandEngage offers and notifies its
 * listener {@link SPBrandEngageClientStatusListener} of changes in the status
 * of the engagement.
 * </p>
 * 
 * Before requesting offers, make sure the have called the {@link
 * SponsorPay#start(String, String, String, Context)} method. At this point you
 * can determine if offers are available with {@link #requestOffers}.
 * 
 * When the engagement is over you must restart the process, querying if offers
 * are available, before you run any engagement again. To check for new virtual
 * coins earned by the user, given you've set your VCS key with the {@link
 * SponsorPay#start(String, String, String, Context)}, simply set a
 * {@link SPCurrencyServerListener} using
 * {@link #setCurrencyListener(SPCurrencyServerListener)} and it will be
 * notified after a successful engagement.
 * 
 * Note - Offer availability ({@link #requestOffers}) cannot be requested while
 * an engagement is running. Call {@link #canRequestOffers()} if you're not sure
 * that this instance is in a state in which a request for offers is possible.
 * 
 */
public class SPBrandEngageClient {
	
	private static final String TAG = "SPBrandEngageClient";
	
	/**
	 * Singleton instance of {@link SPBrandEngageClient}
	 */
	public static final SPBrandEngageClient INSTANCE = new SPBrandEngageClient();

	private static final String MBE_URL_KEY = "mbe";
	
	private static final String SP_START_ENGAGEMENT = "javascript:Sponsorpay.MBE.SDKInterface.do_start()";
	
	private static final String ABOUT_BLANK = "about:blank";
	
	private static final String SP_REQUEST_OFFER_ANSWER = "requestOffers";
	private static final String SP_NUMBER_OF_OFFERS_PARAMETER_KEY = "n";
                                             
	private static final String SP_REQUEST_START_STATUS = "start";
	private static final String SP_REQUEST_STATUS_PARAMETER_KEY = "status";
	private static final String SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE = "STARTED";
	private static final String SP_REQUEST_STATUS_PARAMETER_ENGAGED = "USER_ENGAGED";
	
	/**
	 * Engagement status key used in {@link SPBrandEngageActivity}
	 */
	public static final String SP_ENGAGEMENT_STATUS = "ENGAGEMENT_STATUS";
	
	/**
	 * Parameter used to denote a successful engagement
	 */
	public static final String SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE = "CLOSE_FINISHED";
	
	/**
	 * Parameter used to denote that the engagement as been interrupted
	 */
	public static final String SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE = "CLOSE_ABORTED";
	
	/**
	 * Parameter used to denote an error in the engagement 
	 */
	public static final String SP_REQUEST_STATUS_PARAMETER_ERROR = "ERROR";
	
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

	private SPBrandEngageClient() {
		mHandler = new Handler();
	}

	/**
	 * Queries the server for BrandEngage offers availability. 
	 * 
	 * Offer
	 * availability cannot be requested while an engagement is running or the
	 * server is currently being queried. Call {@link #canRequestOffers()} if you're not
	 * sure that this instance is in a state in which a request for offers is
	 * possible.
	 * 
	 * @param credentials
	 * 			The credentials that will be used for this query. 
	 * 			@see SPCredentials
	 * 			@see SponsorPay#start(String, String, String, Context)
	 * @param activity
	 * 			The calling activity
	 * @return true if a request is being made, false otherwise
	 */
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

	/**
	 * Starts running an available engagement.
	 * 
	 * @param activity
	 * 			The activity that will be used to show the engagement
	 * 
	 * @return true if the engagement can be started
	 */
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
	
	/**
	 * Closes the current engagement
	 */
	public void closeEngagement() {
		if (mStatus == SPBrandEngageOffersStatus.USER_ENGAGED) {
			changeStatus(SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE);
		} else {
			changeStatus(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE);
		}
	}

	/**
	 * @return true if the client is in a state that allows to request for
	 *         offers, false otherwise
	 */
	public boolean canRequestOffers() {
		return mStatus.canRequestOffers();
	}

	/**
	 * @return true if the client is in a state that allows to start and
	 *         engagement, false otherwise
	 */
	public boolean canStartEngagement() {
		return mStatus.canShowOffers();
	}
	
	private boolean canChangeParameters() {
		return mStatus.canChangeParameters();
	}

	/**
	 * @return true if the client shows a toast notifying the user of a
	 *         successful engagement, false otherwise.
	 * 
	 * @see SPBrandEngageClient#shouldShowRewardsNotification()
	 * @see UIStringIdentifier
	 * @see SponsorPayPublisher#setCustomUIString(UIStringIdentifier, String)
	 */
	public boolean shouldShowRewardsNotification() {
		return mShowRewardsNotification;
	}
	
	/**
	 * Sets if the toast reward message shoul be shown
	 * 
	 * @param mShowRewardsNotification
	 */
	public void setShowRewardsNotification(boolean mShowRewardsNotification) {
		this.mShowRewardsNotification = mShowRewardsNotification;
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
	
	/**
	 * Sets the currency name used in this engagement
	 * 
	 * @param currencyName
	 * 			The currency name that will override the default one
	 * 
	 * @return true if successful in setting the name, false otherwise
	 */
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
	
	/**
	 * Sets the additional custom parameters used for this engagement.
	 * 
	 * @param parameters
	 * 			The additional parameters map
	 * 
	 * @return true if successful in setting the parameters, false otherwise
	 */
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
	
	/**
	 * Sets the Virtual Currency listener that will be notified after a
	 * successful engagement.
	 * 
	 * @param listener
	 *            The listener called after a successful request at SponsorPay's
	 *            virtual currency server
	 * 
	 * @return true if successful in setting the listener, false otherwise
	 */
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
	/**
	 * Sets the {@link SPBrandEngageClientStatusListener} that will be notified about the engagement status.
	 * 
	 * @param listener
	 * 			The {@link SPBrandEngageClientStatusListener}
	 * @return true if successful in setting the listener, false otherwise
	 */
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
		return SponsorPayBaseUrlProvider.getBaseUrl(MBE_URL_KEY);
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

	//�Hack section - don't shop around here
	
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