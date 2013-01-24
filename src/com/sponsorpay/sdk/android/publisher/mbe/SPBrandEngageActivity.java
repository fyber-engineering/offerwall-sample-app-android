package com.sponsorpay.sdk.android.publisher.mbe;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SPBrandEngageActivity extends Activity implements SPBrandEngageClientStatusListener {
	

	private boolean mPendingClose = false;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		

		if (Build.VERSION.SDK_INT == 9 ||
				Build.VERSION.SDK_INT == 10) {
			setRequestedOrientation(
					   ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		
		SPBrandEngageClient.INSTANCE.setStatusListener(this);
		SPBrandEngageClient.INSTANCE.startEngagement(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mPendingClose) {
			SPBrandEngageClient.INSTANCE.closeEngagement();
		}
	}
	
	@Override
	public void onBackPressed() {
		SPBrandEngageClient.INSTANCE.closeEngagement();
		super.onBackPressed();
	}

	private void closeActivity() {
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
		case PENDING_CLOSE:
			mPendingClose  = true;
			break;
		default:
			break;
		}
	}
}
