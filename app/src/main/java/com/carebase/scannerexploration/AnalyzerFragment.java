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

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.util.Objects;

public class AnalyzerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analyzer,container,false);
        TextView fullAnalyzedTextView = rootView.findViewById(R.id.full_analyzed_text_view);
        MainActivity mainActivity = (MainActivity) requireActivity();
        String fileName = requireArguments().getString("file_name");

        // get file
        File photoFile = mainActivity.getPhotoFileUri(fileName);

        // get bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        // prepare input image
        int rotationAngle = MainActivity.getRotationAngle(photoFile.getAbsolutePath());
        InputImage image = InputImage.fromBitmap(bitmap, rotationAngle);

        // process image
        TextRecognizer recognizer = TextRecognition.getClient();
        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String fullRecognizedText = visionText.getText();
                    Log.d(AnalyzerFragment.class.getSimpleName(),fullRecognizedText);
                    fullAnalyzedTextView.setText(fullRecognizedText);
                })
                .addOnFailureListener(e -> {
                    Log.e(AnalyzerFragment.class.getSimpleName(),"Image processing failed",e);
                    requireActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(getContext(),"Image processing failed", Toast.LENGTH_LONG).show();
                });
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        return rootView;
    }
}
