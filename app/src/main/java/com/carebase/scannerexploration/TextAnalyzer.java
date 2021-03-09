package com.carebase.scannerexploration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextAnalyzer {
    private View view;
    private ImageProxy imageProxy;
    private int degree;
    private Button analyzeButton;

    private FirebaseVisionImage image;
    private FirebaseVisionTextRecognizer detector;

    public TextAnalyzer(View view, ImageProxy imageProxy, int degree, Button analyzeButton) {
        this.view = view;
        this.imageProxy = imageProxy;
        this.degree = degree;
        this.analyzeButton = analyzeButton;

        getFirebaseVisionImage(); // set image
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer(); // set detector
    }

    private void runTextRecognition() {
        analyzeButton.setEnabled(false);
        detector.processImage(image)
                .addOnSuccessListener(
                    new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            analyzeButton.setEnabled(true);
                            processTextRecognitionResult(firebaseVisionText);
                        }
                    })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                analyzeButton.setEnabled(true);
                                Toast.makeText(view.getContext(), "Text Recognition Failed", Toast.LENGTH_LONG).show();
                                Log.e("TextAnalyzer", "Text Recognition Failed");
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(view.getContext(), "No Text Found", Toast.LENGTH_LONG).show();
            Log.d("TextAnalyzer", "No Text Found");
            return;
        }
        // graphicOverlay.clear(); //clear any text previously displayed on the screen
        List<String> allText = new ArrayList<>();
        for (FirebaseVisionText.TextBlock block: blocks) {
            List<FirebaseVisionText.Line> lines = block.getLines();
            for (FirebaseVisionText.Line line: lines) {
                List<FirebaseVisionText.Element> elements = line.getElements();
                for (FirebaseVisionText.Element element: elements) {
                    Log.d("TextAnalyzer", element.getText());
                    allText.add(element.getText());
                }
            }
        }
    }

    private void getFirebaseVisionImage() {
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();
        int rotation = degreesToFirebaseRotation();
        image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
    }

    private int degreesToFirebaseRotation() {
        switch (degree) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }
}
