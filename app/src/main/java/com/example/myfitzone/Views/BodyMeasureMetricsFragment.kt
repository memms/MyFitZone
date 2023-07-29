package com.example.myfitzone.Views

import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.example.myfitzone.Models.UserBodyMeasureModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.BodyMeasureListLinearLayoutBinding
import com.example.myfitzone.databinding.FragmentBodyMeasureMetricsBinding
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.EntryXComparator
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Collections


class bodyMeasureMetricsFragment : Fragment() {

    private val TAG = "bodyMeasureMetricsFragment"
    private var _binding: FragmentBodyMeasureMetricsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userBodyMeasureModel: UserBodyMeasureModel
    private var selectedType = ""
    private var selectedName = ""
    private lateinit var adapter: BodyMetricAdapter
    private var list: MutableList<UserBodyMetrics> = mutableListOf()


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
        adapter = BodyMetricAdapter()
        binding.entriesRecyclerViewBodyMeasureMetrics.adapter = adapter
        binding.entriesRecyclerViewBodyMeasureMetrics.layoutManager = LinearLayoutManager(requireContext())
        getBodyMeasurementHistory()

    }

    private fun setGraph(){
        val chart = binding.lineChartBodyMeasureMetrics
        chart.description.isEnabled = false
        val entries = arrayListOf<Entry>()
        Log.d(TAG, "setGraph: list $list")
        list.forEach {
            entries.add(Entry(it.timestamp.toFloat(), it.metricValue.toFloat()))
        }
        Collections.sort(entries, EntryXComparator())
        Log.d(TAG, "setGraph: entries $entries")
        val dataSet = LineDataSet(entries, selectedName)
        dataSet.setDrawCircles(true)
        dataSet.setDrawCircleHole(false)
        dataSet.circleRadius = 5f
        dataSet.lineWidth = 3f
        dataSet.color = Color.Blue.toArgb()
        dataSet.setDrawFilled(true)
        dataSet.setDrawValues(false)
        dataSet.valueFormatter = object : ValueFormatter(){
            fun getFormattedValueForMarker(value: Float): String {
                return "${value} ${FieldUnits.unitOfBody(selectedName)}"
            }
        }

        val xAxis = chart.xAxis
        xAxis.valueFormatter = object : IndexAxisValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = value.toLong()
                val sdf = SimpleDateFormat("MMM dd")
                return sdf.format(calendar.time)
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.spaceMin = 0.5f

        val yAxisRight = chart.axisRight
        yAxisRight.isEnabled = false

        val yAxis = chart.axisLeft
        yAxis.valueFormatter = object : ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                return "${value} ${FieldUnits.unitOfBody(selectedName)}"
            }
        }

        chart.setDrawGridBackground(false)
        chart.setDrawBorders(true)
        chart.setBorderColor(Color.Black.toArgb())
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawMarkers(true)
        chart.marker = CustomMarkerView(requireContext(), R.layout.chart_marker)
        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()

    }

    inner class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
        val TAG = "CustomMarkerView"
        val topVal = findViewById<TextView>(R.id.marker_value_top)
        val bottomVal = findViewById<TextView>(R.id.marker_value_bottom)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            val value = e!!.y
            topVal.text = "${value} ${FieldUnits.unitOfBody(selectedName)}"
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = e.x.toLong()
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            bottomVal.text = sdf.format(calendar.time)
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF((-(width/2)).toFloat(), (-height).toFloat())
        }

    }

    private fun getBodyMeasurementHistory() {
        list.clear()
        userBodyMeasureModel.getSelectedBodyMeasureMetrics(object : FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val listL = result as MutableMap<Long, UserBodyMetrics>
                list.clear()
                list.addAll(listL.values)
                list.sortByDescending { it.timestamp }
                adapter.notifyDataSetChanged()
                setGraph()
                Log.d(TAG, "onGetComplete: $list")
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), "Error:$string", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun modifyEntry(index: Int){
        val mView = layoutInflater.inflate(R.layout.dialog_body_measure, null)
        val valueEditText = mView.findViewById<EditText>(R.id.value_body_measure_dialog)
        val unitTextView = mView.findViewById<TextView>(R.id.unit_body_measure_dialog)
        val date = mView.findViewById<TextView>(R.id.date_body_measure_dialog)
        val formatter = SimpleDateFormat("MM/dd/yyyy")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = list[index].timestamp
        date.text = formatter.format(calendar.time)
        date.setOnClickListener {
            Log.d(TAG, "addEntryOnClick: date clicked")
            val datePicker = DatePickerDialog(requireContext())
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                date.text = formatter.format(calendar.time)
                Log.d(TAG, "addEntryOnClick: ${calendar.time}")
            }
            datePicker.show()
        }
        unitTextView.text = FieldUnits.unitOfBody(selectedName)
        valueEditText.setText(list[index].metricValue.toString())
        val builder = AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog)
            .setTitle(selectedName)
            .setPositiveButton("Save", null)
            .setNeutralButton("Cancel", null)
            .setNegativeButton("Delete", null)
            .setView(mView)
            .create()

        builder.setOnShowListener {
            builder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary))
            builder.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (valueEditText.text.toString().isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a value", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else if (valueEditText.text.toString()
                            .toDouble() >= 0.0 && valueEditText.text.toString().all { it.isDigit() || it == '.'  }
                    ) {
                        Log.d(TAG, "modifyEntry: ${valueEditText.text}")
                        val userBodyMetric = UserBodyMetrics(
                            calendar.toInstant().toEpochMilli(),
                            selectedType,
                            selectedName,
                            valueEditText.text.toString().toDouble(),
                            Instant.now().toEpochMilli()
                        )
                        userBodyMeasureModel.updateBodyMeasurement(
                            userBodyMetric,
                            list[index].timestamp,
                            object : FirestoreGetCompleteCallbackArrayList {
                                override fun onGetComplete(result: ArrayList<String>) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Successfully modified",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    builder.dismiss()
                                    getBodyMeasurementHistory()
                                }
                                override fun onGetFailure(string: String) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error:$string",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            })
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please enter a positive number",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            builder.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(resources.getColor(R.color.colorPrimary))
            builder.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener {
                    builder.dismiss()
                }
//            builder.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor()
            builder.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener {
                    userBodyMeasureModel.deleteBodyMeasurement(list[index], object : FirestoreGetCompleteCallbackArrayList{
                        override fun onGetComplete(result: ArrayList<String>) {
                            Toast.makeText(requireContext(), "Successfully deleted", Toast.LENGTH_SHORT)
                                .show()
                            builder.dismiss()
                            getBodyMeasurementHistory()
                        }

                        override fun onGetFailure(string: String) {
                            Toast.makeText(requireContext(), "Error:$string", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                }
        }
        builder.show()
        val phoneWidth = resources.displayMetrics.widthPixels/1.3
        builder.window!!.attributes = builder.window!!.attributes.apply {
            width = phoneWidth.toInt()
            height = LayoutParams.WRAP_CONTENT
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
                    } else if(valueEditText.text.toString().toDouble() >= 0.0 && valueEditText.text.toString().all { it.isDigit() || it == '.' }) {
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
                                getBodyMeasurementHistory()
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

    inner class BodyMetricAdapter: RecyclerView.Adapter<BodyMetricAdapter.BodyMetricViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyMetricViewHolder {
            val binding = BodyMeasureListLinearLayoutBinding.inflate(layoutInflater, parent, false)
            return BodyMetricViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BodyMetricViewHolder, position: Int) {
            val bodyMetric = list[position]
            val formatter = SimpleDateFormat("MMM dd")
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = bodyMetric.timestamp
            holder.dateL.text = formatter.format(calendar.time)
            holder.timeL.text = SimpleDateFormat("hh:mm a").format(calendar.time)
            holder.valueL.text = bodyMetric.metricValue.toString()
            holder.unitL.text = FieldUnits.unitOfBody(bodyMetric.metricName)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        inner class BodyMetricViewHolder(binding: BodyMeasureListLinearLayoutBinding): RecyclerView.ViewHolder(binding.root) {
            val dateL = binding.dateBodyMeasureList
            val timeL = binding.timeBodyMeasureList
            val valueL = binding.valueBodyMeasureList
            val unitL = binding.unitBodyMeasureList
            init {
                binding.root.setOnClickListener {
                    Log.d(TAG, "BodyMetricViewHolder: clicked")
                    modifyEntry(adapterPosition)
                }
            }
        }

    }



}