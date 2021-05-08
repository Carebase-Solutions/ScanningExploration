package com.carebase.scannerexploration;

import com.carebase.carebasescanner.BarcodeAnalyzer;
import com.carebase.carebasescanner.BarcodeResultFragment;
import com.carebase.carebasescanner.ScanningActivity;

public class CustomScanningActivity extends ScanningActivity {
    private static final String TAG = CustomScanningActivity.class.getSimpleName();

    @Override
    public void showBottomSheet(String udi, BarcodeAnalyzer.BarcodeType type) {
        CustomBarcodeResultFragment.show(getSupportFragmentManager(), udi, type, this::restartUseCases);
        scanningViewModel.clearUseCases();
    }
}
