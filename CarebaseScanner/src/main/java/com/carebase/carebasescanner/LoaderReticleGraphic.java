package com.carebase.carebasescanner;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

/**
 * Similar to the camera reticle but with additional progress ring to indicate an object is getting
 * confirmed for a follow up processing, e.g. barcode searching.
 */
public class LoaderReticleGraphic extends GraphicOverlay.Graphic {

    private final ObjectConfirmationController confirmationController;

    private final Paint outerRingFillPaint;
    private final Paint outerRingStrokePaint;
    private final Paint progressRingStrokePaint;
    private final Paint innerRingPaint;

    private final int outerRingFillRadius;
    private final int outerRingStrokeRadius;
    private final int innerRingStrokeRadius;

    public LoaderReticleGraphic(GraphicOverlay overlay, ObjectConfirmationController confirmationController) {
        super(overlay);

        this.confirmationController = confirmationController;

        Resources resources = overlay.getResources();

        outerRingFillPaint = new Paint();
        outerRingFillPaint.setStyle(Paint.Style.FILL);
        outerRingFillPaint.setColor(ContextCompat.getColor(context, R.color.object_reticle_outer_ring_fill));

        outerRingStrokePaint = new Paint();
        outerRingStrokePaint.setStyle(Paint.Style.STROKE);
        outerRingStrokePaint.setStrokeWidth(Integer.valueOf(resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_width)).floatValue());
        outerRingStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        outerRingStrokePaint.setColor(ContextCompat.getColor(context, R.color.object_reticle_outer_ring_stroke));

        progressRingStrokePaint = new Paint();
        progressRingStrokePaint.setStyle(Paint.Style.STROKE);
        progressRingStrokePaint.setStrokeWidth(Integer.valueOf(resources.getDimensionPixelOffset(R.dimen.object_reticle_progress_ring_stroke_width)).floatValue());
        progressRingStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        progressRingStrokePaint.setColor(ContextCompat.getColor(context, R.color.white));

        innerRingPaint = new Paint();
        innerRingPaint.setStyle(Paint.Style.STROKE);
        innerRingPaint.setStrokeWidth(Integer.valueOf(resources.getDimensionPixelOffset(R.dimen.object_reticle_inner_ring_stroke_width)).floatValue());
        innerRingPaint.setStrokeCap(Paint.Cap.ROUND);
        innerRingPaint.setColor(ContextCompat.getColor(context, R.color.white));

        outerRingFillRadius = resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_fill_radius);
        outerRingStrokeRadius = resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_radius);
        innerRingStrokeRadius = resources.getDimensionPixelOffset(R.dimen.object_reticle_inner_ring_stroke_radius);

    }

    @Override
    void draw(Canvas canvas) {
        float cx = canvas.getWidth() / 2f;
        float cy = canvas.getHeight() / 2f;
        canvas.drawCircle(cx,cy,
                Integer.valueOf(outerRingFillRadius).floatValue(), outerRingFillPaint);
        canvas.drawCircle(cx,cy,
                Integer.valueOf(outerRingStrokeRadius).floatValue(), outerRingStrokePaint);
        canvas.drawCircle(cx,cy,
                Integer.valueOf(innerRingStrokeRadius).floatValue(), innerRingPaint);

        RectF progressRect = new RectF(
                cx - outerRingStrokeRadius,
                cy - outerRingStrokeRadius,
                cx + outerRingStrokeRadius,
                cy + outerRingStrokeRadius
        );

        float sweepAngle = confirmationController.getProgress() * 360;
        canvas.drawArc(progressRect, 0f, sweepAngle, false, progressRingStrokePaint);
    }
}
