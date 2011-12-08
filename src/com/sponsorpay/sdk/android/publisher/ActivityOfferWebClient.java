package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.util.Log;

public class ActivityOfferWebClient extends OfferWebClient {

	private Activity mHostActivity;
	private boolean mShouldHostActivityStayOpen;

	public ActivityOfferWebClient(Activity hostActivity, boolean shouldStayOpen) {
		super();
		mHostActivity = hostActivity;
		mShouldHostActivityStayOpen = shouldStayOpen;
	}

	@Override
	protected void onSponsorPayExitScheme(int resultCode, String targetUrl) {
		mHostActivity.setResult(resultCode);
		boolean willCloseHostActivity = false;

		if (targetUrl == null) { // Exit scheme without target url: just close the host activity
			willCloseHostActivity = true;
		} else {
			willCloseHostActivity = !mShouldHostActivityStayOpen;
			launchActivityWithUrl(mHostActivity, targetUrl);	
		}
	
		Log.i(OfferWebClient.LOG_TAG, "Should stay open: " + mShouldHostActivityStayOpen +
				", will close activity: " + willCloseHostActivity);
		
		if (willCloseHostActivity)
			mHostActivity.finish();
	}

}
