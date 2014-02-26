/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation.interstitial;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.appia.sdk.AdParameters;
import com.appia.sdk.Appia;
import com.appia.sdk.BannerAdResult;
import com.appia.sdk.BannerAdSize;
import com.sponsorpay.mediation.AppiaMediationAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

public class AppiaInterstitialMediationAdapter extends
		SPInterstitialMediationAdapter<AppiaMediationAdapter>  {

	private static final String PLACEMENT_ID = "placementId";

	private Appia mAppia;
	private AdParameters mAdParameters;
	private BannerAdResult mAdResult;

	public AppiaInterstitialMediationAdapter(AppiaMediationAdapter adapter,
			Appia appia, Activity activity) {
		super(adapter);
		this.mAppia = appia;
		mAdParameters = new AdParameters();
		String placementId = SPMediationConfigurator.getConfiguration(adapter.getName(), PLACEMENT_ID, String.class);
		mAdParameters.getAppiaParameters().put(PLACEMENT_ID, placementId );
		this.mActivityRef = new WeakReference<Activity>(activity);
		checkForAds(activity);
	}

	@Override
	public boolean show(Activity parentActivity) {
		if (mAdResult != null){
			if (!mAdResult.hasError()) {
				AppiaDialog myDialog = new AppiaDialog(parentActivity);
				myDialog.show(mAdResult);
				myDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						fireCloseEvent();
					}
				});
				return true;
			} else {
				fireShowErrorEvent(mAdResult.getErrorText());
			}
		}
		return false;
	}


	@Override
	protected void checkForAds(Context context) {
		Activity activity = mActivityRef.get();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mAdResult = mAppia.getBannerAd(mActivityRef.get(),
							mAdParameters, BannerAdSize.SIZE_768x1024);
					if (mAdResult.hasError()) {
						fireValidationErrorEvent(mAdResult.getErrorText());
					} else {
						setAdAvailable();
					}
				}
			});
		}
	}
	
	private class AppiaDialog extends Dialog {

		public AppiaDialog(Context context) {
			super(context);
		}
		
		public void show(BannerAdResult adResult) {
			RelativeLayout layout = new RelativeLayout(getContext());
			layout.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
			getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(layout);
			
			if (!adResult.hasError()) {
				DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
				
				int dpValue = 15; // margin in dips
				float d = displayMetrics.density;
				int margin = (int)(dpValue * d); 

				View adView = adResult.getView();
				adView.setId(3);

				LayoutParams adViewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				adViewLayoutParams.setMargins(margin, margin, margin, margin);

				int dpi = displayMetrics.densityDpi;
				
				String path = "";
				if (dpi > 160 && dpi < 240 ) {
					path = "-hdpi";
				} else if (dpi < 320) {
					path = "-xhdpi";
				} else {
					path = "-xxhdpi";
				}
				
				ImageView imgView = new ImageView(getContext());
				imgView.setId(1);
				InputStream is = getClass().getResourceAsStream("/com/appia/res/drawable" + path + "/close_active.png");
				imgView.setImageDrawable(Drawable.createFromStream(is, ""));
				LayoutParams imgViewLayoutParams = new LayoutParams((int)(2 * dpValue * d),(int)(2 * dpValue * d));
				imgViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.ALIGN_PARENT_RIGHT);
				
				imgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
				
				FrameLayout touchFrame = new FrameLayout(getContext());
				LayoutParams frameLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				frameLayoutParams.addRule(RelativeLayout.BELOW, 1);
				frameLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, adView.getId());
				frameLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, adView.getId());
				frameLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, adView.getId());
				touchFrame.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						v.postDelayed(new Runnable() {
							@Override
							public void run() {
								fireClickEvent();
								dismiss();
							}
						}, 200);
						return false;
					}
				});
				
				layout.addView(adView, adViewLayoutParams);
				layout.addView(imgView, imgViewLayoutParams);
				layout.addView(touchFrame, frameLayoutParams);
			}
			fireImpressionEvent();
			show();
		}
    }

}