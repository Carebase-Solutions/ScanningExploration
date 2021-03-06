package com.carebase.carebasescanner

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface OnDismissCallback {
    fun onDismiss()
}

open class BarcodeResultFragment(val onDismissCallback: OnDismissCallback) : BottomSheetDialogFragment() {

    override fun onCreateView(
            layoutInflater: LayoutInflater,
            viewGroup: ViewGroup?,
            bundle: Bundle?
    ): View {
        val view = layoutInflater.inflate(R.layout.bottom_sheet, viewGroup)
        val udiFieldLayout = view.findViewById<View>(R.id.udi_field_container)
        val typeFieldLayout = view.findViewById<View>(R.id.type_field_container)
        val udiTextView = view.findViewById<TextView>(R.id.udi_field_value)
        val typeTextView = view.findViewById<TextView>(R.id.type_field_value)

        val progressBar = view.findViewById<ProgressBar>(R.id.progress_indicator);
        val scanningViewModel = ViewModelProvider(requireActivity()).get(ScanningViewModel::class.java)

        val arguments = arguments
        val udi = arguments?.getString(ARG_UDI_FIELD)
        val type = arguments?.getString(ARG_TYPE_FIELD)

        udiTextView.text = udi
        typeTextView.text = type
        udiFieldLayout.visibility = View.VISIBLE
        typeFieldLayout.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        scanningViewModel.setState(ScanningViewModel.ScanningState.DETECTED)

        return view
    }

    override fun onDismiss(dialogInterface: DialogInterface) {
        // Back to working state after the bottom sheet is dismissed.
        super.onDismiss(dialogInterface)
        onDismissCallback.onDismiss();
    }

    companion object {

        private const val TAG = "BarcodeResultFragment"
        private const val ARG_UDI_FIELD = "arg_udi_field"
        private const val ARG_TYPE_FIELD = "arg_type_field"

        fun show(fragmentManager: FragmentManager, udi: String, type: String, onDismissCallback: OnDismissCallback) {
            val barcodeResultFragment = BarcodeResultFragment(onDismissCallback)
            val bundle = Bundle()
            bundle.putString(ARG_UDI_FIELD, udi)
            bundle.putString(ARG_TYPE_FIELD, type)
            barcodeResultFragment.arguments = bundle
            barcodeResultFragment.show(fragmentManager, TAG)
        }

        fun dismiss(fragmentManager: FragmentManager) {
            (fragmentManager.findFragmentByTag(TAG) as BarcodeResultFragment?)?.dismiss()
        }
    }
}