package com.carebase.carebasescanner;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * Controls the progress of object confirmation before performing additional operation on the
 * detected object.
 */
public class ObjectConfirmationController {
    private final GraphicOverlay graphicOverlay;

    private final CountDownTimer countDownTimer;

    /** Returns the confirmation progress described as a float value in the range of [0, 1].  */
    private float progress = 0f;

    public float getProgress() {
        return progress;
    }

    private boolean isConfirmed;

    public boolean getIsConfirmed() {
        return progress == 1f;
    }

    /**
     * @param graphicOverlay Used to refresh camera overlay when the confirmation progress updates.
     */
    public ObjectConfirmationController(GraphicOverlay graphicOverlay) {
        this.graphicOverlay = graphicOverlay;
        long confirmationTimeMs = Long.valueOf(1000);
        countDownTimer = new CountDownTimer(confirmationTimeMs, 20) {
            @Override
            public void onTick(long millisUntilFinished) {
                progress = ((float) confirmationTimeMs - millisUntilFinished) / confirmationTimeMs;
                graphicOverlay.invalidate();
            }

            @Override
            public void onFinish() {
                progress = 1f;
            }
        };
    }

    public void confirming() {
        reset();
        countDownTimer.start();
    }

    public void reset() {
        countDownTimer.cancel();
        progress = 0f;
    }
}