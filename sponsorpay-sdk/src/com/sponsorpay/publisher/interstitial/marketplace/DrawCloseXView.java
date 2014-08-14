package com.sponsorpay.publisher.interstitial.marketplace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class DrawCloseXView extends View {
    private Paint paint;
    private int densityDependantRadius;
    
    private static final int fixed_radius = 15;

    public DrawCloseXView(Context context) {
        super(context);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
		
		densityDependantRadius = (int) (fixed_radius * mDisplayMetrics.density);
    }
    
	@Override
    public void onDraw(Canvas canvas) {
    	canvas.drawLine(0, 0, densityDependantRadius, densityDependantRadius, paint);
		canvas.drawLine(densityDependantRadius, 0, 0, densityDependantRadius, paint);
	}
    
}