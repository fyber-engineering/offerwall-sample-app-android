/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */
package com.sponsorpay.mediation.interstitial;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.sponsorpay.mediation.YuMeMediationAdapter;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.yume.android.sdk.YuMeAdBlockType;
import com.yume.android.sdk.YuMeAdEvent;
import com.yume.android.sdk.YuMeAppInterface;
import com.yume.android.sdk.YuMeException;
import com.yume.android.sdk.YuMeParentViewInfo;

public class YuMeInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<YuMeMediationAdapter> implements YuMeAppInterface {

	public static YuMeInterstitialMediationAdapter INTERSTITIAL;
	public static YuMeMediationAdapter ADAPTER;
	private boolean mAdPlaying = false;
	private Activity yumeActivity;
	
	public YuMeInterstitialMediationAdapter(YuMeMediationAdapter adapter, Activity activity) {
		super(adapter);
		mActivityRef = new WeakReference<Activity>(activity);
		INTERSTITIAL = this;
		ADAPTER = mAdapter;
		
	}

	@Override
	protected boolean show(Activity parentActivity) {
//		try {
//			FrameLayout frameLayout = new FrameLayout(parentActivity);
////			frameLayout.setOnKeyListener(new OnKeyListener() {
////				@Override
////				public boolean onKey(View v, int keyCode, KeyEvent event) {
////					Log.e("EVENT", "=========\n=========\n=========\n=========\n=========\n=========\n=========\n=========\n=========\n");
////					if (keyCode == KeyEvent.KEYCODE_BACK) { //Back key pressed
////				       	try {
////							mAdapter.getYuMeSDKInterface().YuMeSDK_BackKeyPressed();
////						} catch (YuMeException e) {
////							e.printStackTrace();
////						}
////				        return true;
////					} else if (keyCode == KeyEvent.KEYCODE_HOME) {
////						try {
////							mAdapter.getYuMeSDKInterface().YuMeSDK_StopAd();
////						} catch (YuMeException e) {
////							e.printStackTrace();
////						}
////						return true;
////					}
////					return false;
////				}
////			});
//			frameLayout.setBackgroundColor(Color.BLACK);
//			parentActivity.addContentView(frameLayout, new LayoutParams(
//					LayoutParams.FILL_PARENT,
//					LayoutParams.FILL_PARENT));
//			mAdapter.getYuMeSDKInterface().YuMeSDK_SetParentView(frameLayout);
//			mAdapter.getYuMeSDKInterface().YuMeSDK_ShowAd(YuMeAdBlockType.PREROLL);
//			return true;
//		} catch (YuMeException e) {
//			e.printStackTrace();
//		}
//		return false;
//		YuMeDialog dialog = new YuMeDialog(parentActivity);
//		dialog.setOnDismissListener(new OnDismissListener() {
//			@Override
//			public void onDismiss(DialogInterface dialog) {
//				fireCloseEvent();
//			}
//		});
//		return dialog.showAd();
		
		
		
		//SPMediationConfigurator.INSTANCE.
		mAdPlaying = false;
		Intent intent = new Intent(parentActivity, YuMeActivity.class);
		parentActivity.startActivityForResult(intent, 325641);
		return true;
	}

	@Override
	protected void checkForAds(Context context) {
	}

