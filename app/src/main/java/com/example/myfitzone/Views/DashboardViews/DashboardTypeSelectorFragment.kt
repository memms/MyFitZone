package com.example.myfitzone.Views.DashboardViews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentDashboardTypeSelectorBinding


class DashboardTypeSelectorFragment : Fragment() {

    private val TAG = "DashboardTypeSelectorFragment"
    private var _binding: FragmentDashboardTypeSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentDashboardTypeSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners(){
        binding.dashboardTypeSelectorLayoutBodyMeasurement.setOnClickListener {
            view?.let{
                it.findNavController().navigate(R.id.action_dashboardTypeSelectorFragment_to_bodyMeasureDashboardSelector)
            }
        }
        binding.dashboardTypeSelectorLayoutExerciseMeasurement.setOnClickListener {
            view?.let{
                it.findNavController().navigate(R.id.action_dashboardTypeSelectorFragment_to_exerciseDashboardGroupSelector)
            }
        }
        binding.dashboardTypeSelectorLayoutSensorMeasurement.setOnClickListener {
            view?.let{
                it.findNavController().navigate(R.id.action_dashboardTypeSelectorFragment_to_sensorDashboardSelector)
            }
        }
        binding.dashboardTypeSelectorLayoutAnalyticsMeasurement.setOnClickListener {

        }
        binding.closeButtonDashboardTypeSelector.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}