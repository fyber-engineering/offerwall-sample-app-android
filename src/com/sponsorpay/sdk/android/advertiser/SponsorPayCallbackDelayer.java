/**
 * SponsorPay Android Advertiser SDK
 *
 * Copyright 2011 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import com.sponsorpay.sdk.android.HostInfo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

/**
 * Enables triggering the advertiser's callback with a delay. Uses the Android alarm mechanism
 * provided by {@link AlarmManager}.
 * 
 */
public class SponsorPayCallbackDelayer extends BroadcastReceiver {
	public static final String ACTION_TRIGGER_SPONSORPAY_CALLBACK = "ACTION_TRIGGER_SPONSORPAY_CALLBACK";
	public static final String EXTRA_APPID_KEY = "EXTRA_APPID_KEY";
	public static final int MILLISECONDS_IN_MINUTE = 60000;

	/**
	 * Triggers the Advertiser callback after the specified delay has passed. Will use the provided
	 * Apps ID instead of trying to retrieve the one defined in the host application's manifest.
	 * Registers an alarm with the OS {@link AlarmManager}. {@link #onReceive(Context, Intent)} will
	 * be invoked when the specified period of time has elapsed .
	 * 
	 * @param context
	 *            Host application context.
	 * @param appId
	 *            The App ID to use. Pass an empty string to let the SDK try to retrieve it from the
	 *            application manifest.
	 * @param delayMinutes
	 *            The delay in minutes for triggering the Advertiser callback.
	 */
	public static void callWithDelay(Context context, String appId, long delayMinutes) {
		Log.d(SponsorPayCallbackDelayer.class.toString(), "callWithDelay called");

		// if HostInfo must launch a RuntimeException due to an invalid App ID value, let it do that
		// right now:
		if (appId == null || appId.equals("")) {
			HostInfo hostInfo = new HostInfo(context);
			hostInfo.getAppId();
		}

		// Create a new instance and register it as BroadcastReceiver
		SponsorPayCallbackDelayer instance = new SponsorPayCallbackDelayer();
		IntentFilter checkInFilter = new IntentFilter(ACTION_TRIGGER_SPONSORPAY_CALLBACK);
		context.registerReceiver(instance, checkInFilter);

		// Generate the PendingIntent which will be called with delay
		Intent intent = new Intent(ACTION_TRIGGER_SPONSORPAY_CALLBACK);
		intent.putExtra(EXTRA_APPID_KEY, appId);
		PendingIntent triggerCallbackPendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_ONE_SHOT);

		// Calculate the delay and register the PendingIntent with the Alarm Manager
		long timeForCheckInAlarm = SystemClock.elapsedRealtime() + delayMinutes
				* MILLISECONDS_IN_MINUTE;
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeForCheckInAlarm,
				triggerCallbackPendingIntent);
	}

	/**
	 * onReceived method overridden from {@link BroadcastReceiver}. Performs the actual triggering
	 * of the callback.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(this.getClass().toString(), "Calling SponsorPayAdvertiser.register");
		SponsorPayAdvertiser.register(context, intent.getStringExtra(EXTRA_APPID_KEY));
		context.unregisterReceiver(this);
	}
}
