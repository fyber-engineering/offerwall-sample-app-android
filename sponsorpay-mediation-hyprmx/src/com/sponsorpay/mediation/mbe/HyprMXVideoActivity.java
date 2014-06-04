package com.sponsorpay.mediation.mbe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hyprmx.android.sdk.HyprMXHelper;
import com.hyprmx.android.sdk.HyprMXPresentation;
import com.sponsorpay.mediation.helper.HyprMXVideoAdapterHelper;
import com.sponsorpay.utils.SponsorPayLogger;

/**
 * Purpose of this activity is to host displaying the video and provide proper
 * handling mechanism for received ad result.
 * 
 * @author hubert
 * 
 */

public class HyprMXVideoActivity extends Activity {

	private static final String TAG = HyprMXVideoActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		runPresentation();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SponsorPayLogger.d(TAG, "onactivityresult");
		super.onActivityResult(requestCode, resultCode, data);
		HyprMXHelper.processActivityResult(this, requestCode, resultCode, data,
				HyprMXVideoAdapterHelper.getHyprMXVideoMediationAdapter());
		finish();
	}

	private void runPresentation() {
//		HyprMXMediationAdapter adapter = (HyprMXMediationAdapter) HyprMXVideoAdapterHelper.getHyprMXMediationAdapter();
		HyprMXVideoMediationAdapter videoAdapter = HyprMXVideoAdapterHelper.getHyprMXVideoMediationAdapter();
		HyprMXPresentation presentation = videoAdapter.getPresentation();
		presentation.show(this);
	}
}
