package com.fyber.offerwall.sampleapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fyber.fairbid.ads.OfferWall;
import com.fyber.fairbid.ads.offerwall.OfferWallError;
import com.fyber.fairbid.ads.offerwall.OfferWallListener;
import com.fyber.fairbid.ads.offerwall.VirtualCurrencyErrorResponse;
import com.fyber.fairbid.ads.offerwall.VirtualCurrencyListener;
import com.fyber.fairbid.ads.offerwall.VirtualCurrencySuccessfulResponse;
import com.fyber.fairbid.internal.VirtualCurrencySettings;
import com.fyber.offerwall.sampleapp.fragments.OfferwallFragment;

public class MainActivity extends FragmentActivity {
    private static final String APP_ID = "135704";
    private static final String SECURITY_TOKEN = "sec_135704";

    private static final String USER_ID = "userId";

    private static final int DURATION_MILLIS = 300;
    private static final int DEGREES_360 = 360;
    private static final int DEGREES_0 = 0;
    private static final float PIVOT_X_VALUE = 0.5f;
    private static final float PIVOT_Y_VALUE = 0.5f;
    private static final String TAG = "FyberMainActivity";

    Fragment fragment;

    OfferWallListener offerWallListener = new OfferWallListener() {
        @Override
        public void onShowError(@Nullable String s, @NonNull OfferWallError offerWallError) {
            Log.i("OfferWallListener", "offer wall show error: " + offerWallError);
        }

        @Override
        public void onShow(@Nullable String placementId) {
            Log.i("OfferWallListener", "offer wall shown! placement id: " + placementId);
        }

        @Override
        public void onClose(@Nullable String placementId) {
            Log.i("OfferWallListener", "offer wall closed! placement id: "+ placementId);
        }
    };

    VirtualCurrencyListener vcListener = new VirtualCurrencyListener() {
        @Override
        public void onVirtualCurrencySuccess(@NonNull VirtualCurrencySuccessfulResponse virtualCurrencySuccessfulResponse) {
            Log.i("VirtualCurrencyListener", "VCS Success: "+ virtualCurrencySuccessfulResponse);
        }

        @Override
        public void onVirtualCurrencyError(@NonNull VirtualCurrencyErrorResponse virtualCurrencyError) {
            Log.i("VirtualCurrencyListener", "VCS error: "+ virtualCurrencyError);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //	enable OfferWall logs so that we can see what is going on the SDK level
        OfferWall.setLogLevel(OfferWall.LogLevel.DEBUG);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.offer_wall_fragment_layout);
        if (fragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new OfferwallFragment();
            fragmentTransaction.add(android.R.id.content, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {

            // You might want to provide your custom user ID to OfferWall SDK. If you do not provide any value here,
            // the OfferWall SDK will generate one for you. You can change this value at any time anyway.
            OfferWall.setUserId(USER_ID);

            // ** SDK INITIALIZATION **
            // If you want to use the Virtual Currency feature in your app, you need to provide an object of VirtualCurrencySettings class
            // that holds the necessary data to make this work:
            // - security token (String),
            // - the VirtualCurrencyListener object that will react to the requests results.
            VirtualCurrencySettings vcSettings = new VirtualCurrencySettings(SECURITY_TOKEN, vcListener);
            // If you don't need to configure Virtual Currency in your app, you can omit this param.
            // OfferWall.start(this, APP_ID, offerWallListener, disableAdvertisingId);

            // The OfferWall SDK `start` method can accept the parameter that defines if the usage of GAID is limited.
            boolean disableAdvertisingId = false;
            // If you don't need to disable the advertising ID, you can omit this parameter as well. It will
            // fall back to `false `by default.
            // OfferWall.start(this, APP_ID, offerWallListener);

            // You can start the OfferWall with 3 different variants of the `start()` method.
            // The following one allows you defining all starting parameters, including Virtual Currency settings
            // and decide whether you want to limit the usage of Google Advertising ID. Otherwise, use the calls
            // demonstrated above.
            OfferWall.start(this, APP_ID, offerWallListener, "fb3876c383844db96d6ddece3c0cd9eb7f9029b0e7f319f0a98da55c1b76d802", disableAdvertisingId, vcSettings);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    // ** Animations **
    public static Animation getClockwiseAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        RotateAnimation rotateAnimation = new RotateAnimation(DEGREES_0, DEGREES_360, Animation.RELATIVE_TO_SELF, PIVOT_X_VALUE, Animation.RELATIVE_TO_SELF, PIVOT_Y_VALUE);
        rotateAnimation.setDuration(DURATION_MILLIS);
        animationSet.addAnimation(rotateAnimation);

        return animationSet;
    }

    public static Animation getCounterclockwiseAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        RotateAnimation rotateAnimation = new RotateAnimation(DEGREES_360, DEGREES_0, Animation.RELATIVE_TO_SELF, PIVOT_X_VALUE, Animation.RELATIVE_TO_SELF, PIVOT_Y_VALUE);
        rotateAnimation.setDuration(DURATION_MILLIS);
        animationSet.addAnimation(rotateAnimation);

        return animationSet;
    }
}
