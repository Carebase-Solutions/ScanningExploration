package com.carebase.scannerexploration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.carebase.carebasescanner.ScanningActivity;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_PERMISSIONS = 10;
    private final static String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.live_scan_button).setOnClickListener(this::startLiveScan);
        findViewById(R.id.picture_scan_button).setOnClickListener(this::startPictureScan);

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void startLiveScan(View v) {
        startActivity(new Intent(this, CustomScanningActivity.class));
    }

    public void startPictureScan(View v) {
        startActivity(new Intent(this,PictureAnalysisActivity.class));
    }

    private boolean allPermissionsGranted() {
        boolean permissionsGranted = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getBaseContext(),permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = false;
            }
        }
        return permissionsGranted;
    }
}

