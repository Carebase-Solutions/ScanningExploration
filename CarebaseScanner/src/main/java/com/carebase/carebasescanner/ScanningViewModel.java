package com.carebase.carebasescanner;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScanningViewModel extends ViewModel {
    private MutableLiveData<String> scannedTextLiveData;
    private MutableLiveData<String> scannedBarcodeLiveData;

    public void setupCamera() {

    }
}
