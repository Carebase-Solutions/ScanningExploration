package com.carebase.carebasescanner;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.mlkit.vision.barcode.Barcode;
import java.util.ArrayList;

public class ScanningActivity extends AppCompatActivity {
    private static final String TAG = ScanningActivity.class.getSimpleName();

    private ScanningViewModel scanningViewModel;

    private GraphicOverlay graphicOverlay;
    private CameraReticleAnimator cameraReticleAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener((view) -> finish());

        PreviewView viewFinder = findViewById(R.id.viewFinder);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        // set up preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        scanningViewModel = new ViewModelProvider(this).get(ScanningViewModel.class);
        scanningViewModel.setupCamera(this,this,preview);

        startGraphicOverlay();

        listenToBarcodeUpdates();
        listenForUDI();
        listenToTextUpdates();
    }

    private void startGraphicOverlay() {
        cameraReticleAnimator = new CameraReticleAnimator(graphicOverlay);
        ReticleGraphic reticleGraphic = new ReticleGraphic(graphicOverlay,cameraReticleAnimator);
        graphicOverlay.add(reticleGraphic);

        scanningViewModel.getStateLiveData().observe(this,state -> {
            // TODO handle confirming state
            if (state == ScanningViewModel.ScanningState.DETECTING || state == ScanningViewModel.ScanningState.CONFIRMING) {
                cameraReticleAnimator.start();
            } else {
                cameraReticleAnimator.cancel();
            }
        });
    }

    private void listenForUDI() {
        scanningViewModel.getScannedUDILiveData().observe(this, udi -> {
            Toast.makeText(this,udi,Toast.LENGTH_LONG).show();
            Log.d(TAG,"UDI: " + udi);
        });
    }

    private void listenToBarcodeUpdates() {
        scanningViewModel.getScannedBarcodeLiveData().observe(this,(barcodeList) -> {
            for (Barcode barcode : barcodeList) {
                if (barcode != null) {
                    ArrayList barcodeFieldList = new ArrayList<BarcodeField>();
                    barcodeFieldList.add(new BarcodeField("Raw Value", barcode.getRawValue()));
                    BarcodeResultFragment.Companion.show(getSupportFragmentManager(), barcodeFieldList);
                }
            }
//            if (scanningViewModel.getStateLiveData().getValue() == ScanningViewModel.ScanningState.SEARCHING) {
//                // display barcodes
//                for (Barcode barcode : barcodeList) {
//                    //
//                }
//            }
//            if (scanningViewModel.getStateLiveData().getValue() == ScanningViewModel.ScanningState.DETECTED) {
//                // display barcodes
//                for (Barcode barcode : barcodeList) {
//                    if (barcode != null) {
//                        ArrayList barcodeFieldList = new ArrayList<BarcodeField>();
//                        barcodeFieldList.add(new BarcodeField("Raw Value", barcode.getRawValue()));
//                        BarcodeResultFragment.Companion.show(getSupportFragmentManager(), barcodeFieldList);
//                    }
//                }
//            }
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
