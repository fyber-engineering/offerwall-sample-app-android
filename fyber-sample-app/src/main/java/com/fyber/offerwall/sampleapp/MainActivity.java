package com.fyber.offerwall.sampleapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fyber.Fyber;
import com.fyber.offerwall.sampleapp.fragments.OfferwallFragment;
import com.fyber.utils.FyberLogger;

import butterknife.ButterKnife;


// Fyber SDK takes advantage of the power of annotations to make mediation simpler to integrate.
// To enable mediation in this app simply uncomment @FyberSDK annotation line below.
// Also, make sure you have the right dependencies in your gradle file.
//@FyberSDK
public class MainActivity extends FragmentActivity {

    private static final String APP_ID = "22915";
    private static final String SECURITY_TOKEN = "token";

    private static final String USER_ID = "userId";

    private static final int DURATION_MILLIS = 300;
    private static final int DEGREES_360 = 360;
    private static final int DEGREES_0 = 0;
    private static final float PIVOT_X_VALUE = 0.5f;
    private static final float PIVOT_Y_VALUE = 0.5f;
    private static final String TAG = "FyberMainActivity";

    Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//		enabling Fyber logs so that we can see what is going on the SDK level
        FyberLogger.enableLogging(BuildConfig.DEBUG);

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

            // ** SDK INITIALIZATION **

            //when you start Fyber SDK you get a Settings object that you can use to customise the SDK behaviour.
            //Have a look at the method 'customiseFyberSettings' to learn more about possible customisation.
            Fyber.Settings fyberSettings = Fyber
                    .with(APP_ID, this)
                    .withSecurityToken(SECURITY_TOKEN)
// by default Fyber SDK will start precaching. If you wish to only start precaching at a later time you can uncomment this line and use 'CacheManager' to start, pause or resume on demand.
//					.withManualPrecaching()
// if you do not provide an user id Fyber SDK will generate one for you
//					.withUserId(USER_ID)
                    .start();
// uncomment to customise Fyber SDK
//			customiseFyberSettings(fyberSettings);

        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    //User Settings to customise Fyber SDK behaviour
    private void customiseFyberSettings(Fyber.Settings fyberSettings) {
        fyberSettings.notifyUserOnReward(false)
                     .closeOfferWallOnRedirect(true)
                     .addParameter("myCustomParamKey", "myCustomParamValue")
                     .setCustomUIString(Fyber.Settings.UIStringIdentifier.GENERIC_ERROR, "my custom generic error msg");
    }

    /*
     * ** Fyber SDK: other features **
     *
     * > this method shows you a couple of features from Fyber SDK that we left out of the sample app:
     * > report installs and rewarded actions (mainly for advertisers)
     * > control over which thread should the requester callback run on
     * > creating a new Requester from an existing Requester
     */

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
