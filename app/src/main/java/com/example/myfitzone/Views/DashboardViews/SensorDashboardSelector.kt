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
import androidx.compose.ui.text.capitalize
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.DataModels.DashboardTemplateData
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.UserExercise
import com.example.myfitzone.Models.DashboardModel
import com.example.myfitzone.R
import com.example.myfitzone.Utils.capitalizeFirst
import com.example.myfitzone.databinding.ExercisesListItemLayoutBinding
import com.example.myfitzone.databinding.FragmentExerciseSelectorBinding
import java.text.SimpleDateFormat

class SensorDashboardSelector:Fragment() {
    private val TAG = "SensorDashboardSelector"
    private var _binding: FragmentExerciseSelectorBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardModel: DashboardModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentExerciseSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.exerciseSelectorCloseButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        dashboardModel = ViewModelProvider(requireActivity())[DashboardModel::class.java]
        binding.exerciseSelectorTitle.text = "Sensor Dashboards"
        binding.exerciseSelectorAddButton.visibility = View.GONE

        //TODO: ability to change time periods
        binding.exerciseSelectorDescription.text = "Select from one of the following sensors"
        dashboardModel.getSensorTemplates(object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val sensorList = result as HashMap<String, DashboardTemplateData>
                Log.d(TAG, "onGetComplete: $sensorList")
                binding.exerciseSelectorRecyclerView.apply {
                    adapter = ExerciseSelectorAdapter(sensorList)
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }

        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ExerciseSelectorAdapter(private val sensorList: HashMap<String, DashboardTemplateData>) : RecyclerView.Adapter<ExerciseSelectorAdapter.ExerciseSelectorViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseSelectorViewHolder {
            val binding = ExercisesListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ExerciseSelectorViewHolder(binding)
        }



        override fun onBindViewHolder(holder: ExerciseSelectorViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int {
            return sensorList.size
        }



        inner class ExerciseSelectorViewHolder(private val binding: ExercisesListItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root), View.OnClickListener {
            fun bind() {
                binding.exerciseName.text = sensorList.keys.toList()[bindingAdapterPosition].capitalizeFirst()
                binding.exerciseDescription.visibility = View.INVISIBLE
                binding.exerciseCreatorName.visibility = View.GONE
                binding.exerciseFields.visibility = View.GONE
                binding.root.setOnClickListener(this)
            }

            override fun onClick(p0: View?) {
                Log.d("TAG", "onClick: ")
                dashboardModel.setValueAddName(sensorList.keys.toList()[bindingAdapterPosition])
                dashboardModel.setDashboardAddType("sensor")
                val map = hashMapOf(
                    sensorList.keys.toList()[bindingAdapterPosition] to sensorList[sensorList.keys.toList()[bindingAdapterPosition]]!!
                )
                Log.d(TAG, "onClick: $map")
                inflateDashboardDialog(map)
            }
        }
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
            builder.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(
                R.color.colorPrimary))
            builder.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (finalValueType != "") {
                        Log.d(TAG, "onClick: $finalValueType")
                        //TODO: Add dashboard to list
                        val itemAdd = DashboardRecyclerData(
                            "${dashboardModel.getValueAddName()} ($finalValueType)",
                            "", //TODO: Make a logo selector.
                            "",
                            map[dashboardModel.getValueAddName()]!!.unit.trim('\n'),
                            0,
                            0
                        )
                        dashboardModel.startAddSensorDashBoard(itemAdd, callback = object:
                            FirestoreGetCompleteCallbackArrayList {
                            override fun onGetComplete(result: ArrayList<String>) {
                                builder.dismiss()
                            }

                            override fun onGetFailure(string: String) {
                                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                            }
                        })
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