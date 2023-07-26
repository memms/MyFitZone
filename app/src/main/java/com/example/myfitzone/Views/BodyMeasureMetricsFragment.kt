package com.example.myfitzone.Views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        TODO("Not yet implemented")
    }


}