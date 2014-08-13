package com.sponsorpay.publisher.interstitial.marketplace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawCloseXView extends View {
    private Paint paint;

    public DrawCloseXView(Context context) {
        super(context);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }
    
	@Override
    public void onDraw(Canvas canvas) {
    	canvas.drawLine(0, 0, 30, 30, paint);
		canvas.drawLine(30, 0, 0, 30, paint);
	}
    
}