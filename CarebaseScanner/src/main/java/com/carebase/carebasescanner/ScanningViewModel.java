package com.carebase.carebasescanner;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Size;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanningViewModel extends ViewModel implements Serializable {
    private static final String TAG = ScanningViewModel.class.getSimpleName();

    /**
     * States that the scanner can be in
     */
    public enum ScanningState {
        /**
         * The scanner is looking for information in the frames.
         */
        DETECTING,
        /**
         * The scanner has found incomplete information and more frames
         * or user is required to make changes to their view.
         */
        CONFIRMING,
        /**
         * The scanner has marked its results and is searching for the product of
         * that result.
         */
        SEARCHING,
        /**
         * The scanner has found the product of its scan and finished scanning.
         */
        DETECTED,
        /**
         * The scanner has not found reliable barcodes or text in the allotted time
         */
        TIMEOUT
    }

    private final MutableLiveData<List<String>> scannedTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Barcode>> scannedBarcodesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> scannedUDILiveData = new MutableLiveData<>();
    private final MutableLiveData<ScanningState> stateLiveData = new MutableLiveData<>();

    private TextAnalyzer textAnalyzer;
    private BarcodeAnalyzer barcodeAnalyzer;

    private ImageAnalysis textAnalysis;
    private ImageAnalysis barcodeAnalysis;

    private ProcessCameraProvider cameraProvider;
    private LifecycleOwner owner;
    private CameraSelector cameraSelector;
    private Preview preview;

    protected CountDownTimer countDownTimer;
    private boolean countDownStarted;

    public void setupCamera(Context context, LifecycleOwner owner, Preview preview) {
        // set up analyzers
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        textAnalyzer = new TextAnalyzer(this::onTextResult);
        textAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640,480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        textAnalysis.setAnalyzer(executorService, textAnalyzer);

        barcodeAnalyzer = new BarcodeAnalyzer(this::onBarcodeResult);
        barcodeAnalysis =  new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640,480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        barcodeAnalysis.setAnalyzer(executorService,barcodeAnalyzer);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                this.owner = owner;
                this.preview = preview;

                // select back camera as default
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // unbind use cases before rebinding
                cameraProvider.unbindAll();

                // bind use cases to camera
                cameraProvider.bindToLifecycle(owner, cameraSelector, preview, barcodeAnalysis);

            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));

    }

    public void onTextResult(List<String> text) {
        scannedTextLiveData.setValue(text);
    }

    public void onBarcodeResult(List<Barcode> barcodeList, @Nullable String udi, BarcodeAnalyzer.State state) {
        if (state == BarcodeAnalyzer.State.DETECTING) {
            stateLiveData.setValue(ScanningState.DETECTING);
            if (!countDownStarted) { startTimeoutCountDown(); }
        } else {
            cancelTimeoutCountDown();
            // partial udi is found
            if (state == BarcodeAnalyzer.State.CONFIRMING) {
                scannedBarcodesLiveData.setValue(barcodeList);
            }
            // udi is found
            else if (state == BarcodeAnalyzer.State.CONFIRMED) {
                stateLiveData.setValue(ScanningState.CONFIRMING);
                scannedBarcodesLiveData.setValue(barcodeList);
                scannedUDILiveData.setValue(udi);
                barcodeAnalysis.clearAnalyzer();
            }
        }
    }

    public MutableLiveData<List<String>> getScannedTextLiveData() {
        return scannedTextLiveData;
    }

    public LiveData<List<Barcode>> getScannedBarcodeLiveData() {
        return scannedBarcodesLiveData;
    }

    public LiveData<String> getScannedUDILiveData() {
        return scannedUDILiveData;
    }

    public LiveData<ScanningState> getStateLiveData() {
        return stateLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        textAnalyzer.destroy();
        barcodeAnalyzer.destroy();
    }

    public void confirming(float progress) {
        boolean isConfirmed = (progress == 1f);
        if (isConfirmed) {
            stateLiveData.setValue(ScanningState.SEARCHING);
        }
    }

    private void startTimeoutCountDown() {
        countDownStarted = true;
        // hardcode 1 minute in ms
        long timeoutMs = Long.valueOf(60000);
        // interval is 1 sec
        countDownTimer = new CountDownTimer(timeoutMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
//                Log.d("timeout", "seconds remaining: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                countDownStarted = false;
                stateLiveData.setValue(ScanningState.TIMEOUT);
                cameraProvider.unbindAll();
            }
        };
        countDownTimer.start();
    }

    protected void cancelTimeoutCountDown() {
        countDownStarted = false;
        if (countDownTimer != null) countDownTimer.cancel();
    }

    protected void restartUseCase() {
        cancelTimeoutCountDown();
        // bind use cases to camera
        cameraProvider.bindToLifecycle(owner, cameraSelector, preview, barcodeAnalysis);
    }
}
