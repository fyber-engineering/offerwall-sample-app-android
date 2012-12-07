package com.sponsorpay.sdk.android.publisher.mbe;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sponsorpay.sdk.android.UrlBuilder;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerAbstractResponse;
import com.sponsorpay.sdk.android.publisher.currency.CurrencyServerDeltaOfCoinsResponse;
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
	
	public static final SPBrandEngageClient INSTANCE = new SPBrandEngageClient();

	private SPBrandEngageOffersStatus status = SPBrandEngageOffersStatus.MUST_QUERY_SERVER_FOR_OFFERS;

	private SPBrandEngageClientStatusListener mStatusListener;

	private Context mContext;
	private WebView mWebView;
	private Handler mHandler;
	private ViewGroup mGroup;
	
	private boolean mCheckForRewardAfterCompletion = true;
	private boolean mShowRewardsNotification = true;
	
	private SPBrandEngageClient() {
		mHandler = new Handler();
	}

	public boolean requestOffers(SPCredentials credentials, Activity activity, ViewGroup group) {
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
				.buildUrl();
		SponsorPayLogger.d(TAG, "Loading URL: " + credentials);
		mWebView.loadUrl(requestUrl);
		setStatusClient(SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS);
		Runnable timeout = new Runnable() {
			@Override
			public void run() {
				if (status == SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS) {
					SponsorPayLogger.d(TAG, "Timeout reached, canceling request...");
					clearWebViewPage();
				}
			}
		};
		//TODO make timeout configurable
		mHandler.postDelayed(timeout, 5000);
	}

	private boolean canRequestOffers() {
		return status != SPBrandEngageOffersStatus.QUERYING_SERVER_FOR_OFFERS &&
				status != SPBrandEngageOffersStatus.SHOWING_OFFERS;
	}
	
	public void cancelEngagement() {
		clearWebViewPage();
		//FIRE close_aborted OR close_finished, depending on the status
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
		if (mGroup != null) {
			mGroup.removeView(mWebView);
		}
	}

	private void changeStatus(String status) {
		if (status.equals(SP_REQUEST_STATUS_PARAMETER_STARTED_VALUE)) {
			setStatusClient(SPBrandEngageOffersStatus.SHOWING_OFFERS);
			notitfyListener(SPBrandEngageClientStatus.STARTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_FINISHED_VALUE)) {
			clearWebViewPage();
			notitfyListener(SPBrandEngageClientStatus.CLOSE_FINISHED);
			if (mShowRewardsNotification) {
				Toast.makeText(
						mContext,
						SponsorPayPublisher
								.getUIString(UIStringIdentifier.MBE_REWARD_NOTIFICATION),
						Toast.LENGTH_LONG).show();
			}
			if (mCheckForRewardAfterCompletion) {
					checkForCoins();
			}
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ABORTED_VALUE)) {
			clearWebViewPage();
			notitfyListener(SPBrandEngageClientStatus.CLOSE_ABORTED);
		} else if (status.equals(SP_REQUEST_STATUS_PARAMETER_ERROR)) {
			clearWebViewPage();
			notitfyListener(SPBrandEngageClientStatus.ERROR);
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
			mWebView.setLayoutParams(new LayoutParams(
	            LayoutParams.FILL_PARENT,
	            LayoutParams.FILL_PARENT));
			layout.addView(mWebView);
		
			mWebView.loadUrl(SP_START_ENGAGEMENT);
			//TODO check if timeout required here
			return true;
		} else {
			SponsorPayLogger.d(TAG,	"SPBrandEngageClient is not ready to show offers. " +
					"Call -requestOffers: and wait until your delegate is called with the" +
					" confirmation that offers have been received.");
			return false;
		}
	}

	public boolean canStartEngagement() {
		return status == SPBrandEngageOffersStatus.READY_TO_SHOW_OFFERS;
	}


	public boolean isCheckForRewardAfterCompletion() {
		return mCheckForRewardAfterCompletion;
	}

	public void setCheckForRewardAfterCompletion(
			boolean mCheckForRewardAfterCompletion) {
		this.mCheckForRewardAfterCompletion = mCheckForRewardAfterCompletion;
	}

	public boolean isShowRewardsNotification() {
		return mShowRewardsNotification;
	}

	public void setShowRewardsNotification(boolean mShowRewardsNotification) {
		this.mShowRewardsNotification = mShowRewardsNotification;
	}
	
	// Status Listener
	
	public void setStatusListener(SPBrandEngageClientStatusListener listener) {
		this.mStatusListener = listener;
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
				SponsorPayLogger.d(TAG, "onReceivedError");
				//TODO show error dialog with native close button
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
			
		});
	}
	
	private String getBaseUrl() {
		return SponsorPayPublisher.shouldUseStagingUrls() ? MBE_STAGING_BASE_URL : MBE_BASE_URL;
	}
	
	private void setStatusClient(SPBrandEngageOffersStatus newStatus) {
		status = newStatus;
		SponsorPayLogger.d(TAG, "SPBrandEngageClient status -> " + newStatus.name());
	}
	
	private void checkForCoins() {
		//delaying it for 5 seconds
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					SponsorPayPublisher.requestNewCoins(mContext, new SPCurrencyServerListener() {
						@Override
						public void onSPCurrencyServerError(CurrencyServerAbstractResponse response) {
							SponsorPayLogger.e(TAG, "VCS error received - " + response.getErrorMessage() );
						}
						
						@Override
						public void onSPCurrencyDeltaReceived(
								CurrencyServerDeltaOfCoinsResponse response) {
							if (response.getDeltaOfCoins() > 0) {
								String text = String
										.format(SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_COINS_NOTIFICATION),
												response.getDeltaOfCoins(),
												SponsorPayPublisher.getUIString(UIStringIdentifier.MBE_DEFAULT_CURRENCY));
								Toast.makeText(mContext, text,
										Toast.LENGTH_LONG).show();
							}
						}
					});
				} catch (RuntimeException e) {
					SponsorPayLogger.e(TAG, "Error in VCS request", e);
				}
			}
		}, 5000);
	}
	
}