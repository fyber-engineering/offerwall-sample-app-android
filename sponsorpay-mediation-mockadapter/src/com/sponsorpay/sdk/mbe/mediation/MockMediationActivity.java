/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.mbe.mediation;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MockMediationActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
				
	    LinearLayout ll = new LinearLayout(this);
	    ll.setOrientation(LinearLayout.VERTICAL);
	    ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    ll.setGravity(Gravity.CENTER);  
	    ll.setBackgroundColor(Color.DKGRAY);

	    TextView tv1 = new TextView(this);
	    tv1.setText("Mock Mediated Network");
	    tv1.setTextSize(24f);
	    tv1.setTextColor(Color.WHITE);
	    tv1.setGravity(Gravity.CENTER_HORIZONTAL);
	    ll.addView(tv1);
	    
	    TextView tv2 = new TextView(this);
	    tv2.setText("Video Player");
	    tv2.setTextSize(62f);
	    tv2.setTypeface(Typeface.DEFAULT_BOLD);
	    tv2.setTextColor(Color.WHITE);
	    tv2.setGravity(Gravity.CENTER_HORIZONTAL);
	    ll.addView(tv2);
	    
	    
	    setContentView(ll);
	}
	
}
