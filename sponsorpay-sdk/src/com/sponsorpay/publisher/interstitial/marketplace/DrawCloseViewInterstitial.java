package com.sponsorpay.publisher.interstitial.marketplace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

public class DrawCloseViewInterstitial extends View {
    Paint paint = new Paint();

    public DrawCloseViewInterstitial(Context context) {
        super(context);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }
    
    @SuppressLint("DrawAllocation")
	@Override
    public void onDraw(Canvas canvas) {
		Bitmap bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);
		Canvas cvs = new Canvas(bitmap);
		cvs.drawLine(0, 0, 40, 40, paint);
		cvs.drawLine(40, 0, 0, 40, paint);

		setBackgroundDrawable(new BitmapDrawable(getContext().getResources(), bitmap));
	}
    
}