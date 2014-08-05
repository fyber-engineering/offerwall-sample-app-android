package com.sponsorpay.mediation.mbe;

import android.app.Activity;
import android.content.Intent;

import com.hyprmx.android.sdk.HyprMXHelper;
import com.hyprmx.android.sdk.HyprMXPresentation;
import com.sponsorpay.mediation.helper.HyprMXVideoAdapterHelper;
import com.sponsorpay.utils.SponsorPayLogger;

/**
 * The purpose of this activity is to host the displaying 
 * video and provide a proper handling mechanism for the
 * received ad result.
 */

public class HyprMXVideoActivity extends Activity {

	private static final String TAG = "HyprMXVideoActivity";

	@Override
	protected void onResume() {
		super.onResume();
		runPresentation();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SponsorPayLogger.d(TAG, "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		HyprMXHelper.processActivityResult(this, requestCode, resultCode, data,
				HyprMXVideoAdapterHelper.getHyprMXVideoMediationAdapter());
		
		//CLose the activity
		finish();
	}

	private void runPresentation() {
		HyprMXVideoMediationAdapter videoAdapter = HyprMXVideoAdapterHelper.getHyprMXVideoMediationAdapter();
		HyprMXPresentation presentation = videoAdapter.getPresentation();
		if (presentation != null) {
			presentation.show(this);
		}
	}
}
