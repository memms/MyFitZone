package com.example.myfitzone.Views

import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.unit.Density
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.example.myfitzone.Models.UserBodyMeasureModel
import com.example.myfitzone.R
import com.example.myfitzone.Utils.LocalToUTC
import com.example.myfitzone.databinding.FragmentBodyMeasureMetricsBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar


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
        val formatter = SimpleDateFormat("MM/dd/yyyy")
        val calendar = Calendar.getInstance()
        date.text = formatter.format(calendar.time)
        date.setOnClickListener {
            Log.d(TAG, "addEntryOnClick: date clicked")
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                date.text = formatter.format(calendar.time)
                Log.d(TAG, "addEntryOnClick: ${calendar.time}")
            }
            datePicker.show()
        }
        unitTextView.text = FieldUnits.unitOfBody(selectedName)
        val builder = AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog)
            .setTitle(selectedName)
            .setPositiveButton("Add", null)
            .setNeutralButton("Cancel", null)
            .setView(mView)
            .create()

        builder.setOnShowListener {
            builder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary))
            builder.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (valueEditText.text.toString().isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a value", Toast.LENGTH_SHORT)
                            .show()
                    } else if(valueEditText.text.toString().toDouble() >= 0.0 && valueEditText.text.toString().all { it.isDigit() }){
                        Log.d(TAG, "addEntryOnClick: ${valueEditText.text}")
                        val userBodyMetric = UserBodyMetrics(
                            calendar.toInstant().toEpochMilli(),
                            selectedType,
                            selectedName,
                            valueEditText.text.toString().toDouble(),
                            Instant.now().toEpochMilli()
                        )
                        userBodyMeasureModel.newBodyMeasurement(userBodyMetric, object: FirestoreGetCompleteCallbackArrayList{
                            override fun onGetComplete(result: ArrayList<String>) {
                                Toast.makeText(requireContext(), "Successfully added", Toast.LENGTH_SHORT)
                                    .show()
                                builder.dismiss()
                            }

                            override fun onGetFailure(string: String) {
                                Toast.makeText(requireContext(), "Error:$string", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                    }
                    else{
                        Toast.makeText(requireContext(), "Please enter a positive number", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            builder.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(resources.getColor(R.color.colorPrimary))
            builder.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener {
                    builder.dismiss()
                }
        }
        builder.show()
        val phoneWidth = resources.displayMetrics.widthPixels/1.3
        builder.window!!.attributes = builder.window!!.attributes.apply {
            width = phoneWidth.toInt()
            height = LayoutParams.WRAP_CONTENT
        }
    }

}