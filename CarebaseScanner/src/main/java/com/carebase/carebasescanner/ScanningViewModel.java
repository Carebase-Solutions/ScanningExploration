package com.carebase.carebasescanner;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScanningViewModel extends ViewModel {
    private static final String TAG = ScanningViewModel.class.getSimpleName();

    private MutableLiveData<String> scannedTextLiveData;
    private MutableLiveData<String> scannedBarcodeLiveData;

    public void setupCamera(Context context, LifecycleOwner owner, Preview preview) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // select back camera as default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // set up analyzers
//                ImageAnalysis textAnalyzer = new ImageAnalysis.Builder().build();
//                textAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor(),new TextAnalyzer((text) -> {
//                    scannedTextLiveData.setValue(text);
//                }));

//                ImageAnalysis barcodeAnalyzer = new ImageAnalysis.Builder().build();
//                textAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor(),new BarcodeAnalyzer((barcode) -> {
//                    scannedTextLiveData.setValue(barcode);
//                }));



                // unbind use cases before rebinding
                cameraProvider.unbindAll();

                // bind use cases to camera
                cameraProvider.bindToLifecycle(owner,cameraSelector,preview);
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }
}
