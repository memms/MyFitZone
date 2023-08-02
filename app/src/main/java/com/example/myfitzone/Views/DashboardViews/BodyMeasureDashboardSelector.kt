package com.example.myfitzone.Views.DashboardViews

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackHashMap
import com.example.myfitzone.DataModels.DashboardTemplateData
import com.example.myfitzone.Models.DashboardModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentBodyMeasureSelectorBinding

class BodyMeasureDashboardSelector: Fragment(), View.OnClickListener {
    private val TAG = "BodyMeasureDashboardSelector"
    private var _binding: FragmentBodyMeasureSelectorBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardModel:DashboardModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBodyMeasureSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideValues()
        setListeners()
        dashboardModel = ViewModelProvider(requireActivity())[DashboardModel::class.java]
    }

    private fun hideValues(){
        binding.weightValueBodyMeasure.visibility = View.GONE
        binding.bodyFatValueBodyMeasure.visibility = View.GONE
        binding.caloriesValueBodyMeasure.visibility = View.GONE
        binding.sleepValueBodyMeasure.visibility = View.GONE
        binding.restingHeartRateValueBodyMeasure.visibility = View.GONE
        binding.neckValueBodyMeasure.visibility = View.GONE
        binding.shoulderValueBodyMeasure.visibility = View.GONE
        binding.chestValueBodyMeasure.visibility = View.GONE
        binding.rightArmValueBodyMeasure.visibility = View.GONE
        binding.leftArmValueBodyMeasure.visibility = View.GONE
        binding.rightForearmValueBodyMeasure.visibility = View.GONE
        binding.leftForearmValueBodyMeasure.visibility = View.GONE
        binding.upperAbsValueBodyMeasure.visibility = View.GONE
        binding.waistValueBodyMeasure.visibility = View.GONE
        binding.hipsValueBodyMeasure.visibility = View.GONE
        binding.rightThighValueBodyMeasure.visibility = View.GONE
        binding.leftThighValueBodyMeasure.visibility = View.GONE
        binding.rightCalfValueBodyMeasure.visibility = View.GONE
        binding.leftCalfValueBodyMeasure.visibility = View.GONE
    }

    private fun setListeners(){
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        var valueType = ""
        when (v?.id) {
            //CORE
            binding.weightLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Weight")
                valueType = "Weight"
            }

            binding.bodyFatLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Body Fat")
                valueType = "Body Fat %"
            }

            binding.caloriesLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Calories")
                valueType = "Calories"
            }

            binding.sleepLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Sleep")
                valueType = "Sleep"
            }

            binding.restingHeartRateLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Resting Heart Rate")
                valueType = "Resting Heart Rate"
            }
            //BODY
            binding.neckLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Neck")
                valueType = "Neck"
            }

            binding.shoulderLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Shoulder")
                valueType = "Shoulder"
            }

            binding.rightArmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Arm")
                valueType = "Right Arm"
            }

            binding.leftArmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Arm")
                valueType = "Left Arm"
            }

            binding.rightForearmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Forearm")
                valueType = "Right Forearm"
            }

            binding.leftForearmLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Forearm")
                valueType = "Left Forearm"
            }

            binding.chestLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Chest")
                valueType = "Chest"
            }

            binding.upperAbsLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Upper Abs")
                valueType = "Upper Abs"
            }

            binding.waistLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Waist")
                valueType = "Waist"
            }

            binding.hipsLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Hips")
                valueType = "Hips"
            }

            binding.rightThighLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Thigh")
                valueType = "Right Thigh"
            }

            binding.leftThighLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Thigh")

                valueType = "Left Thigh"
            }

            binding.rightCalfLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Right Calf")

                valueType = "Right Calf"
            }

            binding.leftCalfLayoutBodyMeasure.id -> {
                Log.d(TAG, "onClick: Left Calf")
                valueType = "Left Calf"
            }
        }
        dashboardModel.setDashboardAddType("bodyMeasure")
        dashboardModel.setValueAddName(valueType)
        getDashboardTemplate()
    }

    fun getDashboardTemplate(){
        dashboardModel.getDashboardTemplate(callback = object: FirestoreGetCompleteCallbackHashMap{
            override fun onGetComplete(result: HashMap<String, *>) {
                val map = result as HashMap<String, DashboardTemplateData>
                Log.d(TAG, "onGetComplete: $map")
                inflateDashboardDialog(map)
            }
        })
    }

    private fun inflateDashboardDialog(map: HashMap<String, DashboardTemplateData>) {
        val mView = layoutInflater.inflate(R.layout.dialog_dashboard_add, null)
        val titleTextView: TextView = mView.findViewById(R.id.title_add_dashboard)
        val measureNameTextView: TextView = mView.findViewById(R.id.measure_name_add_dashboard)
        val measureTypeSpinner: Spinner = mView.findViewById(R.id.measure_type_add_dashboard)
        val valueTypes = arrayListOf<String>()
        var finalValueType = ""
        valueTypes.add("Select Value Type")
        Log.d(TAG, "inflateDashboardDialog: ${map[dashboardModel.getValueAddName()]!!.valueType}")
        valueTypes.addAll(map[dashboardModel.getValueAddName()]!!.valueType)
        val spinnerAdapter = object: ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            valueTypes
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner
                // First item will be used for hint
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView =
                    super.getDropDownView(position, convertView, parent) as TextView
                //set the color of first item in the drop down list to gray
                if (position == 0) {
                    view.setTextColor(Color.GRAY)
                } else {
                    //here it is possible to define color for other items by
                    //view.setTextColor(Color.RED)
                }
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        measureTypeSpinner.adapter = spinnerAdapter
        measureTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d(TAG, "onItemSelected: ${valueTypes[position]}")
                val value = parent!!.getItemAtPosition(position).toString()
                if(value == valueTypes[0]){
                    (view as TextView).setTextColor(Color.GRAY)
                }
                else{
                    finalValueType = value
                }
            }
        }
        titleTextView.setText("Body Measurement Dashboard")
        measureNameTextView.setText(dashboardModel.getValueAddName())

        val builder = AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog)
            .setTitle("Add Dashboard")
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .setView(mView)
            .create()

        builder.setOnShowListener {
            builder.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary))
            builder.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (finalValueType != "") {
                        Log.d(TAG, "onClick: $finalValueType")
                        //TODO: Add dashboard to list
//                        dashboardModel.setDashboardAddType("bodyMeasure")
//                        dashboardModel.setValueAddType(finalValueType)
//                        dashboardModel.setDashboardTemplate(map[dashboardModel.getValueAddName()]!!)
//                        dashboardModel.addDashboard()
                        builder.dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please select a value type",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            builder.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            builder.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener {
                    dashboardModel.clearTempValues()
                    builder.dismiss()
                }

        }
        builder.show()
    }

}