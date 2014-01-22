package com.sponsorpay.mediation.interstitial;

import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.sponsorpay.mediation.MockMediatedAdapter;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.interstitial.mediation.SPInterstitialMediationAdapter;

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
		return SPMediationConfigurator.getConfiguration(
				getName(), INTERSTITIAL_MOCK_SETTING, MockInterstitialSetting.class);
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
			getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.GRAY));
			layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(layout);
			
			TextView interstitialView = new TextView(getContext());
			interstitialView.setText("I'm an intersitial AD");
			interstitialView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			interstitialView.setTextSize(24f);
			interstitialView.setTextColor(Color.WHITE);
			
			
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
			
			layout.addView(interstitialView);
			layout.addView(closeView);
			layout.addView(clickView);
			super.show();
		}
    }
	

}
