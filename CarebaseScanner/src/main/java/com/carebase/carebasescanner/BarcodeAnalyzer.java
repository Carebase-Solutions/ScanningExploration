package com.carebase.carebasescanner;

import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = BarcodeAnalyzer.class.getSimpleName();

    public interface BarcodeAnalyzerListener {
        void update(List<Barcode> barcodeList);
    }
    private final BarcodeAnalyzerListener barcodeAnalyzerListener;

    public BarcodeAnalyzer(BarcodeAnalyzerListener barcodeAnalyzerListener) {
        this.barcodeAnalyzerListener = barcodeAnalyzerListener;
    }

    @Override
    @ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScanner scanner = BarcodeScanning.getClient();

            // Pass image to an ML Kit Vision API
            scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodeList) {
                        if (!barcodeList.isEmpty()) {
                            barcodeAnalyzerListener.update(barcodeList);
                        }
                        imageProxy.close();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Barcode Scanning Failed: ", e);
                        imageProxy.close();
                    }
                });
        }
    }
}
