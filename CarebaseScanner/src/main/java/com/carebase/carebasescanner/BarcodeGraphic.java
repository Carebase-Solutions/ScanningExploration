package com.carebase.carebasescanner;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class BarcodeGraphic extends GraphicOverlay.Graphic {

    private final Paint borderPaint;
    private final Paint fillPaint;
    private final Rect boundingRect;

    protected BarcodeGraphic(GraphicOverlay overlay, Rect boundingRect) {
        super(overlay);
        Resources resources = overlay.getResources();

        this.boundingRect = boundingRect;

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Integer.valueOf(resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_width)).floatValue());
        borderPaint.setColor(ContextCompat.getColor(context,R.color.white));

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(ContextCompat.getColor(context,R.color.white));
        fillPaint.setAlpha(50);
    }

    @Override
    void draw(Canvas canvas) {
        Log.d(BarcodeGraphic.class.getSimpleName(),"Barcode Graphic: width: " + canvas.getWidth() + " height: " + canvas.getHeight());
        Log.d(BarcodeGraphic.class.getSimpleName(),"Barcode Rectangle: " + boundingRect.toShortString());
        float widthScale = Integer.valueOf(canvas.getWidth()).floatValue() / 960f;
        float heightScale = Integer.valueOf(canvas.getHeight()).floatValue() / 1280f;
        Log.d(BarcodeGraphic.class.getSimpleName(),"heightScale: " + heightScale);
        Log.d(BarcodeGraphic.class.getSimpleName(),"widthScale: " + widthScale);
        RectF scaledBoundingRect = new RectF(
                (Integer.valueOf(boundingRect.left).floatValue() * widthScale),
                (Integer.valueOf(boundingRect.top).floatValue() * heightScale),
                (Integer.valueOf(boundingRect.right).floatValue() * widthScale),
                (Integer.valueOf(boundingRect.bottom).floatValue() * heightScale)
        );

        canvas.drawRect(scaledBoundingRect,borderPaint);
        canvas.drawRect(scaledBoundingRect,fillPaint);
    }
}
