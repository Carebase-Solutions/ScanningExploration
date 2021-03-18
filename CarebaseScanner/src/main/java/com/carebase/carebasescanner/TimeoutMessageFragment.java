package com.carebase.carebasescanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TimeoutMessageFragment extends Fragment {
    public static final String TAG = TimeoutMessageFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_timeout_message, container, false);

        Button learnMoreButton = rootView.findViewById(R.id.learnMoreButton);
        Button dismissButton = rootView.findViewById(R.id.dismissButton);

        learnMoreButton.setOnClickListener(view -> learnMore());
        dismissButton.setOnClickListener(view -> dismiss());

        return rootView;
    }

    private void learnMore() {
        // to be implemented
    }

    private void dismiss() {
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);
    }
}