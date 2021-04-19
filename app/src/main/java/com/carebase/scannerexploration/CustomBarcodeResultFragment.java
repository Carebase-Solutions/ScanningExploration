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
        ProgressBar progressBar = view.findViewById(R.id.progress_indicator);
        scanningViewModel = new ViewModelProvider(requireActivity()).get(ScanningViewModel.class);

        Bundle arguments = getArguments();
        String text = Objects.requireNonNull(arguments).getString(ARG_UDI_FIELD) + " found";
        udiTextView.setText(text);
        udiFrameLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        scanningViewModel.setState(ScanningViewModel.ScanningState.DETECTED);
        return view;
    }

    private final static String TAG = "BarcodeResultFragment";
    private final static String ARG_UDI_FIELD = "arg_udi_field";

    public static void show(FragmentManager fragmentManager, String udi, OnDismissCallback onDismissCallback) {
        CustomBarcodeResultFragment customBarcodeResultFragment = new CustomBarcodeResultFragment(onDismissCallback);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_UDI_FIELD, udi);
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
