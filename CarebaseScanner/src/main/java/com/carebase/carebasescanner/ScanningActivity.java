package com.carebase.carebasescanner;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.ViewModelProvider;

import com.google.mlkit.vision.barcode.Barcode;

public class ScanningActivity extends AppCompatActivity {
    private static final String TAG = ScanningActivity.class.getSimpleName();

    private ScanningViewModel scanningViewModel;

    private GraphicOverlay graphicOverlay;
    private CameraReticleAnimator cameraReticleAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        PreviewView viewFinder = findViewById(R.id.viewFinder);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        // set up preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        scanningViewModel = new ViewModelProvider(this).get(ScanningViewModel.class);
        scanningViewModel.setupCamera(this,this,preview);

        startGraphicOverlay();

        listenToBarcodeUpdates();
        listenToTextUpdates();
    }

    private void startGraphicOverlay() {
        cameraReticleAnimator = new CameraReticleAnimator(graphicOverlay);
        cameraReticleAnimator.start();
        ReticleGraphic reticleGraphic = new ReticleGraphic(graphicOverlay,cameraReticleAnimator);
        graphicOverlay.add(reticleGraphic);
    }

    private void listenToBarcodeUpdates() {
        scanningViewModel.getScannedBarcodeLiveData().observe(this,(barcodeList) -> {
            for (Barcode barcode : barcodeList) {
                Log.d(TAG,"Scanned barcode: \n" + barcode.getDisplayValue());
            }
        });
    }

    private void listenToTextUpdates() {
        scanningViewModel.getScannedTextLiveData().observe(this,(textList) -> {
            Log.d(TAG, "Scanned Text: \n");
            for (String text : textList) {
                Log.d("textAnalyze", text);
            }
        });
    }
}
