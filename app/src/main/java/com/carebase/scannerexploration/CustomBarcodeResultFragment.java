package com.carebase.scannerexploration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.carebase.carebasescanner.BarcodeAnalyzer;
import com.carebase.carebasescanner.BarcodeResultFragment;
import com.carebase.carebasescanner.OnDismissCallback;
import com.carebase.carebasescanner.ScanningViewModel;

import java.util.Objects;

public class CustomBarcodeResultFragment extends BarcodeResultFragment {

    private ScanningViewModel scanningViewModel;

    public CustomBarcodeResultFragment(OnDismissCallback onDismissCallback) {
        super(onDismissCallback);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.bottom_sheet,viewGroup,false);
        FrameLayout udiFrameLayout = view.findViewById(R.id.udi_field_container);
        TextView udiTextView = view.findViewById(R.id.udi_field_value);
        FrameLayout typeFrameLayout = view.findViewById(R.id.type_field_container);
        TextView typeTextView = view.findViewById(R.id.type_field_value);
        ProgressBar progressBar = view.findViewById(R.id.progress_indicator);
        scanningViewModel = new ViewModelProvider(requireActivity()).get(ScanningViewModel.class);

        Bundle arguments = getArguments();
        String udi = Objects.requireNonNull(arguments).getString(ARG_UDI_FIELD) + " found";
        String type = Objects.requireNonNull(arguments).getString(ARG_TYPE_FIELD);
        udiTextView.setText(udi);
        udiFrameLayout.setVisibility(View.VISIBLE);
        typeTextView.setText(type);
        typeFrameLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        scanningViewModel.setState(ScanningViewModel.ScanningState.DETECTED);
        return view;
    }

    private final static String TAG = "BarcodeResultFragment";
    private final static String ARG_UDI_FIELD = "arg_udi_field";
    private final static String ARG_TYPE_FIELD = "arg_type_field";

    public static void show(FragmentManager fragmentManager, String udi, String type, OnDismissCallback onDismissCallback) {
        CustomBarcodeResultFragment customBarcodeResultFragment = new CustomBarcodeResultFragment(onDismissCallback);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_UDI_FIELD, udi);
        bundle.putString(ARG_TYPE_FIELD, type);
        customBarcodeResultFragment.setArguments(bundle);
        customBarcodeResultFragment.show(fragmentManager, TAG);
    }

    public static void dismiss(FragmentManager fragmentManager) {
        BarcodeResultFragment fragment = ((BarcodeResultFragment) fragmentManager.findFragmentByTag(TAG));
        if (fragment != null) {
            fragment.dismiss();
        }
    }

}
