package com.sponsorpay.sdk.mediation.interstitial;

import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.android.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.mediation.MockMediatedAdapter;

public class MockMediatedInterstitialAdapter extends
		SPInterstitialMediationAdapter<MockMediatedAdapter> {
	
	public static final String INTERSTITIAL_MOCK_SETTING = "interstitial.mock.setting";
	private  static final String INTERSTITIAL_CLASS_NAME = "interstitial.class.name";

	public MockMediatedInterstitialAdapter(MockMediatedAdapter adapter, HashMap<String, Object> configs) {
		super(adapter);
		configs.put(INTERSTITIAL_MOCK_SETTING, MockInterstitialSetting.ValidationAdsAvailable);
		configs.put(INTERSTITIAL_CLASS_NAME, InterstitialMediationConfigurationActivity.class.getCanonicalName());
	}

	@Override
	protected boolean show(Activity parentActivity) {
		if (getConfig() == MockInterstitialSetting.ShowError) {
			fireErrorEvent("Interstitial show error");
			return false;
		}
		MockInterstitialDialog dialog = new MockInterstitialDialog(parentActivity);
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				fireCloseEvent();
			}
		});
		dialog.show();
		return true;
	}

	@Override
	protected void checkForAds(Context context) {
		switch (getConfig()) {
		case ValidationAdsAvailable:
		case ShowError:
			setAdAvailable();
			break;
		case ValidationError:
			fireErrorEvent("Validation error");
			break;
		case ValidationNoAds:
		default:
			break;
		}
	}
	
	
	private MockInterstitialSetting getConfig() {
		return (MockInterstitialSetting) SPMediationConfigurator.getConfiguration(
				getName(), INTERSTITIAL_MOCK_SETTING, Object.class);
	}
	
	private class MockInterstitialDialog extends Dialog {

		public MockInterstitialDialog(Context context) {
			super(context);
		}
		
		@Override
		public void show() {
			LinearLayout layout = new LinearLayout(getContext());
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
			getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(layout);
			
			TextView interstitialView = new TextView(getContext());
			interstitialView.setText("I'm an intersitial AD");
			interstitialView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			
			TextView closeView = new TextView(getContext());
			closeView.setText("Close");
			closeView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			closeView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			TextView clickView = new TextView(getContext());
			clickView.setText("Click");
			clickView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			clickView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					fireClickEvent();
					dismiss();
				}
			});
			
			
			
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
			super.show();
		}
    }
	

}
