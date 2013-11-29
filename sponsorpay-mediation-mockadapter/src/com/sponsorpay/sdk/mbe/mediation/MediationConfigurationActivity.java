/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mbe.mediation;

import java.util.Map;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.mbe.mediation.helpers.ConfigHolder;
import com.sponsorpay.sdk.mbe.mediation.helpers.MockSetting;

public class MediationConfigurationActivity extends Activity {
	
	
    private OnClickListener listener = new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  MockSetting setting = (MockSetting) v.getTag();
	    	  Map<String, Object> config = SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(MockMediatedAdapter.ADAPTER_NAME);
	    	  config.put(MockMediatedAdapter.MOCK_PLAYING_BEHAVIOUR, setting.getBehaviour());
	    	  config.put(MockMediatedAdapter.VIDEO_EVENT_RESULT, setting.getVideoEvent());
	    	  config.put(MockMediatedAdapter.VALIDATION_RESULT, setting.getValidationResult());
	    	  ConfigHolder.INSTANCE.setCurrentConfig(setting);
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
		text.setText(ConfigHolder.INSTANCE.getCurrentConfig().toString());
		
		ExpandableListView lv = new ExpandableListView(this);
		lv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		lv.setAdapter(new MockMediationListAdapter(this, listener));
		
		layout.addView(text);
		layout.addView(lv);
		
		setContentView(layout);
	}
	
	
	
}
