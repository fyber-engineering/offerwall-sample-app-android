/**
 * SponsorPay Android SDK
 *
 * Copyright 2012 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

public class SponsorPayLogger {

	private static boolean logging = false;

	public static boolean toggleLogging() {
		logging = !logging;
		return logging;
	}
	
	public static boolean isLogging() {
		return logging;
	}

	public static boolean enableLogging(boolean shouldLog) {
		logging = shouldLog;
		return logging;
	}

	public static void e(String tag, String message) {
		if (logging) {
			Log.e(tag, message);
			if (textViewLogger != null) {
				textViewLogger.log(TextViewLogger.Level.ERROR, tag, message, null);
			}
		}
	}

	public static void e(String tag, String message, Exception exception) {
		if (logging) {
			Log.w(tag, message, exception);
			if (textViewLogger != null) {
				textViewLogger.log(TextViewLogger.Level.ERROR, tag, message, exception);
			}
		}
	}

	public static void d(String tag, String message) {
		if (logging) {
			Log.d(tag, message);
			if (textViewLogger != null) {
				textViewLogger.log(TextViewLogger.Level.DEBUG, tag, message, null);
			}
		}
	}

	public static void i(String tag, String message) {
		if (logging) {
			Log.i(tag, message);
			if (textViewLogger != null) {
				textViewLogger.log(TextViewLogger.Level.INFO, tag, message, null);
			}
		}
	}

	public static void v(String tag, String message) {
		if (logging) {
			Log.v(tag, message);
			if (textViewLogger != null) {
				textViewLogger.log(TextViewLogger.Level.VERBOSE, tag, message, null);
			}
		}
	}

	public static void w(String tag, String message) {
		if (logging) {
			Log.w(tag, message);
			if (textViewLogger != null) {
				textViewLogger.log(TextViewLogger.Level.WARNING, tag, message, null);
			}
		}
	}

	public static void w(String tag, String message, Exception exception) {
		if (logging) {
			Log.w(tag, message, exception);
			if (textViewLogger != null) {
				textViewLogger.log(TextViewLogger.Level.WARNING, tag, message, exception);
			}
		}
	}
	
	// Log to text view methods
	
	private static TextViewLogger textViewLogger;
	
	
	private static class TextViewLogger {
		
		public enum Level {
			VERBOSE,
			DEBUG,
			INFO,
			WARNING,
			ERROR,
		}
		
		private TextView mTextView;
		
		private Time mTime = new Time();
		
		public void setTextView(TextView textView) {
			mTextView = textView;
			mTextView.setMovementMethod(new ScrollingMovementMethod());
		}
		
		public TextView getTextView() {
			return mTextView;
		}
		
		public void log(Level level, String tag, String message, Exception exception) {
			
			mTime.setToNow();

			ForegroundColorSpan colorSpan = getColorSpan(level);
			
			String text = mTime.format2445()
					+ " ["
					+ tag
					+ "]\n"
					+ message
					+ (exception != null ? " - Exception: "
							+ exception.getLocalizedMessage() : "") + "\n\n";

			final Spannable spannedText = new SpannableString(text);

			spannedText.setSpan(colorSpan, 0, text.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			mTextView.post(new Runnable() {
				@Override
				public void run() {
					mTextView.append(spannedText);

					Layout layout = mTextView.getLayout();
					if (layout != null) {
						final int scrollAmount = layout.getLineTop(
								mTextView.getLineCount())
								- mTextView.getHeight();
						// if there is no need to scroll, scrollAmount will be <=0
						if (scrollAmount > 0) {
							mTextView.scrollTo(0, scrollAmount);
						}
					}
				}
			});
		}
		
		private ForegroundColorSpan getColorSpan(Level level) {
			ForegroundColorSpan colorSpan;
			
			switch (level) {
			case DEBUG:
				colorSpan = new ForegroundColorSpan(Color.BLUE);
				break;
			case INFO:
				colorSpan = new ForegroundColorSpan(Color.GREEN);
				break;
			case WARNING:
				colorSpan = new ForegroundColorSpan(0xFFA500);
				break;
			case ERROR:
				colorSpan = new ForegroundColorSpan(Color.RED);
				break;
			case VERBOSE:
			default:
				colorSpan = new ForegroundColorSpan(Color.BLACK);
				break;
			}
			return colorSpan;
		}
		
	}
	
	public static void setTextView(TextView textView) {
		if (textViewLogger == null) {
			textViewLogger = new TextViewLogger();
		}
		textViewLogger.setTextView( textView);
	}
	
	public static TextView getTextView() {
		return textViewLogger.getTextView();
	}


}
