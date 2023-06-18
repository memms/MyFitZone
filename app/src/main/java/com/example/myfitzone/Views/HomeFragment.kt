package com.example.myfitzone.Views

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.R
import com.example.myfitzone.databinding.DashboardCardviewBinding
import com.example.myfitzone.databinding.FragmentHomeBinding
import com.example.myfitzone.databinding.FragmentRegistrationBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardCardAdapter: DashCardRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private var dataList = mutableListOf<DashboardRecyclerData>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: Remove Test Data
        dataList.add(DashboardRecyclerData("Weight", "", "70", "kg", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Height", "", "170", "cm", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("BMI", "", "24.2", "", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Calories", "", "2000", "kcal", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Water", "", "2", "L", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Sleep", "", "8", "hours", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Steps", "", "10000", "steps", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Heart Rate", "", "70", "bpm", "Updated 2 hours ago"))
        dataList.add(
            DashboardRecyclerData(
                "Blood Pressure",
                "",
                "120/80",
                "mmHg",
                "Updated 2 hours ago"
            )
        )
        dataList.add(
            DashboardRecyclerData(
                "Blood Sugar",
                "",
                "100",
                "mg/dL",
                "Updated 2 hours ago"
            )
        )
        dataList.add(DashboardRecyclerData("Body Fat", "", "20", "%", "Updated 2 hours ago"))

        recyclerView = binding.recyclerViewHome
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        dashboardCardAdapter = DashCardRecyclerAdapter()
        dashboardCardAdapter.setDashCardList(dataList)
        recyclerView.adapter = dashboardCardAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class DashCardRecyclerAdapter() :
        RecyclerView.Adapter<DashCardRecyclerAdapter.DashCardViewHolder>() {

        private var dashCardList = emptyList<DashboardRecyclerData>()

        internal fun setDashCardList(dashCardList: List<DashboardRecyclerData>) {
            this.dashCardList = dashCardList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DashCardViewHolder {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.dashboard_cardview, parent, false)
            val binding =
                DashboardCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DashCardViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: DashCardViewHolder,
            position: Int
        ) {
            holder.view.cardName.text = dashCardList[position].cardName
            holder.view.cardValue.text = dashCardList[position].cardValue
            holder.view.cardUnit.text = dashCardList[position].cardUnit
            holder.view.cardUpdated.text = dashCardList[position].cardUpdated
//            TODO: Uncomment this when the images are added to the project
//            holder.view.cardLogo.setImageResource(dashCardList[position].cardLogo.toInt())

            holder.itemView.setOnClickListener {
                Log.d("DashCardViewHolder: onBindViewHolder", "onClick: ")
                //TODO("Not yet implemented")
            }
        }

        override fun getItemCount(): Int {

            return dashCardList.size
//            TODO("Not yet implemented")
        }

        inner class DashCardViewHolder(val view: DashboardCardviewBinding) :
            RecyclerView.ViewHolder(view.root), View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d("DashCardViewHolder", "onClick: ")

            }

        }
    }
}