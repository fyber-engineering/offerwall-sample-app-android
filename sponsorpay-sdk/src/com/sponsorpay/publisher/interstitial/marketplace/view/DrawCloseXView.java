package com.sponsorpay.publisher.interstitial.marketplace.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;

public class DrawCloseXView extends View {
    private Paint paint;
    private int densityDependantRadius;
    
    private static final int fixed_radius = 15;

    public DrawCloseXView(Context context, DisplayMetrics mDisplayMetrics) {
        super(context);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
		
		densityDependantRadius = (int) (fixed_radius * mDisplayMetrics.density);
    }
    
	@Override
    public void onDraw(Canvas canvas) {
    	canvas.drawLine(0, 0, densityDependantRadius, densityDependantRadius, paint);
		canvas.drawLine(densityDependantRadius, 0, 0, densityDependantRadius, paint);
	}
    
}