/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.publisher.interstitial.marketplace.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

public class InterstitialCloseButtonRelativeLayout extends RelativeLayout{
	
	private static final int mCloseButtonGreyColor = Color.parseColor("#7F7F7F");
	// Range from 0 to 255, where 0 is fully transparent and 255 fully opaque.
	private static final int FIFTY_PERCENT_TRANSPARENCY = 128;
	private DisplayMetrics mMetrics;

	public InterstitialCloseButtonRelativeLayout(Context context) {
		super(context);
		// get the current display metrics
		mMetrics = context.getResources().getDisplayMetrics();

		createCloseButtonRelativeLayout(context);
	}
	
	private void createCloseButtonRelativeLayout(Context context){

		
		int fifteenDipInPixels = getPixelsFromDip(15);
		int thirtyDipInPixels = getPixelsFromDip(30);
		int sixtyDipInPixels = getPixelsFromDip(60);

		// Image with Drawable
		final ImageView imageView;
		imageView = new ImageView(context);

		// create a circle with diameter 30X30 dip and set the background color
		GradientDrawable circle = new GradientDrawable();
		circle.setColor(mCloseButtonGreyColor);
		circle.setShape(GradientDrawable.OVAL);
		circle.setAlpha(FIFTY_PERCENT_TRANSPARENCY);
		circle.setStroke((int) Math.round(DrawCloseXView.STROKE_WIDTH), Color.WHITE);
		circle.setSize(thirtyDipInPixels, thirtyDipInPixels);
		
		// set the Drawable into the center of the ImageView
		// and set 5 dip padding on each side.
		imageView.setImageDrawable(circle);
		imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ScaleType.CENTER);
		imageView.setPadding(fifteenDipInPixels, fifteenDipInPixels, fifteenDipInPixels, fifteenDipInPixels);

		DrawCloseXView drawView = new DrawCloseXView(context, mMetrics);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(fifteenDipInPixels, fifteenDipInPixels);
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		drawView.setLayoutParams(params);

		this.setLayoutParams(new FrameLayout.LayoutParams(sixtyDipInPixels, sixtyDipInPixels, Gravity.TOP | Gravity.RIGHT));
		this.addView(imageView);
		this.addView(drawView);
	}
	
	public int getPixelsFromDip(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mMetrics);
	}

}