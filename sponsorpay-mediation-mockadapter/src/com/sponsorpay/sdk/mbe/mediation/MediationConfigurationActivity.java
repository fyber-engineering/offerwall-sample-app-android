/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mbe.mediation;

import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;

import com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationConfigurator;
import com.sponsorpay.sdk.mbe.mediation.helpers.MockSetting;

public class MediationConfigurationActivity extends Activity {
	
	
    private OnClickListener listener = new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  MockSetting setting = (MockSetting) v.getTag();
	    	  Map<String, Object> config = SPMediationConfigurator.INSTANCE.getConfigurationForAdaptor(MockMediatedAdaptor.ADAPTOR_NAME);
	    	  config.put(MockMediatedAdaptor.MOCK_PLAYING_BEHAVIOUR, setting.getBehaviour());
	    	  config.put(MockMediatedAdaptor.VIDEO_EVENT_RESULT, setting.getVideoEvent());
	    	  config.put(MockMediatedAdaptor.VALIDATION_RESULT, setting.getValidationResult());
	    	  finish();
	      }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExpandableListView lv = new ExpandableListView(this);
		lv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		lv.setAdapter(new MockMediationListAdapter(this, listener));
		
		setContentView(lv);
	}
	
	
	
}
