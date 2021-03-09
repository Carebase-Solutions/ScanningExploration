package com.carebase.carebasescanner;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.ViewModelProvider;

public class ScanningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        PreviewView viewFinder = findViewById(R.id.viewFinder);

        // set up preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());


        ScanningViewModel scanningViewModel = new ViewModelProvider(this).get(ScanningViewModel.class);
        scanningViewModel.setupCamera(this,this,preview);
    }
}
