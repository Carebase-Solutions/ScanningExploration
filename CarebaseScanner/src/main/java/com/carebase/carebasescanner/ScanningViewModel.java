package com.carebase.carebasescanner;

import android.content.Context;
import android.util.Log;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScanningViewModel extends ViewModel {
    private static final String TAG = ScanningViewModel.class.getSimpleName();

    private final MutableLiveData<List<String>> scannedTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Barcode>> scannedBarcodeLiveData = new MutableLiveData<>();

    public void setupCamera(Context context, LifecycleOwner owner, Preview preview) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // select back camera as default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // set up analyzers
                ImageAnalysis textAnalyzer = new ImageAnalysis.Builder().build();
                textAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor(), new TextAnalyzer((scannedTextLiveData::setValue)));

                ImageAnalysis barcodeAnalyzer = new ImageAnalysis.Builder().build();
                barcodeAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor(),new BarcodeAnalyzer(scannedBarcodeLiveData::setValue));

                // unbind use cases before rebinding
                cameraProvider.unbindAll();

                // bind use cases to camera
                cameraProvider.bindToLifecycle(owner, cameraSelector, preview, textAnalyzer);
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public LiveData<List<Barcode>> getScannedBarcodeLiveData() {
        return scannedBarcodeLiveData;
    }

    public LiveData<List<String>> getScannedTextLiveData() {
        return scannedTextLiveData;
    }
}
