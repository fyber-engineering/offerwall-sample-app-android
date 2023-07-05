/*
 * Fyber Android SDK
 * <p/>
 * Copyright (c) 2015 Fyber. All rights reserved.
 */
package com.fyber.offerwall.sampleapp.fragments;

import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fyber.ads.AdFormat;
import com.fyber.offerwall.sampleapp.MainActivity;
import com.fyber.offerwall.sampleapp.R;
import com.fyber.requesters.RequestError;
import com.fyber.utils.FyberLogger;

public abstract class FyberFragment extends Fragment {

    private boolean isRequestingState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //resetting button
        setButtonToOriginalState();
    }

    /*
     * ** Fragment specific methods **
     */

    protected abstract String getLogTag();

    protected abstract String getRequestText();

    protected abstract String getShowText();

    protected abstract Button getButton();

    protected abstract void performRequest();


    // when a button is clicked, request or show the ad according to Intent availability
    protected void requestOrShowAd() {
        //avoid requesting an ad when already requesting
        if (!isRequestingState()) {
            setButtonToRequestingMode();
            // perform the ad request. Each Fragment has its own implementation.
            performRequest();
        }
    }

    /*
     * ** RequestCallback methods **
     */

    public void onAdNotAvailable(AdFormat adFormat) {
        FyberLogger.d(getLogTag(), "No ad available");
        resetRequestingState();
        resetButtonStateWithAnimation();
    }

    public void onRequestError(RequestError requestError) {
        FyberLogger.d(getLogTag(), "Something went wrong with the request: " + requestError.getDescription());
        resetRequestingState();
        resetButtonStateWithAnimation();
    }
    /*
     * ** State helper methods **
     */

    private void resetRequestingState() {
        isRequestingState = false;
    }

    protected boolean isRequestingState() {
        return isRequestingState;
    }

    /*
     * ** UI state helper methods **
     */

    private void resetButtonStateWithAnimation() {
        getButton().startAnimation(MainActivity.getCounterclockwiseAnimation());
        getButton().setText(getRequestText());
    }

    protected void setButtonToRequestingMode() {
        getButton().startAnimation(MainActivity.getClockwiseAnimation());
        getButton().setText(getString(R.string.getting_offers));
        isRequestingState = true;
    }

    protected void setButtonToSuccessState(Button button, String text) {
        setButtonColorAndText(button, text, getResources().getColor(R.color.buttonColorSuccess));
    }

    protected void setButtonToOriginalState() {
        setButtonColorAndText(getButton(), getRequestText(), getResources().getColor(R.color.colorPrimary));
    }

    private void setButtonColorAndText(Button button, String text, int color) {
        Drawable drawable = button.getBackground();
        ColorFilter filter = new LightingColorFilter(color, color);
        drawable.setColorFilter(filter);
        button.setText(text);
    }

}
