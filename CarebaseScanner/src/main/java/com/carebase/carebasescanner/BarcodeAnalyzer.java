package com.carebase.carebasescanner;

import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = BarcodeAnalyzer.class.getSimpleName();

    public interface BarcodeAnalyzerListener {
        void update(List<Barcode> barcodeList, @Nullable String udi);
    }

    private final BarcodeAnalyzerListener barcodeAnalyzerListener;

    private final BarcodeScanner scanner;

    private final List<Barcode> udiBarcodes = new ArrayList<>();
    private final String[] udi = new String[]{null,null};

    public BarcodeAnalyzer(BarcodeAnalyzerListener barcodeAnalyzerListener) {
        this.barcodeAnalyzerListener = barcodeAnalyzerListener;
        scanner = BarcodeScanning.getClient();
    }

    @Override
    @ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            // Pass image to an ML Kit Vision API
            scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodeList) {
                        analyzeBarcodes(barcodeList);
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

    public void destroy() {
        scanner.close();
    }

    private void analyzeBarcodes(List<Barcode> barcodeList) {
        for (Barcode barcode : barcodeList) {
            String b = barcode.getDisplayValue();
            Log.d(TAG,"Scanned: " + b);
            if (isUDI(b)) {
                udiBarcodes.add(barcode);
                barcodeAnalyzerListener.update(udiBarcodes, b);
                return;
            }

            if (isDI(b)) {
                if (udi[0] == null) {
                    udiBarcodes.add(0,barcode);
                    udi[0] = b;
                }
            } else if (isPI(b)) {
                if (udi[1] == null) {
                    udiBarcodes.add(barcode);
                    udi[1] = b;
                }
            }
        }
        if (udi[0] != null && udi[1] != null) {
            if (isHIBCCDI(udi[0]) && isHIBCCPI(udi[1])){
                String udi = this.udi[0] + "/" + this.udi[1].substring(1);
                if (isHIBCCUDI(udi)) {
                    barcodeAnalyzerListener.update(udiBarcodes, udi);
                }
            }
            if (isGS1UDI(udi[0] + udi[1])) {
                barcodeAnalyzerListener.update(udiBarcodes, udi[0] + udi[1]);
            }
        }
        barcodeAnalyzerListener.update(udiBarcodes,null);
    }

    private boolean isDI(String barcode) {
        return isGS1DI(barcode) || isHIBCCDI(barcode);
    }

    private boolean isPI(String barcode) {
        return isGS1PI(barcode) || isHIBCCPI(barcode);
    }

    private boolean isUDI(String barcode) {
        return isGS1UDI(barcode) || isHIBCCUDI(barcode);
    }

    private static final String GS1DIREGEX = "01\\d{14}";
    private static final String GS1PIREGEX = "(11\\d{6})?17\\d{6}10[A-Za-z0-9]{2,20}(21[A-Za-z0-9]{2,20})?";
    private static final String GS1UDIREGEX = GS1DIREGEX + GS1PIREGEX;
    private final Pattern GS1DIPATTERN = Pattern.compile(GS1DIREGEX);
    private final Pattern GS1PIPATTERN = Pattern.compile(GS1PIREGEX);
    private final Pattern GS1UDIPATTERN = Pattern.compile(GS1UDIREGEX);

    private boolean isGS1DI(String barcode) {
        Matcher matcher = GS1DIPATTERN.matcher(barcode);
        return matcher.matches();
    }

    private boolean isGS1PI(String barcode) {
        Matcher matcher = GS1PIPATTERN.matcher(barcode);
        return matcher.matches();
    }

    private boolean isGS1UDI(String barcode) {
        Matcher matcher = GS1UDIPATTERN.matcher(barcode);
        return matcher.matches();
    }

    private static final String HIBCCDIREGEX = "\\+[A-Za-z0-9]{6,23}";
    private static final String HIBCCPIREGEX = "\\+([^_/]{6,30})(/([^_/]{6,30})?)*";
    private static final String HIBCCUDIREGEX = "\\+[A-Za-z0-9]{{6,23}(/([^_/]{6,30})?)+";
    private final Pattern HIBCCDIPATTERN = Pattern.compile(HIBCCDIREGEX);
    private final Pattern HIBCCPIPATTERN = Pattern.compile(HIBCCPIREGEX);
    private final Pattern HIBCCUDIPATTERN = Pattern.compile(HIBCCUDIREGEX);

    private boolean isHIBCCDI(String barcode) {
        Matcher matcher = HIBCCDIPATTERN.matcher(barcode);
        return matcher.matches();
    }

    private boolean isHIBCCPI(String barcode) {
        Matcher matcher = HIBCCPIPATTERN.matcher(barcode);
        return matcher.matches();
    }

    private boolean isHIBCCUDI(String barcode) {
        Matcher matcher = HIBCCUDIPATTERN.matcher(barcode);
        return matcher.matches();
    }

}
