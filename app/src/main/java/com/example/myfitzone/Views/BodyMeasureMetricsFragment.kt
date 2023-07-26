package com.example.myfitzone.Views

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myfitzone.Models.UserBodyMeasureModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentBodyMeasureMetricsBinding


class bodyMeasureMetricsFragment : Fragment() {

    private val TAG = "bodyMeasureMetricsFragment"
    private var _binding: FragmentBodyMeasureMetricsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userBodyMeasureModel: UserBodyMeasureModel
    private var selectedType = ""
    private var selectedName = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding  = FragmentBodyMeasureMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userBodyMeasureModel = ViewModelProvider(requireActivity())[UserBodyMeasureModel::class.java]
        binding.closeButtonBodyMeasureMetrics.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        selectedName = userBodyMeasureModel.getSelectedName()
        selectedType = userBodyMeasureModel.getSelectedType()
        Log.d(TAG, "onViewCreated: $selectedType")
        Log.d(TAG, "onViewCreated: $selectedName")
        binding.titleBodyMeasureMetrics.text = selectedName
        binding.addEntryButtonBodyMeasureMetrics.setOnClickListener {
            addEntryOnClick()
        }
    }

    private fun addEntryOnClick() {

        val mView = layoutInflater.inflate(R.layout.dialog_body_measure, null)
        val valueEditText = mView.findViewById<EditText>(R.id.value_body_measure_dialog)
        val unitTextView = mView.findViewById<TextView>(R.id.unit_body_measure_dialog)
        val date = mView.findViewById<TextView>(R.id.date_body_measure_dialog)
        val builder = AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog)
            .setTitle(selectedName)
            .setPositiveButton("Add", null)
            .setNeutralButton("Cancel", null)
            .setView(mView)
            .create()

        builder.setOnShowListener {
            builder.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (valueEditText.text.toString().isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a value", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.d(TAG, "addEntryOnClick: ${valueEditText.text.toString()}")
                        builder.dismiss()
                    }
                }
            builder.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener {
                    builder.dismiss()
                }
        }
        builder.window?.setLayout(
            10, 10
        )
        builder.show()

    }

}