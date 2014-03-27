package com.sponsorpay.mediation.interstitial;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.sponsorpay.mediation.helper.YuMeConfigurationsHelper;
import com.yume.android.sdk.YuMeAdBlockType;
import com.yume.android.sdk.YuMeException;

//most of this class code is "borrowed" from YuMe sample application 
public class YuMeActivity extends Activity {

	/** relative Layout that contains the frame layout and video view */
	private RelativeLayout rLayout = null;

	/** Display Metrics object */
	DisplayMetrics displayMetrics = new DisplayMetrics();

	/** frame layout that holds the video view */
	FrameLayout fLayout = null;

	/** combined height of status bar and title bar */
	static int STATUS_BAR_AND_TITLE_BAR_HEIGHT = 0;

	/** Delay timer */
	private Timer delayTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);

		super.onCreate(savedInstanceState);

		/* Create the screen Layout that holds the Frame Layout and VideoView */
		rLayout = new RelativeLayout(this);
		if (rLayout != null) {
			ViewGroup.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT);
			if (rLayoutParams != null) {
				rLayout.setLayoutParams(rLayoutParams);
			}
		}

		/* Create the fLayout */
		fLayout = new FrameLayout(this);
		if (fLayout != null) {
			ViewGroup.LayoutParams fLayoutParams = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT);
			if (fLayoutParams != null) {
				fLayout.setLayoutParams(fLayoutParams);
			}
		}

		/* request for title bar icon */
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(rLayout);

		YuMeConfigurationsHelper.getYuMeInterstitialAdapter().setYuMeActivity(
				this);
		/* create the display view */
		createDisplayView();

		/*
		 * this delay timer is started in order to make sure that status bar and
		 * title bar height is calculated correctly
		 */
		startDelayTimer();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		YuMeConfigurationsHelper.getYuMeInterstitialAdapter().fireImpression();
	}

	@Override
	public void onBackPressed() {
		YuMeConfigurationsHelper.getYuMeInterstitialAdapter()
				.backButtonPressed();
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	public void finish() {
		YuMeConfigurationsHelper.getYuMeInterstitialAdapter().notifyClose();
		super.finish();
	}

	/**
	 * Resizes the ad layout.
	 */
	void resizeAdLayout() {
		/* get the available width and height for ad display */
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(displayMetrics);
		int displayWidth = displayMetrics.widthPixels;
		int displayHeight = displayMetrics.heightPixels;

		/*
		 * set / modify the rLayout padding to make the frame layout positioned
		 * properly, in case of non-full screen parentview for ad display
		 */
		if (rLayout != null) {
			rLayout.setPadding(0, 0, 0, 0);
		}

		/* set / modify the Frame Layout params */
		if (fLayout != null) {
			ViewGroup.LayoutParams fLayoutParams1 = fLayout.getLayoutParams();
			if (fLayoutParams1 == null) {
				fLayoutParams1 = new FrameLayout.LayoutParams(displayWidth,
						displayHeight - STATUS_BAR_AND_TITLE_BAR_HEIGHT);
			} else {
				fLayoutParams1.width = displayWidth;
				fLayoutParams1.height = displayHeight
						- STATUS_BAR_AND_TITLE_BAR_HEIGHT;
			}
			fLayout.setLayoutParams(fLayoutParams1);
		}
	}

	/**
	 * Creates the Display View.
	 */
	private void createDisplayView() {
		/* remove existing views from layout, if any */
		removeViewsFromLayout();

		/* add the views to layout */
		addViewsToLayout();
	}

	/**
	 * Adds views to layout.
	 */
	private void addViewsToLayout() {
		/* add the fLayout to rLayout */
		if (rLayout != null) {
			rLayout.addView(fLayout);
		}
	}

	/**
	 * Removes the views from layout.
	 */
	private void removeViewsFromLayout() {
		/* remove the fLayout from rLayout if added already */
		if (rLayout != null) {
			rLayout.removeView(fLayout);
		}
	}

	/**
	 * Gets the combined height of status bar and title bar.
	 * 
	 * @return The combined height of status bar and title bar.
	 */
	public int getStatusBarAndTitleBarHeight() {
		return STATUS_BAR_AND_TITLE_BAR_HEIGHT;
	}

	/**
	 * Calculates the combined height of status bar and title bar.
	 */
	public void calculateStatusBarAndTitleBarHeight() {
		Rect rect = new Rect();
		Window window = this.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);

		int statusBarHeight = rect.top;
		int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
				.getTop();

		int titleBarHeight = 0;
		if (contentViewTop > 0) {
			titleBarHeight = contentViewTop - statusBarHeight;
		}
		/*
		 * work-around to fix the issue wherein statusBarHeight is always >0
		 * even, if not present
		 */
		if (contentViewTop == 0) {
			statusBarHeight = 0;
		}

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		/*
		 * check if status bar resides at the bottom of the device screen like
		 * in Motorola Xoom
		 */
		if (rect.bottom < display.getHeight()) {
			statusBarHeight = display.getHeight() - rect.bottom;
			STATUS_BAR_AND_TITLE_BAR_HEIGHT = titleBarHeight;
		} else {
			STATUS_BAR_AND_TITLE_BAR_HEIGHT = statusBarHeight + titleBarHeight;
		}
	}

	/**
	 * Creates and starts the Delay timer.
	 */
	private void startDelayTimer() {
		if (delayTimer == null) {
			/* create and start the Delay timer */
			int timeVal = 50; /* ms */
			delayTimer = new Timer();
			delayTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					onDelayTimerExpired();
				}
			}, timeVal);
		}
	}

	/**
	 * Stops the Delay timer.
	 */
	void stopDelayTimer() {
		if (delayTimer != null) {
			delayTimer.cancel();
			delayTimer = null;
		}
	}

	/**
	 * Listener for timer expiry event from Delay timer.
	 */
	void onDelayTimerExpired() {
		/* stop the Delay timer */
		stopDelayTimer();

		/* perform the ad display on UI thread */
		runOnUiThread(displayAdOnUIThread);
	}

	/**
	 * Displays the ad on UI thread.
	 */
	private Runnable displayAdOnUIThread = new Runnable() {
		public void run() {
			/* get the status bar and title bar height */
			calculateStatusBarAndTitleBarHeight();

			/* resize the ad layout */
			resizeAdLayout();
			try {
				YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_SetControlBarToggle(true);
				YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_SetParentView(fLayout);
				YuMeConfigurationsHelper.getYuMeSDKInterface().YuMeSDK_ShowAd(YuMeAdBlockType.PREROLL);
			} catch (YuMeException e) {
				e.printStackTrace();
				finish();
			}
		}
	};

}