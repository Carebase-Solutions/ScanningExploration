package com.carebase.carebasescanner;

import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = TextAnalyzer.class.getSimpleName();

    private InputImage image;
    private TextRecognizer recognizer;

    public interface TextAnalyzerListener {
        void update(List<String> textList);
    }
    private final TextAnalyzerListener textAnalyzerListener;

    public TextAnalyzer(TextAnalyzer.TextAnalyzerListener textAnalyzerListener) {
        this.textAnalyzerListener = textAnalyzerListener;
    }

    @Override
    @ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            recognizer = TextRecognition.getClient();
            runTextRecognition(imageProxy);
        }
    }

    private void runTextRecognition(ImageProxy imageProxy) {
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        List<String> textList = processTextRecognitionResult(visionText);
                        if (!textList.isEmpty()) {
                            textAnalyzerListener.update(textList);
                        }
                        imageProxy.close();
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG,"Text Scanning Failed: ", e);
                                imageProxy.close();
                            }
                        });
    }

    private List<String> processTextRecognitionResult(Text visionText) {
        List<String> allText = new ArrayList<>();
        List<Text.TextBlock> blocks = visionText.getTextBlocks();
        if (blocks.size() == 0) {
            Log.d("TextAnalyzer", "No Text Found");
            return null;
        }
        // graphicOverlay.clear(); //clear any text previously displayed on the screen
        for (Text.TextBlock block: blocks) {
            List<Text.Line> lines = block.getLines();
            for (Text.Line line: lines) {
                allText.add(line.getText());
                // if want to get elements(words) instead of lines
//                List<Text.Element> elements = line.getElements();
//                for (Text.Element element: elements) {
//                    allText.add(element.getText());
//                }
            }
        }
        return allText;
    }
}
