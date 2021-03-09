package com.carebase.carebasescanner;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

public class ScanningActivity extends AppCompatActivity {
    private PreviewView viewFinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        viewFinder = findViewById(R.id.viewFinder);
    }
}
