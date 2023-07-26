package com.example.myfitzone.Views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.myfitzone.Models.UserBodyMeasureModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentBodyMeasureSelectorBinding


class BodyMeasureSelectorFragment : Fragment(), View.OnClickListener {

    private val TAG = "BodyMeasureSelectorFragment"
    private var _binding: FragmentBodyMeasureSelectorBinding? = null
    private val binding get() = _binding!!
    private lateinit var userBodyMeasureModel: UserBodyMeasureModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBodyMeasureSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userBodyMeasureModel = ViewModelProvider(requireActivity()).get(UserBodyMeasureModel::class.java)
        binding.closeButtonBodyMeasure.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        setListeners()
    }

    fun setListeners(){
        //CORE
        binding.weightLayoutBodyMeasure.setOnClickListener(this)
        binding.bodyFatLayoutBodyMeasure.setOnClickListener(this)
        binding.caloriesLayoutBodyMeasure.setOnClickListener(this)
        binding.sleepLayoutBodyMeasure.setOnClickListener(this)
        binding.restingHeartRateLayoutBodyMeasure.setOnClickListener(this)
        //BODY
        binding.neckLayoutBodyMeasure.setOnClickListener(this)
        binding.shoulderLayoutBodyMeasure.setOnClickListener(this)
        binding.rightArmLayoutBodyMeasure.setOnClickListener(this)
        binding.leftArmLayoutBodyMeasure.setOnClickListener(this)
        binding.rightForearmLayoutBodyMeasure.setOnClickListener(this)
        binding.leftForearmLayoutBodyMeasure.setOnClickListener(this)
        binding.chestLayoutBodyMeasure.setOnClickListener(this)
        binding.upperAbsLayoutBodyMeasure.setOnClickListener(this)
        binding.waistLayoutBodyMeasure.setOnClickListener(this)
        binding.hipsLayoutBodyMeasure.setOnClickListener(this)
        binding.rightThighLayoutBodyMeasure.setOnClickListener(this)
        binding.leftThighLayoutBodyMeasure.setOnClickListener(this)
        binding.rightCalfLayoutBodyMeasure.setOnClickListener(this)
        binding.leftCalfLayoutBodyMeasure.setOnClickListener(this)
    }

    fun getAllLastMetrics() {

    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //CORE
            binding.weightLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Weight")
                userBodyMeasureModel.setSelectedType("core")
                userBodyMeasureModel.setSelectedName("Weight")
            }
            binding.bodyFatLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Body Fat")
                userBodyMeasureModel.setSelectedType("core")
                userBodyMeasureModel.setSelectedName("Body Fat %")
            }
            binding.caloriesLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Calories")
                userBodyMeasureModel.setSelectedType("core")
                userBodyMeasureModel.setSelectedName("Calories")
            }
            binding.sleepLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Sleep")
                userBodyMeasureModel.setSelectedType("core")
                userBodyMeasureModel.setSelectedName("Sleep")
            }
            binding.restingHeartRateLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Resting Heart Rate")
                userBodyMeasureModel.setSelectedType("core")
                userBodyMeasureModel.setSelectedName("Resting Heart Rate")
            }
            //BODY
            binding.neckLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Neck")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Neck")
            }
            binding.shoulderLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Shoulder")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Shoulder")
            }
            binding.rightArmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Arm")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Right Arm")
            }
            binding.leftArmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Arm")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Left Arm")
            }
            binding.rightForearmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Forearm")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Right Forearm")
            }
            binding.leftForearmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Forearm")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Left Forearm")
            }
            binding.chestLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Chest")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Chest")
            }
            binding.upperAbsLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Upper Abs")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Upper Abs")
            }
            binding.waistLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Waist")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Waist")
            }
            binding.hipsLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Hips")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Hips")
            }
            binding.rightThighLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Thigh")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Right Thigh")
            }
            binding.leftThighLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Thigh")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Left Thigh")
            }
            binding.rightCalfLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Calf")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Right Calf")
            }
            binding.leftCalfLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Calf")
                userBodyMeasureModel.setSelectedType("body")
                userBodyMeasureModel.setSelectedName("Left Calf")
            }
        }
        view?.let {
            it.findNavController().navigate(R.id.action_bodyMeasureSelectorFragment_to_bodyMeasureMetricsFragment)
        }
    }


}