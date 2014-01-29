package com.sponsorpay.utils;

import android.os.Handler;

public class SPHandler extends Handler{

	private static SPHandler instance = new SPHandler();
	
	private SPHandler() {
	}

	public static boolean hasMessage(int what) {
		return instance.hasMessages(what);
	}

	public static void sendMessageDelayed(int what, int delayMillis) {
		instance.sendEmptyMessageDelayed(what, delayMillis);
	}
	
}