	//YuMeAppInterface implementation
	@Override
	public void YuMeApp_EventListener(YuMeAdBlockType adBlockType, YuMeAdEvent adEvent,
			String eventInfo) {
		switch (adEvent) {
		case AD_READY:
			if (adBlockType == YuMeAdBlockType.PREROLL) {
				Log.d("Event", "AD READY (Preroll)");
			}
			break;
		case AD_AND_ASSETS_READY:
			if (adBlockType == YuMeAdBlockType.PREROLL) {
				Log.d("Event", "AD AND ASSETS_READY (Preroll)");
			}
			setAdAvailable();
			break;
		case AD_NOT_READY:
			if (adBlockType == YuMeAdBlockType.PREROLL) {
				Log.d("Event", "AD NOT READY (Preroll).");
			}
			break;
		case AD_PRESENT:
			Log.d("Event", "AD PRESENT");
			break;
		case AD_PLAYING:
			Log.d("Event", "AD PLAYING");
			mAdPlaying = true;
			break;
		case AD_ABSENT:
			Log.d("Event", "AD ABSENT");
			break;
		case AD_COMPLETED:
			Log.d("Event", "AD COMPLETED");
			mAdPlaying = false;
			// Application can play ad content
//			notifyClose();
			close();
//			setAdAvailable();
			break;
		case AD_ERROR:
			Log.d("Event", "AD ERROR");
			Log.e("Error", "Error Info: " + eventInfo);
			fireShowErrorEvent(eventInfo);
			try {
				mAdapter.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
			} catch (YuMeException e) {
				e.printStackTrace();
			}
			break;
		case AD_EXPIRED:
			Log.d("Event", "AD EXPIRED");
			// Application can call YuMeSDK_InitAd() to prefetch another ad. If
			// YuMeSDK_InitAd() is not called, the SDK will do an auto-prefetch
			// after the first YuMeSDK_ShowAd() call following AD_EXPIRED. No
			// ad will be served for this particular YuMeSDK_ShowAd() call.
			
			fireShowErrorEvent(eventInfo);
			try {
				mAdapter.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
			} catch (YuMeException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public Context YuMeApp_GetActivityContext() {
		return getYumEActivity();
	}

	private Activity getYumEActivity() {
		return yumeActivity;
	}
	
	public void setYuMeActivity( Activity activity) {
		yumeActivity = activity;
	}

	@Override
	public Context YuMeApp_GetApplicationContext() {
		if (getActivity() != null) {
			return getActivity().getApplicationContext();
		}
		return null;
	}

	@Override
	public YuMeParentViewInfo YuMeApp_GetParentViewInfo() {
		Context appContext =  YuMeApp_GetApplicationContext();
		Log.e("EVENT", "=========\n=========\n=========\n=========\n=========\n=========\n=========\n=========\n=========\n");
		if (appContext != null) {
			Log.e("EVENT", "=========\n=========\n=========\n=========\n=========\n=========\n=========\n=========\n=========\n");
			Display display = ((WindowManager) appContext
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			DisplayMetrics displayMetrics = new DisplayMetrics();
			display.getMetrics(displayMetrics);
			YuMeParentViewInfo parentViewInfo = new YuMeParentViewInfo();
			parentViewInfo.width = displayMetrics.widthPixels;
			parentViewInfo.height = displayMetrics.heightPixels;
			parentViewInfo.left = 0;
			parentViewInfo.top = 0;
			 parentViewInfo.statusBarAndTitleBarHeight = 0;
			// STATUS_BAR_AND_TITLE_BAR_HEIGHT;
			return parentViewInfo;
		}
		return null;
	}

	public void close() {
		getActivity().finishActivity(325641);
	}
	
	public void notifyClose() {
		if (mAdPlaying) {
			try {
				mAdapter.getYuMeSDKInterface().YuMeSDK_StopAd();
				mAdapter.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
			} catch (YuMeException e) {
				e.printStackTrace();
			}
		}
		fireCloseEvent();
		setAdAvailable();
	}

	public void impression() {
		fireImpressionEvent();
	}

	public void backButton() {
		try {
			mAdapter.getYuMeSDKInterface().YuMeSDK_BackKeyPressed();
			mAdPlaying = false;
			mAdapter.getYuMeSDKInterface().YuMeSDK_InitAd(YuMeAdBlockType.PREROLL);
			//	close();
		} catch (YuMeException e) {
			e.printStackTrace();
		}
	}
	
//	@Override
//	public void backButtonPressed() {
//		backButton();
//	}
//	
//	@Override
//	public void activityOnPause() {
////		super.activityOnPause();
//		notifyClose();
//	}
			
//	public class YuMeActivity extends Activity {
//		
//		@Override
//		protected void onCreate(Bundle savedInstanceState) {
//			super.onCreate(savedInstanceState);
//			requestWindowFeature(Window.FEATURE_NO_TITLE);
//			FrameLayout frameLayout = new FrameLayout(this);
//			frameLayout.setBackgroundColor(Color.BLACK);
//			setContentView(frameLayout, new LayoutParams(
//					LayoutParams.FILL_PARENT,
//					LayoutParams.FILL_PARENT));
//			try {
//				mAdapter.getYuMeSDKInterface().YuMeSDK_SetParentView(frameLayout);
//				mAdapter.getYuMeSDKInterface().YuMeSDK_ShowAd(YuMeAdBlockType.PREROLL);
//			} catch (YuMeException e) {
//				e.printStackTrace();
//				finish();
//			}
//		}
//		
//		@Override
//		protected void onPostCreate(Bundle savedInstanceState) {
//			super.onPostCreate(savedInstanceState);
//			fireImpressionEvent();
//		}
//		
//		@Override
//		public void onBackPressed() {
//			try {
//				mAdapter.getYuMeSDKInterface().YuMeSDK_BackKeyPressed();
//			} catch (YuMeException e) {
//				e.printStackTrace();
//			}
//			super.onBackPressed();
//		}
//
//		@Override
//		protected void onPause() {
//			try {
//				mAdapter.getYuMeSDKInterface().YuMeSDK_StopAd();
//			} catch (YuMeException e) {
//				e.printStackTrace();
//			}
//			fireCloseEvent();
//			super.onPause();
//		}
//	}
	
	/*private class YuMeDialog extends Dialog {

		public YuMeDialog(Context context) {
			super(context);
		}
		
		@Override
		public void onBackPressed() {
	       	try {
				mAdapter.getYuMeSDKInterface().YuMeSDK_BackKeyPressed();
			} catch (YuMeException e) {
				e.printStackTrace();
			}
//	        return true;
//		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
//			try {
//				mAdapter.getYuMeSDKInterface().YuMeSDK_StopAd();
//			} catch (YuMeException e) {
//				e.printStackTrace();
//			}	
			super.onBackPressed();
		}
		public boolean showAd() {
			FrameLayout layout = new FrameLayout(getContext());
			layout.setBackgroundColor(getContext().getResources().getColor(android.R.color.black));
			getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(layout);
			
			try {
				mAdapter.getYuMeSDKInterface().YuMeSDK_SetParentView(layout);
				mAdapter.getYuMeSDKInterface().YuMeSDK_ShowAd(YuMeAdBlockType.PREROLL);
				fireImpressionEvent();
				show();
				return true;
			} catch (YuMeException e) {
				e.printStackTrace();
				return false;
			}
			
//			if (!adResult.hasError()) {
//				DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
//				
//				int dpValue = 15; // margin in dips
//				float d = displayMetrics.density;
//				int margin = (int)(dpValue * d); 
//
//				View adView = adResult.getView();
//				adView.setId(3);
//
//				LayoutParams adViewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//				adViewLayoutParams.setMargins(margin, margin, margin, margin);
//
//				int dpi = displayMetrics.densityDpi;
//				
//				String path = "";
//				if (dpi > 160 && dpi < 240 ) {
//					path = "-hdpi";
//				} else if (dpi < 320) {
//					path = "-xhdpi";
//				} else {
//					path = "-xxhdpi";
//				}
//				
//				ImageView imgView = new ImageView(getContext());
//				imgView.setId(1);
//				InputStream is = getClass().getResourceAsStream("/com/appia/res/drawable" + path + "/close_active.png");
//				imgView.setImageDrawable(Drawable.createFromStream(is, ""));
//				LayoutParams imgViewLayoutParams = new LayoutParams((int)(2 * dpValue * d),(int)(2 * dpValue * d));
//				imgViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.ALIGN_PARENT_RIGHT);
//				
//				imgView.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						dismiss();
//					}
//				});
//				
//				FrameLayout touchFrame = new FrameLayout(getContext());
//				LayoutParams frameLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//				frameLayoutParams.addRule(RelativeLayout.BELOW, 1);
//				frameLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, adView.getId());
//				frameLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, adView.getId());
//				frameLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, adView.getId());
//				touchFrame.setOnTouchListener(new OnTouchListener() {
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						v.postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								fireClickEvent();
//								dismiss();
//							}
//						}, 200);
//						return false;
//					}
//				});
//				
//				layout.addView(adView, adViewLayoutParams);
//				layout.addView(imgView, imgViewLayoutParams);
//				layout.addView(touchFrame, frameLayoutParams);
//			}

		}
    }*/
	
}
