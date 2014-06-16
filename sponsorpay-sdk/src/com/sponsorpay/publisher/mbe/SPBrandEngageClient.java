/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.publisher.mbe;

import java.util.HashMap;
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
import android.os.Looper;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.BadTokenException;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.mediation.SPMediationCoordinator;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.publisher.mbe.SPBrandEngageClientStatusListener.SPBrandEngageClientStatus;
import com.sponsorpay.publisher.mbe.mediation.SPBrandEngageMediationJSInterface;
import com.sponsorpay.publisher.mbe.mediation.SPMediationValidationEvent;
import com.sponsorpay.publisher.mbe.mediation.SPMediationVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoEvent;
import com.sponsorpay.publisher.mbe.mediation.SPTPNVideoValidationResult;
import com.sponsorpay.utils.SPWebClient;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;

/**
 * <p>
 * Provides methods to request and show BrandEngage offers and notifies its
 * listener {@link SPBrandEngageClientStatusListener} of changes in the status
 * of the engagement.
 * </p>
 * 
 * Before requesting offers, make sure the have called the {@link
 * SponsorPay#start(String, String, String, Activity)} method. At this point you
 * can determine if offers are available with {@link #requestOffers}.
 * 
 * When the engagement is over you must restart the process, querying if offers
 * are available, before you run any engagement again. To check for new virtual
 * coins earned by the user, given you've set your VCS key with the {@link
 * SponsorPay#start(String, String, String, Activity)}, simply set a
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
	private static final String SP_JS_NOTIFY = "javascript:Sponsorpay.MBE.SDKInterface.notify";
	
	private static final String ABOUT_BLANK = "about:blank";
	
	private static final String SP_REQUEST_OFFER_ANSWER = "requestOffers";
	private static final String SP_NUMBER_OF_OFFERS_PARAMETER_KEY = "n";
                                             
	private static final String SP_REQUEST_START_STATUS = "start";
	private static final String SP_REQUEST_STATUS_PARAMETER_KEY = "status";
	private static final String SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE = "STARTED";
	private static final String SP_REQUEST_STATUS_PARAMETER_ENGAGED = "USER_ENGAGED";
	
	private static final String SP_REQUEST_VALIDATE = "validate";
	private static final String SP_THIRD_PARTY_NETWORK_PARAMETER = "tpn";
	private static final String SP_THIRD_PARTY_ID_PARAMETER = "id";
	private static final String SP_REQUEST_PLAY = "play";
	
	private static final String KEY_FOR_CLIENT_CUSTOM_PARAMETER    = "client";
	private static final String KEY_FOR_PLATFORM_CUSTOM_PARAMETER  = "platform";
	private static final String KEY_FOR_REWARDED_CUSTOM_PARAMETER  = "rewarded";
	private static final String KEY_FOR_AD_FORMAT_CUSTOM_PARAMETER = "ad_format";
	
	
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
	
	public static final int TIMEOUT = 10000 ;
	
	private static final int VCS_TIMEOUT = 3000 ;

	private static final int VIDEO_EVENT = 1;
	private static final int VALIDATION_RESULT = 2;
	private static final int LOAD_URL = 123;
	private static final int ON_PAUSE = 522;

	private Handler mHandler;
	private Handler mWebViewHandler;
	
	private Activity mActivity;
	private Context mContext;
	private WebView mWebView;
	
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
	
	private IntentFilter mIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
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

	private SPBrandEngageMediationJSInterface mJSInterface;

	private SPBrandEngageClient() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case VALIDATION_RESULT:
					SponsorPayLogger.d(TAG, "Timeout reached, canceling request...");
					processQueryOffersResponse(0);
					break;
				case VIDEO_EVENT:
					//something went wrong, show error dialog message
					showErrorDialog(SponsorPayPublisher
							.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
					break;
				}
			}
		};
		mWebViewHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LOAD_URL:
					if (mWebView != null) {
						String url = msg.obj.toString();
						mWebView.loadUrl(url);
						if (url.equals(ABOUT_BLANK)) {
							mWebView = null;
							mActivity = null;
						}
					}
					break;
				case ON_PAUSE:
					doPauseWebView();
					break;
				default:
					SponsorPayLogger.e(TAG, "Unknow message what field");
					break;
				}
			}
		};
		mJSInterface = new SPBrandEngageMediationJSInterface();
	}

	/**
	 * Queries the server for BrandEngage offers availability. 
	 * 
	 * Offer availability cannot be requested while an engagement is running or the
	 * server is currently being queried. Call {@link #canRequestOffers()} if you're not
	 * sure that this instance is in a state in which a request for offers is
	 * possible.
	 * 
	 * @param credentials
	 * 			The credentials that will be used for this query. 
	 * 			@see SPCredentials
	 * 			@see SponsorPay#start(String, String, String, Activity)
	 * @param activity
	 * 			The calling activity
	 * @return true if a request is being made, false otherwise
	 */
	public boolean requestOffers(SPCredentials credentials, Activity activity) {
		if (canRequestOffers()) {
			if (Build.VERSION.SDK_INT < 9) {
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
				.setCurrency(mCurrency).addExtraKeysValues(mCustomParameters).addKeyValue(KEY_FOR_CLIENT_CUSTOM_PARAMETER, "sdk")
				.addKeyValue(KEY_FOR_PLATFORM_CUSTOM_PARAMETER, "android").addKeyValue(KEY_FOR_REWARDED_CUSTOM_PARAMETER, "1")
				.addKeyValue(KEY_FOR_AD_FORMAT_CUSTOM_PARAMETER, "video").addScreenMetrics().buildUrl();
		SponsorPayLogger.d(TAG, "Loading URL: " + requestUrl);
		loadUrl(requestUrl);
		setClientStatus(SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS);
		
		mHandler.sendEmptyMessageDelayed(VALIDATION_RESULT, TIMEOUT);
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
		return startEngagement(activity, playThroughMediation());
	}
	
	public boolean startEngagement(Activity activity,
			boolean playThroughMediation) {
		if (activity != null) {
			if (canStartEngagement()) {
				
				loadUrl(SP_START_ENGAGEMENT);
				
				mActivity = activity;
				if (!playThroughMediation) {
					mActivity.addContentView(mWebView, new LayoutParams(
							LayoutParams.FILL_PARENT,
							LayoutParams.FILL_PARENT));
					mContext.registerReceiver(mNetworkStateReceiver, mIntentFilter);
				}
			
				checkEngagementStarted();
				return true;
			} else {
				SponsorPayLogger.d(TAG,	"SPBrandEngageClient is not ready to show offers. " +
						"Call requestOffers() and wait until your listener is called with the" +
						" confirmation that offers have been received.");
			}
		} else {
			SponsorPayLogger.d(TAG,	"The provided activity is null, SPBrandEngageClient cannot start the engagement.");
		}
		return false;
	}	
	/**
	 * Closes the current engagement
	 */
	public void closeEngagement() {
		try {
			mContext.unregisterReceiver(mNetworkStateReceiver);
		} catch (IllegalArgumentException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		}
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
	 * @see SPBrandEngageClient#setShowRewardsNotification(boolean)
	 * @see UIStringIdentifier
	 * @see SponsorPayPublisher#setCustomUIString(UIStringIdentifier, String)
	 */
	public boolean shouldShowRewardsNotification() {
		return mShowRewardsNotification;
	}
	
	/**
	 * Sets if the toast reward message should be shown
	 * 
	 * @param mShowRewardsNotification
	 */
	public void setShowRewardsNotification(boolean mShowRewardsNotification) {
		this.mShowRewardsNotification = mShowRewardsNotification;
	}
	
	private void processQueryOffersResponse(int numOffers) {
		mHandler.removeMessages(VALIDATION_RESULT);
		boolean areOffersAvailable = numOffers > 0;
		if (areOffersAvailable) {
			setClientStatus(SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS);
		} else {
			clearWebViewPage();
		}
		if (mStatusListener != null) {
			mStatusListener.didReceiveOffers(areOffersAvailable);
		}
	}

	private void changeStatus(String status) {
		if (status.equals(SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE)) {
			mHandler.removeMessages(VIDEO_EVENT);
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
			loadUrl(ABOUT_BLANK);
		}
		setClientStatus(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
	}
	
	private void loadUrl(String url) {
		if (StringUtils.notNullNorEmpty(url)) {
			Message m = Message.obtain(mWebViewHandler);
			m.what = LOAD_URL;
			m.obj = url;
			m.sendToTarget();
		}
	}

	private void checkEngagementStarted() {
		mHandler.sendEmptyMessageDelayed(VIDEO_EVENT, TIMEOUT);
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

	public boolean playThroughMediation() {
		return mJSInterface.playThroughTirdParty(mWebView);
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
			mWebView.getSettings().setUserAgentString(
							"Mozilla/5.0 (X11; CrOS i686 4319.74.0) " +
							"AppleWebKit/537.36 (KHTML, like Gecko) " +
							"Chrome/29.0.1547.57 " +
							"Safari/537.36 (Sponsorpay SDK)");
		}
		
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		
		mWebView.setWebChromeClient(getWebChromeClient());
		
		mWebView.setWebViewClient(getWebClient());

		mWebView.setOnTouchListener(getOnTouchListener());

		mWebView.addJavascriptInterface(mJSInterface,
				mJSInterface.getInterfaceName());
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
			try {
				dialogBuilder.show();
			} catch (BadTokenException e ) {
				mShowingDialog = false;
				SponsorPayLogger.e(TAG, "Unable to show the dialog window");
			}
		}
	}
	
	private String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(MBE_URL_KEY);
	}
	
	private void setClientStatus(SPBrandEngageOffersStatus newStatus) {
		if (mStatus != newStatus) {
			mStatus = newStatus;
			SponsorPayLogger.d(TAG, "SPBrandEngageClient mStatus -> " + newStatus.name());
		}
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
					if( mVCSListener != null ) {
						try {
							SponsorPayPublisher.requestNewCoins(SponsorPay
									.getCurrentCredentials().getCredentialsToken(),
									mContext, mVCSListener, null, null, mCurrency);
						} catch (RuntimeException e) {
							SponsorPayLogger.e(TAG, "Error in VCS request", e);
						}
					} else {
						SponsorPayLogger.d(TAG, "There's no VCS listener");
					};
				}
			}, VCS_TIMEOUT);
		}
	}
	

	private WebViewClient getWebClient() {
		if (mWebClient == null) {
				
			mWebClient = new SPWebClient(mActivity) {
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.contains("youtube.com")) {
						SponsorPayLogger.d(TAG, "Preventing Youtube app");
						return true;
					} else {
						return super.shouldOverrideUrlLoading(view, url);
					}
				}
				
				@Override
				protected void processSponsorPayScheme(String command, Uri uri) {
					if (command.equals(SP_REQUEST_OFFER_ANSWER)) {
						processQueryOffersResponse(Integer.parseInt(uri
								.getQueryParameter(SP_NUMBER_OF_OFFERS_PARAMETER_KEY)));
					} else if (command.equals(SP_REQUEST_START_STATUS)) {
						changeStatus(uri.getQueryParameter(SP_REQUEST_STATUS_PARAMETER_KEY));
					} else if (command.equals(SP_REQUEST_VALIDATE)) {
						String tpnName = uri.getQueryParameter(SP_THIRD_PARTY_NETWORK_PARAMETER);
						SponsorPayLogger.d(TAG, "MBE client asks to validate a third party network: " + tpnName);
						HashMap<String, String> contextData = new HashMap<String, String>(1);
						contextData.put(SP_THIRD_PARTY_ID_PARAMETER, uri.getQueryParameter(SP_THIRD_PARTY_ID_PARAMETER));
						SPMediationCoordinator.INSTANCE.validateVideoNetwork(mContext, tpnName,
								contextData, new SPMediationValidationEvent() {
							@Override
							public void validationEventResult(String name, SPTPNVideoValidationResult result, Map<String, String> contextData) {
								String url = String.format("%s('validate', {tpn:'%s', id:%s, result:'%s'})", 
										SP_JS_NOTIFY, name, contextData.get(SP_THIRD_PARTY_ID_PARAMETER), result);
								SponsorPayLogger.d(TAG, "Notifying - " + url);
								loadUrl(url);
							}
						});
					} else if (command.equals(SP_REQUEST_PLAY)) {
						String tpnName = uri.getQueryParameter(SP_THIRD_PARTY_NETWORK_PARAMETER);
						HashMap<String, String> contextData = new HashMap<String, String>(1);
						contextData.put(SP_THIRD_PARTY_ID_PARAMETER, uri.getQueryParameter(SP_THIRD_PARTY_ID_PARAMETER));
						SponsorPayLogger.d(TAG, "MBE client asks to play an offer from a third party network:" + tpnName);
						SPMediationCoordinator.INSTANCE.startVideoEngagement(mActivity, tpnName,
								contextData, new SPMediationVideoEvent() {
							@Override
							public void videoEventOccured(String name, SPTPNVideoEvent event,
									Map<String, String> contextData) {
								if (event == SPTPNVideoEvent.SPTPNVideoEventStarted) {
									changeStatus(SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE);
								}
								String url = String.format("%s('play', {tpn:'%s', id:%s, result:'%s'})", 
										SP_JS_NOTIFY, name, contextData.get(SP_THIRD_PARTY_ID_PARAMETER), event);
								SponsorPayLogger.d(TAG, "Notifying - " + url);
								loadUrl(url);
							}
						});
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
					SponsorPayLogger.d(TAG, "js alert - " + message);
					showJSDialog(url, message);
					result.cancel();
					return true;
				}
				
				private void showJSDialog(String url, String message) {
					SponsorPayLogger.d(TAG, "js alert - " + message);
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

	
	//Hack section - don't shop around here
	public void onPause() {
		Message m = Message.obtain(mWebViewHandler);
		m.what = ON_PAUSE;
		m.sendToTarget();
	}
	
	private void doPauseWebView() {
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
}
