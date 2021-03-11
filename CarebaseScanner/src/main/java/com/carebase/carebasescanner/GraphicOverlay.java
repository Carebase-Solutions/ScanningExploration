package com.carebase.carebasescanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {
    private final Object lock = new Object();

    private int previewWidth = 0;
    private float widthScaleFactor = 1.0f;
    private int previewHeight = 0;
    private float heightScaleFactor = 1.0f;
    private List<Graphic> graphicList = new ArrayList<>();

    abstract static class Graphic {
        protected GraphicOverlay overlay;
        protected Context context;

        protected Graphic(GraphicOverlay overlay) {
           this.overlay = overlay;
           context = overlay.getContext();
        }

        abstract void draw(Canvas canvas);
    }

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void clear() {
        synchronized (lock) {
            graphicList.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphicList.add(graphic);
        }
    }

    public void setCameraInfo(PreviewView previewView) {
        previewWidth = previewView.getWidth();
        previewHeight = previewView.getHeight();
    }

    public Float translateX(Float x) {
        return x * widthScaleFactor;
    }

    public Float translateY(Float y) {
        return y * heightScaleFactor;
    }

    public RectF translateRect(Rect rect) {
        return new RectF(translateX(Integer.valueOf(rect.left).floatValue()),
        translateY(Integer.valueOf(rect.top).floatValue()),
        translateX(Integer.valueOf(rect.right).floatValue()),
        translateY(Integer.valueOf(rect.bottom).floatValue()));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            for (Graphic graphic : graphicList) {
                graphic.draw(canvas);
            }
        }
    }
}