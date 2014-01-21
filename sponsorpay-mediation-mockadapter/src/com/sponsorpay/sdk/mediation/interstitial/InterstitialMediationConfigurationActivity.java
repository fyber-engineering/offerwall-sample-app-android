/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mediation.interstitial;

import java.util.Map;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sponsorpay.sdk.android.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.mediation.MockMediatedAdapter;

public class InterstitialMediationConfigurationActivity extends Activity {
	
	
    private OnClickListener listener = new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  MockInterstitialSetting setting = (MockInterstitialSetting) v.getTag();
	    	  Map<String, Object> config = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(MockMediatedAdapter.ADAPTER_NAME);
	    	  config.put(MockMediatedInterstitialAdapter.INTERSTITIAL_MOCK_SETTING, setting);
	    	  finish();
	      }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView text = new TextView(this);
		text.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		text.setTextSize(25f);
		text.setTypeface(Typeface.DEFAULT_BOLD);
//		text.setText(ConfigHolder.INSTANCE.getCurrentVideoConfig().toString());
		text.setText(SPMediationConfigurator.getConfiguration(
				MockMediatedAdapter.ADAPTER_NAME,
				MockMediatedInterstitialAdapter.INTERSTITIAL_MOCK_SETTING, MockInterstitialSetting.class)
				.toString());
		
		ListView lv = new ListView(this);
		lv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		lv.setAdapter(new MockInterstitialMediationListAdapter(this, listener));
		
		layout.addView(text);
		layout.addView(lv);
		
		setContentView(layout);
	}
	
	
	
}
