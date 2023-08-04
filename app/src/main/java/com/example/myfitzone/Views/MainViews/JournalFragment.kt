package com.example.myfitzone.Views.MainViews

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.CalenderEventData
import com.example.myfitzone.Models.UserExerciseModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.CalendarDayLayoutBinding
import com.example.myfitzone.databinding.CalendarEventItemViewBinding
import com.example.myfitzone.databinding.CalendarHeaderLayoutBinding
import com.example.myfitzone.databinding.FragmentJournalBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter



class JournalFragment : Fragment() {
    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private val journalAdapter = JournalAdapter()
    private var selectedDate: LocalDate? = null
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val today = LocalDate.now()

    private val events = mutableMapOf<LocalDate, List<CalenderEventData>>()
    private lateinit var userExerciseModel: UserExerciseModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.journalRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = journalAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
        userExerciseModel = ViewModelProvider(requireActivity())[UserExerciseModel::class.java]
        binding.journalCalendarView.monthScrollListener = {it ->
            binding.journalYear.text = it.yearMonth.year.toString()
            binding.journalMonth.text = it.yearMonth.month.name
            val yearMonth = "${it.yearMonth.year}-${it.yearMonth.month.value}"
            Log.d("JournalFragment", "onViewCreated: $yearMonth")
            userExerciseModel.getUserExercises(yearMonth, callback = object : FirestoreGetCompleteAny{
                override fun onGetComplete(result: Any) {
                    val list = result as MutableMap<LocalDate, CalenderEventData>
                    list.forEach{event ->
                        events.putIfAbsent(event.key, listOf(event.value))
                    }
                    Log.d("JournalFragment", "onGetComplete: $events")
                    binding.journalCalendarView.notifyCalendarChanged()
                    selectedDate?.let { it1 -> updateAdapterForDate(it1) }
                }

                override fun onGetFailure(string: String) {
                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                }

            })
            selectDate(it.yearMonth.atDay(1))
        }

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(50)
        configureBinders(daysOfWeek)
        binding.journalCalendarView.apply {
            setup(startMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        if (savedInstanceState == null) {
            // Show today's events initially.
            binding.journalCalendarView.post { selectDate(today) }
        }

    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                    }
                }
            }
        }
        binding.journalCalendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.calendarDayText
                val blueDotView = container.binding.calendarBlueDotView
                val greenDotView = container.binding.calendarGreenDotView

                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.visibility = View.VISIBLE
                    when (data.date) {
                        today -> {
                            textView.setTextColor(Color.WHITE)
                            textView.setBackgroundResource(R.drawable.blue_circle)
                            blueDotView.visibility = View.INVISIBLE
                            greenDotView.visibility = View.INVISIBLE
                        }
                        selectedDate -> {
                            textView.setTextColor(Color.BLUE)
                            textView.setBackgroundResource(R.drawable.light_blue_circle)
                            blueDotView.visibility = View.INVISIBLE
                            greenDotView.visibility = View.INVISIBLE
                        }
                        else -> {
                            textView.setTextColor(Color.BLACK)
                            textView.background = null
                            //Show Events depending on type
                            var bodyMeasure = false
                            var exercise = false
                            events[data.date]?.let {
                                it.forEach {calendarEventData ->
                                    if(calendarEventData.type == "bodyMeasure" && !bodyMeasure){
                                        bodyMeasure = true
                                    }
                                    if (calendarEventData.type == "exercise" && !exercise){
                                        exercise = true
                                    }
                                    if(bodyMeasure && exercise){
                                        return
                                    }
                                }
                            }
//                            Log.d("JournalFragment", "bind: $bodyMeasure $exercise")
                            blueDotView.isVisible = exercise
                            greenDotView.isVisible = bodyMeasure
                        }
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                    blueDotView.visibility = View.INVISIBLE
                    greenDotView.visibility = View.INVISIBLE
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderLayoutBinding.bind(view)
        }
        binding.journalCalendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if(container.legendLayout.calendarLegend1.text != daysOfWeek[0].name.first().toString()){
                        container.legendLayout.calendarLegend1.text = daysOfWeek[0].name.first().toString()
                        container.legendLayout.calendarLegend2.text = daysOfWeek[1].name.first().toString()
                        container.legendLayout.calendarLegend3.text = daysOfWeek[2].name.first().toString()
                        container.legendLayout.calendarLegend4.text = daysOfWeek[3].name.first().toString()
                        container.legendLayout.calendarLegend5.text = daysOfWeek[4].name.first().toString()
                        container.legendLayout.calendarLegend6.text = daysOfWeek[5].name.first().toString()
                        container.legendLayout.calendarLegend7.text = daysOfWeek[6].name.first().toString()
                    }
                }
            }
    }

    private fun selectDate(date: LocalDate) {
        if(selectedDate == date) return
        val oldDate = selectedDate
        selectedDate = date
        oldDate?.let { binding.journalCalendarView.notifyDateChanged(it) }
        binding.journalCalendarView.notifyDateChanged(date)
        updateAdapterForDate(date)
    }

    private fun updateAdapterForDate(date: LocalDate) {
        journalAdapter.apply {
            events.clear()
            events.addAll(this@JournalFragment.events[date] ?: emptyList())
            notifyDataSetChanged()
        }
        binding.journalCalendarSelectedDate.text = selectionFormatter.format(date)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class JournalAdapter() : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

        val events = mutableListOf<CalenderEventData>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
            return JournalViewHolder(
                CalendarEventItemViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
            )
        }

        override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
            holder.bind(events[position])
        }

        override fun getItemCount(): Int {
            return events.size
        }

        inner class JournalViewHolder(private val binding: CalendarEventItemViewBinding) : RecyclerView.ViewHolder(binding.root){

            init{
                itemView.setOnClickListener{
                    //TODO: Open Event Details
                }
            }
            fun bind(calenderEventData: CalenderEventData) {
                binding.calendarEventDetails.text = calenderEventData.description
                val simpleDateFormat = SimpleDateFormat("hh:mm a")
                binding.calendarEventTime.text = simpleDateFormat.format(calenderEventData.timeMillis)
                binding.calendarEventTitle.text = calenderEventData.title
            }

        }
    }


}
