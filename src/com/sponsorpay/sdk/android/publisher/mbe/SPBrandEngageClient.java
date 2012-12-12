package com.sponsorpay.sdk.android.publisher.mbe;

import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClientStatusListener.SPBrandEngageClientStatus;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPBrandEngageClient {
	
	private enum SPBrandEngageOffersStatus {
	    MUST_QUERY_SERVER_FOR_OFFERS,
	    QUERYING_SERVER_FOR_OFFERS,
	    READY_TO_SHOW_OFFERS,
	    SHOWING_OFFERS
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
	
	private static final String SP_REQUEST_EXIT = "exit";
	private static final String SP_REQUEST_URL_PARAMETER_KEY = "url";
	
	private static final int TIMEOUT = 10000 ;
	
	private SPBrandEngageOffersStatus mStatus = SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS;

	private SPBrandEngageClientStatusListener mStatusListener;

	private Context mContext;
	private WebView mWebView;
	private Handler mHandler;
//	private ViewGroup mGroup;
	
	private String mCurrency;
	
//	private boolean mCheckForRewardAfterCompletion = true;
	private boolean mShowRewardsNotification = true;
	
	private SPCurrencyServerListener mVCSListener;
	
	private Map<String, String> mCustomParameters;
	private FrameLayout mLayout;
	private boolean mShowingDialog = false;

	
	private SPBrandEngageClient() {
		mHandler = new Handler();
	}

	public boolean requestOffers(SPCredentials credentials, Activity activity) {
//	public boolean requestOffers(SPCredentials credentials, Activity activity, ViewGroup group) {
		if (canRequestOffers()) {
			if (mWebView == null) {
				setupWebView(activity);
			}
			
//			if (mGroup == null && group != null && Build.VERSION.SDK_INT == 7) {
//				mGroup = group;
//				mWebView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
//				mGroup.addView(mWebView);
//				mWebView.setVisibility(View.VISIBLE);
//				mWebView.loadUrl("https://www.google.com");
////				mWebView.loadData("<!DOCTYPE html><html><body><h1>My First Heading</h1><p>My first paragraph.</p></body></html>", "text/html", "UTF-8");
////				clearWebViewPage();
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

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
				.setCurrency(mCurrency).addExtraKeysValues(mCustomParameters).buildUrl();
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

	private boolean canRequestOffers() {
		return mStatus != SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS &&
				mStatus != SPBrandEngageOffersStatus.SHOWING_OFFERS;
	}
	
	public void closeEngagement() {
		clearWebViewPage();
		//FIRE close_aborted OR close_finished, depending on the mStatus
		notitfyListener(SPBrandEngageClientStatus.CLOSE_ABORTED);
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
//		if (mGroup != null) {
//			mGroup.removeView(mWebView);
//		}
	}

	private void changeStatus(String status) {
		if (status.equals(SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE)) {
			setStatusClient(SPBrandEngageOffersStatus.SHOWING_OFFERS);
			notitfyListener(SPBrandEngageClientStatus.STARTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE)) {
			clearWebViewPage();
			notitfyListener(SPBrandEngageClientStatus.CLOSE_FINISHED);
			if (mShowRewardsNotification) {
				Toast.makeText(mContext,
						SponsorPayPublisher
								.getUIString(UIStringIdentifier.MBE_REWARD_NOTIFICATION),
						Toast.LENGTH_LONG).show();
			}
			checkForCoins();
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE)) {
			clearWebViewPage();
			notitfyListener(SPBrandEngageClientStatus.CLOSE_ABORTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ERROR)) {
			showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT));
//			clearWebViewPage();
//			notitfyListener(SPBrandEngageClientStatus.ERROR);
		}
	}

	private void notitfyListener(SPBrandEngageClientStatus status) {
		if (mStatusListener != null) {
			mStatusListener.didChangeStatus(status);
		}
	}
	
	private void clearWebViewPage() {
		mWebView.loadUrl(ABOUT_BLANK);
		setStatusClient(SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS);
	}
	
	public boolean startEngament(FrameLayout layout) {
		if (canStartEngagement()) {
			mLayout = layout;
			
			layout.addView(mWebView);

			mWebView.setLayoutParams(new LayoutParams(
	            LayoutParams.FILL_PARENT,
	            LayoutParams.FILL_PARENT));
		
			mWebView.loadUrl(SP_START_ENGAGEMENT);
			checkEngagementStarted();
			return true;
		} else {
			SponsorPayLogger.d(TAG,	"SPBrandEngageClient is not ready to show offers. " +
					"Call -requestOffers: and wait until your delegate is called with the" +
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
//					showCloseButton();
				}
			}
		};
		
		mHandler.postDelayed(r, TIMEOUT);
	}

	public boolean canStartEngagement() {
		return mStatus == SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS;
	}
	
	private boolean canChangeParameters() {
		return mStatus == SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS
				|| mStatus == SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS;
	}


//	public boolean isCheckForRewardAfterCompletion() {
//		return mCheckForRewardAfterCompletion;
//	}
//
//	public void setCheckForRewardAfterCompletion(
//			boolean mCheckForRewardAfterCompletion) {
//		this.mCheckForRewardAfterCompletion = mCheckForRewardAfterCompletion;
//	}

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
			SponsorPayLogger.d(TAG, "Cannot change custom parameters while a request to the " +
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
		mWebView = new WebView(mContext);

		mWebView.setId(new Random().nextInt());
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginsEnabled(true);
		
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				SponsorPayLogger.d(TAG, "URL -> " + url);
				if (url.startsWith("sponsorpay")) {
					Uri uri = Uri.parse(url);
					String host = uri.getHost();
					if (host.equals(SP_REQUEST_EXIT)) {
						//TODO fix this
						uri.getQueryParameter(SP_REQUEST_URL_PARAMETER_KEY);
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
		
		BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

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
							showErrorDialog(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_OFFLINE));
						}
					});
				}
		    }
		};

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		activity.registerReceiver(networkStateReceiver, filter);
	}
	
	private void showErrorDialog(String message) {
		if (!mShowingDialog ) {
			mShowingDialog = true;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mLayout.getContext());
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