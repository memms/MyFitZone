package com.example.myfitzone.Views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.example.myfitzone.DataModels.UserExercise
import com.example.myfitzone.Models.UserBodyMeasureModel
import com.example.myfitzone.Models.UserExercisesModel
import com.example.myfitzone.R
import com.example.myfitzone.Utils.getAverage
import com.example.myfitzone.databinding.BodyMeasureListLinearLayoutBinding
import com.example.myfitzone.databinding.ChartExerciseItemViewBinding
import com.example.myfitzone.databinding.FragmentChartsBinding
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.EntryXComparator
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections


class ChartsFragment : Fragment() {

    private val TAG = "ChartsFragment"
    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChartsAdapterExercise
    private lateinit var userBodyMeasureModel: UserBodyMeasureModel
    private lateinit var userExercisesModel: UserExercisesModel


    data class Chart(val date: String, val time: String, val value: ArrayList<String>)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val type = arguments?.getString("type")
        val name = arguments?.getString("name")
        binding.titleCharts.text = name
        if(type == null || name == null){
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onViewCreated: Error type ($type) or name ($name) is null or empty")
            return
        }
        Log.d(TAG, "onViewCreated: $type $name")
        when (type){
            "exercise" ->{
                userExercisesModel = ViewModelProvider(requireActivity())[UserExercisesModel::class.java]
                getExerciseCharts(name)
            }
            "bodyMeasure" ->{
                userBodyMeasureModel = ViewModelProvider(requireActivity())[UserBodyMeasureModel::class.java]
                getBodyMeasureCharts(name)
            }
            "sensor" ->{
                getSensorCharts(name)
            }
            "analytics" ->{
                getAnalyticsCharts(name)
            }
        }
    }

    /**
     * Split the card name to get the name and the type
     * @return List<String> with the name [0] and the type [1]
     */
    private fun splitName(name: String): List<String> {
        val exercisename = name.split('(')[0].trim()
        val exerciseType = name.split('(')[1].split(')')[0].trim()
        return listOf(exercisename, exerciseType)
    }

    private fun getAnalyticsCharts(name: String) {
        //TODO: Get the analytics charts
    }

    private fun getSensorCharts(name: String) {

    }

    private fun getBodyMeasureCharts(name: String) {
        var timeRange = "30d"
        val sdf = java.text.SimpleDateFormat("yyyy-MM")
        var timeRangeMilis= arrayOf<Long>() //[0] = start, [1] = end
        //TODO:
        val listDocNames: List<String> = when(timeRange) {
            "30d" -> {
                val today = Calendar.getInstance()
                timeRangeMilis += today.timeInMillis.toLong()
                val list = mutableListOf<String>()
                list.add(sdf.format(today.time))
                today.add(Calendar.DATE, -30)
                timeRangeMilis += today.timeInMillis.toLong()
                if (!list.contains(sdf.format(today.time))) {
                    list.add(sdf.format(today.time))
                }
                list.toList()

            }
            else -> return
        }
        val (bodyMeasureName, type) = splitName(name)
        userBodyMeasureModel.setSelectedName(bodyMeasureName)
        userBodyMeasureModel.getSelectedBodyMeasureMetrics(callback = object : FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val result = result as Map<Long, UserBodyMetrics>
                val list = result.values.toMutableList()
                list.sortByDescending { it.timestamp }
                setupRecyclerViewBody(list)
                setupGraphBody(list)
                userBodyMeasureModel.setSelectedName("")
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun getExerciseCharts(name: String) {

        //TODO: Set up for multiple timeranges
        var timeRange = "30d"
        val sdf = java.text.SimpleDateFormat("yyyy-MM")
        var timeRangeMilis= arrayOf<Long>() //[0] = start, [1] = end
        val listDocNames: List<String> = when(timeRange){
            "30d" -> {
                val today = Calendar.getInstance()
                timeRangeMilis += today.timeInMillis.toLong()
                val list = mutableListOf<String>()
                list.add(sdf.format(today.time))
                today.add(Calendar.DATE, -30)
                timeRangeMilis += today.timeInMillis.toLong()
                if(!list.contains(sdf.format(today.time))){
                    list.add(sdf.format(today.time))
                }
                list.toList()

            }
//            "120d" -> {
//                //TODO: Get the last 120 days
//            }
//            "1y" -> {
//                //TODO
//            }
            else -> return
        }
        userExercisesModel.getUserExercises(listDocNames, callback = object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val (exercisename, exerciseType) = splitName(name)
                //TODO get the exercises with the same fieldmaps as templates.
                val list: MutableList<UserExercise> = (result as List<UserExercise>).filter { it.name == exercisename
                        && it.timeAdded < timeRangeMilis[0]
                        && it.timeAdded > timeRangeMilis[1] }.toMutableList()
                setupRecyclerViewExercise(list.sortedByDescending { it.timeAdded })
                setupGraphExercise(list.sortedBy { it.timeAdded })

            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setupRecyclerViewBody(list: List<UserBodyMetrics>){
        var chartList = arrayListOf<Chart>()
        val sdf = SimpleDateFormat("MMM dd, yyyy")
        val sdf2 = SimpleDateFormat("hh:mm a")
        list.forEach {
            val date = sdf.format(it.timestamp)
            val time = sdf2.format(it.timestamp)
            val tempArrayList = arrayListOf<String>(it.metricValue.toString(), FieldUnits.unitOfBody(it.metricName))
            chartList.add(Chart(date, time, tempArrayList))
        }
        adapter = ChartsAdapterExercise(chartList)
        binding.entriesRecyclerViewCharts.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.entriesRecyclerViewCharts.adapter = adapter
        binding.entriesRecyclerViewCharts.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupRecyclerViewExercise(list: List<UserExercise>){
        var chartList = arrayListOf<Chart>()
        val sdf = SimpleDateFormat("MMM dd, yyyy")
        val sdf2 = SimpleDateFormat("hh:mm a")
        list.forEach {
            val date = sdf.format(it.timeAdded)
            val time = sdf2.format(it.timeAdded)

            val tempArrayList = arrayListOf<String>()
            for (field in it.fieldmap){
                val stringBuilder = StringBuilder()
                if(field.key == "sets"){
                    stringBuilder.append("Sets: \n")
                    for(i in 0 until field.value as Long){
                        stringBuilder.append("Set ${i+1}: \n")
                    }
                    tempArrayList.add(0, stringBuilder.toString().trim('\n'))
                }
                else{
                    if(field.key == "Reps"){
                        stringBuilder.append("${field.key}:\n")
                    }
                    else {
                        stringBuilder.append("${field.key} (${FieldUnits.unitOf(field.key)}): \n")
                    }
                    for(i in 0 until (field.value as List<String>).size){
                        stringBuilder.append("${(field.value as List<String>)[i]} \n")
                    }
                    tempArrayList.add(stringBuilder.toString().trim('\n'))
                }
            }

            chartList.add(Chart(date, time, tempArrayList))
        }

        adapter = ChartsAdapterExercise(chartList)
        binding.entriesRecyclerViewCharts.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.entriesRecyclerViewCharts.adapter = adapter
        binding.entriesRecyclerViewCharts.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun setupGraphBody(list: List<UserBodyMetrics>){
        val chart = binding.combinedChartCharts
        chart.description.isEnabled = false
        val entries = arrayListOf<Entry>()
        Log.d(TAG, "setGraph: list $list")
        list.forEach {
            entries.add(Entry(it.timestamp.toFloat(), it.metricValue.toFloat()))
        }
        Collections.sort(entries, EntryXComparator())
        Log.d(TAG, "setGraph: entries $entries")
        val dataSet = LineDataSet(entries, list[0].metricName)
        dataSet.setDrawCircles(true)
        dataSet.setDrawCircleHole(false)
        dataSet.circleRadius = 5f
        dataSet.lineWidth = 3f
        dataSet.color = Color.Blue.toArgb()
        dataSet.setDrawFilled(true)
        dataSet.setDrawValues(false)
        dataSet.valueFormatter = object : ValueFormatter(){
            fun getFormattedValueForMarker(value: Float): String {
                return "${value} ${FieldUnits.unitOfBody(list[0].metricName)}"
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
                return "${value} ${FieldUnits.unitOfBody(list[0].metricName)}"
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
        val combinedData = CombinedData()
        combinedData.setData(lineData)
        chart.data = combinedData
        chart.invalidate()
    }

    private fun setupGraphExercise(list: List<UserExercise>) {
        val chart = binding.combinedChartCharts
        chart.description.isEnabled = false
        val i = list[0].fieldmap.keys.size //number of lines in the graph
        //the lines for each value will represent an average

        var scatterDataSet = arrayListOf<IScatterDataSet>()
        var lineDataSet = arrayListOf<ILineDataSet>()
        //get the index of "sets" in fieldmap
        val combinedData = CombinedData()
        for(j in 0 until i){    //itterate fieldmap size
            if(list[0].fieldmap.keys.toList()[j] == "sets"){    //skip sets
                continue
            }

            val entries = arrayListOf<Entry>()
            val averageEntries = arrayListOf<Entry>()
                list.forEach {
                    val sets = it.fieldmap["sets"] as Long
                    Log.d(TAG, "setupGraph: ${it.fieldmap}")
                    for (k in 0 until sets.toInt()){
                        val time = it.timeAdded

                        Log.d(TAG, "setupGraph: ${list[0].fieldmap.keys.toList()[j]}")
                        Log.d(TAG, "setupGraph: ${it.fieldmap[list[0].fieldmap.keys.toList()[j]]}")
                        Log.d(TAG, "setupGraph: ${(it.fieldmap[list[0].fieldmap.keys.toList()[j]] as ArrayList<String>)[k].toFloat()}")
                        val value = (it.fieldmap[list[0].fieldmap.keys.toList()[j]] as ArrayList<String>)[k].toFloat()
                        entries.add(Entry(time.toFloat(), value))
                    }
                    val tempEntry = Entry(it.timeAdded.toFloat(), (it.fieldmap[list[0].fieldmap.keys.toList()[j]] as List<String>).getAverage().toFloat())
                    tempEntry.data = FieldUnits.unitOf(list[0].fieldmap.keys.toList()[j]) + " avg"
                    averageEntries.add(tempEntry)
                }
            Collections.sort(entries, EntryXComparator())
            Collections.sort(averageEntries, EntryXComparator())
            val dataSet = ScatterDataSet(entries, list[0].fieldmap.keys.toList()[j])
            dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            dataSet.color = ColorTemplate.COLORFUL_COLORS[j]
            dataSet.setDrawValues(false)
            dataSet.setDrawHighlightIndicators(false)
//            dataSet.valueFormatter = object : ValueFormatter(){
//                fun getFormattedValueForMarker(value: Float): String {
//                    return "${value} ${FieldUnits.unitOf(list[0].fieldmap.keys.toList()[j])}"
//                }
//            }
            scatterDataSet.add(dataSet)
            val lineDataSet2 = LineDataSet(averageEntries, "${list[0].fieldmap.keys.toList()[j]} (avg/sess)")
            lineDataSet2.setDrawCircles(false)
            lineDataSet2.setDrawCircleHole(false)
            lineDataSet2.circleRadius = 0f
            lineDataSet2.lineWidth = 1f
            lineDataSet2.color = ColorTemplate.COLORFUL_COLORS[j]
            lineDataSet2.setDrawFilled(false)
            lineDataSet2.setDrawValues(false)
            lineDataSet2.setDrawHighlightIndicators(true)
            lineDataSet2.valueFormatter = object : ValueFormatter(){
                fun getFormattedValueForMarker(value: Float): String {
                    return "${value} ${FieldUnits.unitOf(list[0].fieldmap.keys.toList()[j])}"
                }
            }
            lineDataSet.add(lineDataSet2)

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
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.setLabelCount(7, true)
        xAxis.spaceMin = 0.5f
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawLabels(true)
        xAxis.setDrawLimitLinesBehindData(true)
        xAxis.setDrawGridLinesBehindData(true)

        val yAxisRight = chart.axisRight
        yAxisRight.isEnabled = false

        chart.setDrawGridBackground(false)
        chart.setDrawBorders(true)
        chart.setBorderColor(Color.Black.toArgb())
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawMarkers(true)

        chart.marker = CustomMarkerView(requireContext(), R.layout.chart_marker)
        combinedData.setData(ScatterData(scatterDataSet))
        combinedData.setData(LineData(lineDataSet))
        chart.data = combinedData
        chart.animateXY(1000, 1000)
        chart.legend.entries.forEach {
            if(it.label.contains("avg")){
                it.form = Legend.LegendForm.LINE
            }
            else {
                it.form = Legend.LegendForm.CIRCLE
            }
        }
        chart.invalidate();
    }

    inner class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
        val TAG = "CustomMarkerView"
        val topVal = findViewById<TextView>(R.id.marker_value_top)
        val bottomVal = findViewById<TextView>(R.id.marker_value_bottom)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            if(e!!.data == null){
                findViewById<LinearLayout>(R.id.chart_marker_layout).visibility = View.GONE
                return
            }
            findViewById<LinearLayout>(R.id.chart_marker_layout).visibility = View.VISIBLE
            val value = e!!.y
            topVal.text = "${value} ${e.data}"
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = e.x.toLong()
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val sdf2 = SimpleDateFormat("hh:mm a")
            bottomVal.text = sdf.format(calendar.time) + "\n" + sdf2.format(calendar.time)
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF((-(width/2)).toFloat(), (-height).toFloat())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    inner class ChartsAdapterExercise(private val list: ArrayList<Chart>) : RecyclerView.Adapter<ChartsAdapterExercise.ViewHolder>() {
        val TAG = "ChartsAdapter"

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ChartExerciseItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.dateBodyMeasureList.apply {
                text = "${list[position].date} \n ${list[position].time}"
                textSize = 14f
            }
            for(string in list[position].value){
                val textView = TextView(requireContext())
                textView.apply {
                    text = string.trim('\n')
                    textSize = 14f
                    if(string.contains("Sets")){
                        gravity = Gravity.START
                    }
                    else {
                        gravity = Gravity.END
                    }
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    setPadding(0, 0, 10, 0)
                }
                holder.binding.chartExerciseItemView.addView(textView)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        inner class ViewHolder(val binding: ChartExerciseItemViewBinding) : RecyclerView.ViewHolder(binding.root){

        }
    }

}