package com.carebase.scannerexploration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This fragment will display the results of the scan
 */
public class ResultFragment extends Fragment {

    private TextAnalyzerLineAdapter lineAdapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analyzer, container, false);

        PictureAnalysisActivity parent = (PictureAnalysisActivity) requireActivity();

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        lineAdapter = new TextAnalyzerLineAdapter();
        recyclerView.setAdapter(lineAdapter);

        MaterialButton confirmButton = rootView.findViewById(R.id.confirm_button);
        confirmButton.setEnabled(false);
        confirmButton.setOnClickListener(view -> parent.onSaveAnnotations(lineAdapter.getTextList()));

        MaterialToolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener((view) -> requireActivity().getSupportFragmentManager().popBackStack());

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
                    List<Pair<String, Boolean>> lines = visionText.getTextBlocks().stream()
                            .map(textBlock -> new Pair<String, Boolean>(textBlock.getText(), false))
                            .collect(Collectors.toList());
                    Log.d(ResultFragment.class.getSimpleName(), lines.toString());
                    lineAdapter.setTextList(lines);
                    lineAdapter.notifyDataSetChanged();
                    confirmButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(ResultFragment.class.getSimpleName(), "Image processing failed", e);
                    requireActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(getContext(), "Image processing failed", Toast.LENGTH_LONG).show();
                });
        return rootView;
    }


}
