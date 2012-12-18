package com.sponsorpay.sdk.android.publisher.mbe;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SPBrandEngageActivity extends Activity implements SPBrandEngageClientStatusListener {
	
//	private FrameLayout mFrameLayout;
//	private String TAG = "SPBrandEngageActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
//		mFrameLayout = new FrameLayout(this);
//		mFrameLayout.setBackgroundResource(R.color.background_dark);
//		setContentView(mFrameLayout);

		SPBrandEngageClient.INSTANCE.setStatusListener(this);
//		SPBrandEngageClient.INSTANCE.startEngament(mFrameLayout);
		SPBrandEngageClient.INSTANCE.startEngament(this);
//		setContentView(mFrameLayout);
	}
	
//	@Override
//	protected void onStop() {
//		mFrameLayout.removeAllViews();
//		super.onStop();
//	}
	
	@Override
	public void onBackPressed() {
		SPBrandEngageClient.INSTANCE.closeEngagement();
		super.onBackPressed();
	}

	
	private void closeActivity() {
//		mFrameLayout.removeAllViews();
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
		case ERROR:
			closeActivity();
			break;
		default:
			break;
		}
	}
}
