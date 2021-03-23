package com.carebase.carebasescanner;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener((view) -> onFinish());

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

    private void onFinish() {
        scanningViewModel.countDownTimer.cancel();
        finish();
    }

    private void startGraphicOverlay() {
        cameraReticleAnimator = new CameraReticleAnimator(graphicOverlay);
        ReticleGraphic reticleGraphic = new ReticleGraphic(graphicOverlay,cameraReticleAnimator);

        ObjectConfirmationController confirmationController = new ObjectConfirmationController(graphicOverlay);
        LoaderReticleGraphic loaderReticleGraphic = new LoaderReticleGraphic(graphicOverlay, confirmationController);

        scanningViewModel.getStateLiveData().observe(this,state -> {
            // TODO handle confirming state
            if (state == ScanningViewModel.ScanningState.DETECTING) {
                if (!graphicOverlay.contains(reticleGraphic)) {
                    graphicOverlay.add(reticleGraphic);
                }
                cameraReticleAnimator.start();
            } else if (state == ScanningViewModel.ScanningState.CONFIRMING) {
                graphicOverlay.clear();
                graphicOverlay.add(loaderReticleGraphic);
                // start loading animation
                confirmationController.confirming();
                scanningViewModel.confirming(confirmationController.getProgress());
            } else if (state == ScanningViewModel.ScanningState.TIMEOUT) {
                graphicOverlay.clear();
                showTimeoutMessage();
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
            if (scanningViewModel.getStateLiveData().getValue() == ScanningViewModel.ScanningState.SEARCHING) {
                // display barcodes
                for (Barcode barcode : barcodeList) {
                    //
                }
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

    public void showTimeoutMessage() {
        Fragment fragment = new TimeoutMessageFragment(scanningViewModel::restartUseCases);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_scanning, fragment, TimeoutMessageFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

}
