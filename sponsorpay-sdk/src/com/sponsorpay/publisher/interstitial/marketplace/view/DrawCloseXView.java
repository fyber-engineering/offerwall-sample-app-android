/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.publisher.interstitial.marketplace.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;

public class DrawCloseXView extends View {
	public static final float STROKE_WIDTH = 1.5f;
	private static final int mFixed_radius = 15;
	// Range from 0 to 255, where 0 is fully transparent and 255 fully opaque.
	private static final int SEVENTY_PERCENT_OPAQUE = 178;
	private Paint mPaint;
	private int mDensityDependantRadius;
    

    public DrawCloseXView(Context context, DisplayMetrics mDisplayMetrics) {
        super(context);
        
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(1.5f);
		mPaint.setAlpha(SEVENTY_PERCENT_OPAQUE);
		mPaint.setColor(Color.WHITE);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);

		mDensityDependantRadius = (int) (mFixed_radius * mDisplayMetrics.density);
    }
    
	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawLine(0, 0, mDensityDependantRadius, mDensityDependantRadius, mPaint);
		canvas.drawLine(mDensityDependantRadius, 0, 0, mDensityDependantRadius, mPaint);
	}
    
}