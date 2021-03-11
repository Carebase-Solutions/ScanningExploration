package com.carebase.carebasescanner;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

public class ReticleGraphic extends GraphicOverlay.Graphic {
    private final CameraReticleAnimator animator;

    private final Paint outerRingFillPaint;
    private final Paint outerRingStrokePaint;
    private final Paint innerRingStrokePaint;
    private final Paint ripplePaint;
    private final int outerRingFillRadius;
    private final int outerRingStrokeRadius;
    private final int innerRingStrokeRadius;
    private final int rippleSizeOffset;
    private final int rippleStrokeWidth;
    private final int rippleAlpha;

    public ReticleGraphic(GraphicOverlay overlay, CameraReticleAnimator animator) {
        super(overlay);

        this.animator = animator;

        Resources resources = overlay.getResources();

        outerRingFillPaint = new Paint();
        outerRingFillPaint.setStyle(Paint.Style.FILL);
        outerRingFillPaint.setColor(ContextCompat.getColor(context, R.color.object_reticle_outer_ring_fill));

        outerRingStrokePaint = new Paint();
        outerRingStrokePaint.setStyle(Paint.Style.STROKE);
        outerRingStrokePaint.setStrokeWidth(Integer.valueOf(resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_width)).floatValue());
        outerRingStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        outerRingStrokePaint.setColor(ContextCompat.getColor(context, R.color.object_reticle_outer_ring_stroke));

        innerRingStrokePaint = new Paint();
        innerRingStrokePaint.setStyle(Paint.Style.STROKE);
        innerRingStrokePaint.setStrokeWidth(Integer.valueOf(resources.getDimensionPixelOffset(R.dimen.object_reticle_inner_ring_stroke_width)).floatValue());
        innerRingStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        innerRingStrokePaint.setColor(ContextCompat.getColor(context, R.color.white));

        ripplePaint = new Paint();
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setColor(ContextCompat.getColor(context, R.color.reticle_ripple));

        outerRingFillRadius = resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_fill_radius);
        outerRingStrokeRadius = resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_radius);
        innerRingStrokeRadius = resources.getDimensionPixelOffset(R.dimen.object_reticle_inner_ring_stroke_radius);
        rippleSizeOffset = resources.getDimensionPixelOffset(R.dimen.object_reticle_ripple_size_offset);
        rippleStrokeWidth = resources.getDimensionPixelOffset(R.dimen.object_reticle_ripple_stroke_width);
        rippleAlpha = ripplePaint.getAlpha();
    }

    @Override
    public void draw(Canvas canvas) {
       float cx = canvas.getWidth() / 2f;
       float cy = canvas.getHeight() / 2f;
       canvas.drawCircle(cx,cy,
               Integer.valueOf(outerRingFillRadius).floatValue(),
               outerRingFillPaint);
       canvas.drawCircle(cx,cy,
               Integer.valueOf(outerRingStrokeRadius).floatValue(),
               outerRingStrokePaint);
       canvas.drawCircle(cx,cy,
               Integer.valueOf(innerRingStrokeRadius).floatValue(),
               innerRingStrokePaint);

       // draws the ripple to simulate the breathing animation effect
       ripplePaint.setAlpha(Float.valueOf(rippleAlpha * animator.getRippleAlphaScale()).intValue());
       ripplePaint.setStrokeWidth(rippleStrokeWidth * animator.getRippleStrokeWidthScale());
       float radius = outerRingStrokeRadius + rippleSizeOffset * animator.getRippleSizeScale();
       canvas.drawCircle(cx,cy,radius,ripplePaint);
    }
}
