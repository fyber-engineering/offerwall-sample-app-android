/**
 * Fyber Android SDK
 * <p/>
 * Copyright (c) 2015 Fyber. All rights reserved.
 */
package com.fyber.sampleapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.widget.Button;

import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.sampleapp.MainActivity;
import com.fyber.sampleapp.R;
import com.fyber.utils.AdFormat;
import com.fyber.utils.FyberLogger;

public abstract class FyberFragment extends Fragment  implements RequestCallback {

	protected static final int INTERSTITIAL_REQUEST_CODE = 8792;
	protected static final int OFFERWALL_REQUEST_CODE = 8795;
	protected static final int REWARDED_VIDEO_REQUEST_CODE = 8796;

	protected Intent intent;

	public abstract String  getLogTag();
	public abstract String  getRequestText();
	public abstract String  getShowText();
	public abstract Button getButton();

	// ** RequestCallback methods **

	@Override
	public void onAdAvailable(Intent intent) {
		getButton().startAnimation(MainActivity.getClockwiseAnimation());
		setButtonColorAndText(getButton(), getShowText(), getResources().getColor(R.color.buttonColorSuccess));
		this.intent = intent;

		//if you are using a general purpose requestCallback like this you might want to verify which adFormat will this intent show.
		//You can use the AdFormat class to obtain an AdFormat from a given intent. Then you perform ad foramt specific actions e.g.:
		AdFormat adFormat = AdFormat.getFromIntent(intent);
		switch (adFormat){
			case OFFER_WALL:
				//in our sample app, we want to show the offer wall in a single step
				startActivityForResult(intent, OFFERWALL_REQUEST_CODE);
				break;
		}
	}

	@Override
	public void onAdNotAvailable() {
		FyberLogger.d(getLogTag(), "no ad available");
		resetState();
	}

	@Override
	public void onRequestError(RequestError requestError) {
		//FIXME: this is ambiguous. In the case of the rewarded video, it can either be a video request error or a vcs request error. Should we move to anonymous callbacks?
		FyberLogger.d(getLogTag(), "error requesting ad: " + requestError.getDescription());
		resetState();
	}

	private void resetState() {
		intent = null;
		getButton().startAnimation(MainActivity.getReverseClockwiseAnimation());
		getButton().setText(getRequestText());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REWARDED_VIDEO_REQUEST_CODE:
					// If you did not chain a vcs callback in the rewarded video request, you can uncomment the line below and the respective method to make a separate vcs request.
//					requestVirtualCurrency();
				case INTERSTITIAL_REQUEST_CODE:
				case OFFERWALL_REQUEST_CODE:
					intent = null;
					setButtonColorAndText(getButton(), getRequestText(), getResources().getColor(R.color.colorPrimary));
					break;
				default:
					break;
			}
		}
	}

	public void setButtonColorAndText(Button button, String text, int color) {
		Drawable drawable = button.getBackground();
		ColorFilter filter = new LightingColorFilter(color, color);
		drawable.setColorFilter(filter);
		button.setText(text);
	}

}
