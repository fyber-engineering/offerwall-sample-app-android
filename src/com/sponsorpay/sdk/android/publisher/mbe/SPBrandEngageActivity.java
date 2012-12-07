package com.sponsorpay.sdk.android.publisher.mbe;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

public class SPBrandEngageActivity extends Activity implements SPBrandEngageClientStatusListener {
	
	private FrameLayout mFrameLayout;
//	private String TAG = "SPBrandEngageActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		mFrameLayout = new FrameLayout(this);
		SPBrandEngageClient.INSTANCE.startEngament(mFrameLayout);
		SPBrandEngageClient.INSTANCE.setStatusListener(this);
		setContentView(mFrameLayout);
	}
	
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
////		if (Build.VERSION.SDK_INT > 11) {
//		SponsorPayLogger.d(TAG,	"Orientation = "
//				+ (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE"
//								: "PORTRAIT"));
////		}
//		super.onConfigurationChanged(newConfig);
//	}
	
	
	@Override
	protected void onStop() {
		mFrameLayout.removeAllViews();
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {
		SPBrandEngageClient.INSTANCE.cancelEngagement();
		super.onBackPressed();
	}

	
	private void closeActivity() {
		mFrameLayout.removeAllViews();
		finish();
	}
	
	@Override
	protected void onDestroy() {
		SPBrandEngageClient.INSTANCE.setStatusListener(null);
		super.onDestroy();
	}
	
	//Status listener
	
	@Override
	public void didReceiveOffers(boolean areOffersAvaliable) {
		//do nothing
	}

	@Override
	public void didChangeStatus(SPBrandEngageClientStatus newStatus) {
		switch (newStatus) {
		case CLOSE_ABORTED:
		case CLOSE_FINISHED:
			closeActivity();
			break;
		default:
			break;
		}
	}
}
