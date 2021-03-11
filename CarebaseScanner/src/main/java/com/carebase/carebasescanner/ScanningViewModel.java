package com.carebase.carebasescanner;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import androidx.annotation.MainThread;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanningViewModel extends ViewModel {
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
        DETECTED
    }

    private final MutableLiveData<List<String>> scannedTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Barcode>> scannedBarcodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<ScanningState> stateLiveData = new MutableLiveData<>();

    private TextAnalyzer textAnalyzer;
    private BarcodeAnalyzer barcodeAnalyzer;

    public void setupCamera(Context context, LifecycleOwner owner, Preview preview) {
        // set up analyzers
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        textAnalyzer = new TextAnalyzer(this::onTextResult);
        ImageAnalysis textAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640,480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        textAnalysis.setAnalyzer(executorService, textAnalyzer);

        barcodeAnalyzer = new BarcodeAnalyzer(this::onBarcodeResult);
        ImageAnalysis barcodeAnalysis =  new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640,480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        barcodeAnalysis.setAnalyzer(executorService,barcodeAnalyzer);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // select back camera as default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

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
        stateLiveData.setValue(ScanningState.DETECTING);
        scannedTextLiveData.setValue(text);
    }

    public void onBarcodeResult(List<Barcode> barcodeList) {
//        stateLiveData.setValue(ScanningState.DETECTING);
        // to test loading animation
        stateLiveData.setValue(ScanningState.CONFIRMING);
        scannedBarcodeLiveData.setValue(barcodeList);
    }

    public MutableLiveData<List<String>> getScannedTextLiveData() {
        return scannedTextLiveData;
    }

    public LiveData<List<Barcode>> getScannedBarcodeLiveData() {
        return scannedBarcodeLiveData;
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

    @MainThread
    public void confirming(float progress) {
        boolean isConfirmed = (progress == 1f);
        if (isConfirmed) {
            stateLiveData.setValue(ScanningState.SEARCHING);
        } else {
            stateLiveData.setValue(ScanningState.CONFIRMING);
        }
    }
}
