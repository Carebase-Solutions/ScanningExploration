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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = BarcodeAnalyzer.class.getSimpleName();

    public interface BarcodeAnalyzerListener {
        void update(List<Barcode> barcodeList, State state);
    }

    private final BarcodeAnalyzerListener barcodeAnalyzerListener;

    private final BarcodeScanner scanner;

    private final List<Barcode> udiBarcodes = new ArrayList<>();
    private final String[] udi = new String[]{null,null};

    public enum State {
        DETECTING,
        CONFIRMING,
        CONFIRMED,
    }

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
            if (isGS1UDI(b)) {
                udiBarcodes.add(barcode);
                barcodeAnalyzerListener.update(udiBarcodes, State.CONFIRMED);
                return;
            }

            if (isGS1DI(b)) {
                if (udi[0] == null) {
                    udiBarcodes.add(0,barcode);
                    udi[0] = b;
                }
            } else if (isGS1PI(b)) {
                if (udi[1] == null) {
                    udiBarcodes.add(barcode);
                    udi[1] = b;
                }
            }
        }
        if (udi[0] != null && udi[1] != null && isGS1UDI(udi[0] + udi[1])) {
            barcodeAnalyzerListener.update(udiBarcodes, State.CONFIRMED);
        }
        else if (udi[0] != null || udi[1] != null) {
            barcodeAnalyzerListener.update(udiBarcodes, State.CONFIRMING);
        }
        barcodeAnalyzerListener.update(udiBarcodes,State.DETECTING);
    }

    private static final String GS1DIREGEX = "01\\d{14}";
    private static final String GS1PIREGEX = "(11\\d{6})?17\\d{6}10(\\w[^_]){2,20}(21(\\w[^_]){2,20})?";
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

    private String getGS1DIString(String udi) {
        Matcher matcher = GS1DIPATTERN.matcher(udi);
        return matcher.group();
    }

    private String getGS1PIString(String udi) {
        Matcher matcher = GS1PIPATTERN.matcher(udi);
        return matcher.group();
    }

    private boolean isGS1UDI(String barcode) {
        Matcher matcher = GS1UDIPATTERN.matcher(barcode);
        return matcher.matches();
    }
}
