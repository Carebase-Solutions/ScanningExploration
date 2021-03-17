package com.carebase.carebasescanner

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BarcodeResultFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
            layoutInflater: LayoutInflater,
            viewGroup: ViewGroup?,
            bundle: Bundle?
    ): View {
        val view = layoutInflater.inflate(R.layout.barcode_bottom_sheet, viewGroup)

        val arguments = arguments
        val barcodeFieldList: List<BarcodeField> =
                if (arguments?.containsKey(ARG_BARCODE_FIELD_LIST) == true) {
                    arguments.getParcelableArrayList(ARG_BARCODE_FIELD_LIST) ?: ArrayList()
                } else {
                    Log.e(TAG, "No barcode field list passed in!")
                    ArrayList()
                }

        val recyclerView = view.findViewById<RecyclerView>(R.id.barcode_field_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = BarcodeFieldAdapter(barcodeFieldList)

        return view
    }

    override fun onDismiss(dialogInterface: DialogInterface) {
        // Back to working state after the bottom sheet is dismissed.
        // Do this once ScanningViewModel has setState method
        //ViewModelProvider(activity).get(ScanningViewModel::class.java).setWorkflowState(ScanningViewModel.DETECTING)
        super.onDismiss(dialogInterface)
    }

    companion object {

        private const val TAG = "BarcodeResultFragment"
        private const val ARG_BARCODE_FIELD_LIST = "arg_barcode_field_list"

        fun show(fragmentManager: FragmentManager, barcodeFieldArrayList: List<BarcodeField>) {
            val barcodeResultFragment = BarcodeResultFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList(ARG_BARCODE_FIELD_LIST, ArrayList(barcodeFieldArrayList))
            barcodeResultFragment.arguments = bundle
            barcodeResultFragment.show(fragmentManager, TAG)
        }

        fun dismiss(fragmentManager: FragmentManager) {
            (fragmentManager.findFragmentByTag(TAG) as BarcodeResultFragment?)?.dismiss()
        }
    }
}