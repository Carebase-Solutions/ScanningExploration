package com.carebase.scannerexploration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.util.Objects;

/**
 * This fragment will display the results of the scan
 */
public class ResultFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analyzer,container,false);
        TextView fullAnalyzedTextView = rootView.findViewById(R.id.full_analyzed_text_view);
        PictureAnalysisActivity mainActivity = (PictureAnalysisActivity) requireActivity();
        String fileName = requireArguments().getString("file_name");

        // get file
        File photoFile = mainActivity.getPhotoFileUri(fileName);

        // get bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        // prepare input image
        int rotationAngle = PictureAnalysisActivity.getRotationAngle(photoFile.getAbsolutePath());
        InputImage image = InputImage.fromBitmap(bitmap, rotationAngle);

        // process image
        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String fullRecognizedText = visionText.getText();
                    Log.d(ResultFragment.class.getSimpleName(),fullRecognizedText);
                    fullAnalyzedTextView.setText(fullRecognizedText);
                })
                .addOnFailureListener(e -> {
                    Log.e(ResultFragment.class.getSimpleName(),"Image processing failed",e);
                    requireActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(getContext(),"Image processing failed", Toast.LENGTH_LONG).show();
                });
        Objects.requireNonNull(((PictureAnalysisActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        return rootView;
    }
}
