package com.fyber.sampleapp;

import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.Button;

import com.fyber.Fyber;
import com.fyber.sampleapp.fragments.InterstitialFragment;
import com.fyber.sampleapp.fragments.OfferwallFragment;
import com.fyber.sampleapp.fragments.RewardedVideoFragment;
import com.fyber.utils.FyberLogger;


public class MainActivity extends FragmentActivity {

	//FIXME: what are the correct values to use here?
	private static final String APP_ID = "24913";
	private static final String SECURITY_TOKEN = "128194d69f9a68d14db869140e1a108b";

	public static final int DURATION_MILLIS = 300;
	public static final int DEGREES_360 = 360;
	public static final int DEGREES_0 = 0;
	public static final float PIVOT_X_VALUE = 0.5f;
	public static final float PIVOT_Y_VALUE = 0.5f;
	public static final int INTERSTITIAL_FRAGMENT_NUMBER = 0;
	public static final int REWARDED_VIDEO_FRAGMENT_NUMBER = 1;
	public static final int OFFERWALL_FRAGMENT_NUMBER = 2;
	public static final String TAG = "FyberMainActivity";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_fyber_main);

//		enabling Fyber logs so that we can see what is going on on the sdk level
		FyberLogger.enableLogging(true);

		setupViewPager();
		setupToolbar();
	}

	private void setupViewPager() {
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(1);
	}

	private void setupToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		toolbar.setTitle(getString(R.string.fyber_header));
		toolbar.setLogo(R.drawable.ic_launcher);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			Fyber fyber = Fyber
					.with(APP_ID, this)
					.withSecurityToken(SECURITY_TOKEN)
					//FIXME: add correct values so that uncommenting the code works correctly. Add comments explaining each line?
//					.withManualPrecaching()
//					.withCustomParams(...)
//					.withUserId(USER_ID)
					;

			//when you start Fyber sdk you get a Settings object that you can use to customise the sdk behaviour.
			//Have a look at the method 'customiseFyberSettings' to learn more about possible customisation.
			Fyber.Settings fyberSettings = fyber.start();

//			customiseFyberSettings(fyberSettings);

		} catch (IllegalArgumentException e) {
			Log.d(TAG, e.getLocalizedMessage());
		}
	}

	private void customiseFyberSettings(Fyber.Settings fyberSettings) {
		fyberSettings.showNotificationOnUserRewarded(false);
		fyberSettings.closeOfferWallOnRedirect(true);
		fyberSettings.showNotificationOnUserEngaged(true);
		fyberSettings.addParameter("myCustomParamKey", "myCustomParamValue");
		fyberSettings.setCustomUIString(Fyber.Settings.UIStringIdentifier.GENERIC_ERROR, "my custom generic error msg");
	}

	// ** Animations **

	public static Animation getClockwiseAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(DEGREES_0, DEGREES_360, Animation.RELATIVE_TO_SELF, PIVOT_X_VALUE, Animation.RELATIVE_TO_SELF, PIVOT_Y_VALUE);
		rotateAnimation.setDuration(DURATION_MILLIS);
		animationSet.addAnimation(rotateAnimation);

		return animationSet;
	}

	public static Animation getReverseClockwiseAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(DEGREES_360, DEGREES_0, Animation.RELATIVE_TO_SELF, PIVOT_X_VALUE, Animation.RELATIVE_TO_SELF, PIVOT_Y_VALUE);
		rotateAnimation.setDuration(DURATION_MILLIS);
		animationSet.addAnimation(rotateAnimation);

		return animationSet;
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {

				case INTERSTITIAL_FRAGMENT_NUMBER:
					return InterstitialFragment.newInstance();

				case REWARDED_VIDEO_FRAGMENT_NUMBER:
					return RewardedVideoFragment.newInstance();

				case OFFERWALL_FRAGMENT_NUMBER:
					return OfferwallFragment.newInstance();

				default:
					return RewardedVideoFragment.newInstance();
			}
			// getItem is called to instantiate the fragment for the given page.
		}

		@Override
		public int getCount() {
			// Show 3 total pages ( one for each ad format).
			return 3;
		}

		//FIXME: we should center the icons and add a descriptive title like in the iOS app
		@Override
		public CharSequence getPageTitle(int position) {
			return getSpannableString(position);
		}

		private SpannableString getSpannableString(int position) {
			Drawable image;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				image = getDrawable(imageResId[position]);
			} else {
				image = getResources().getDrawable(imageResId[position]);
			}
			image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
			SpannableString spannableString = new SpannableString(" ");
			ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
			spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			return spannableString;
		}

		private int[] imageResId = {
				R.drawable.ic_action_icon_interstitial,
				R.drawable.ic_action_icon_rewarded_video,
				R.drawable.ic_action_icon_offerwall
		};
	}

}
