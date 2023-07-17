package com.example.myfitzone.Views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.Models.UserDetailModel
import com.example.myfitzone.Models.UserExercisesModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.DashboardCardviewBinding
import com.example.myfitzone.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardCardAdapter: DashCardRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private var dataList = mutableListOf<DashboardRecyclerData>()

    private val TAG = "HomeFragment"
    private lateinit var loggedInUser : UserDetailModel
    private lateinit var exerciseModel: UserExercisesModel


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

        loggedInUser = ViewModelProvider(requireActivity())[UserDetailModel::class.java]
        exerciseModel = ViewModelProvider(requireActivity())[UserExercisesModel::class.java]

        Log.d(TAG, "onViewCreated: ${loggedInUser.getUser().toString()}")

//        populateCards()
        //TODO: Remove Test Data
        //default
        dataList.add(DashboardRecyclerData("Weight", "", "00", "kg", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Height", "", "00", "cm", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Steps", "", "00", "steps", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Calories", "", "00", "kcal", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Distance Travelled", "", "0.0", "m", "Updated 2 hours ago"))

        //user enabled
        dataList.add(DashboardRecyclerData("BMI", "", "24.2", "", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Water", "", "2", "L", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Sleep", "", "8", "hours", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Heart Rate", "", "70", "bpm", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Body Fat", "", "20", "%", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Blood Pressure", "", "120/80", "mmHg", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Blood Sugar", "", "100", "mg/dL", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Body Temperature", "", "98.6", "°F", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Oxygen Saturation", "", "98", "%", "Updated 2 hours ago"))
        dataList.add(DashboardRecyclerData("Respiratory Rate", "", "16", "breaths/min", "Updated 2 hours ago"))

        recyclerView = binding.recyclerViewHome
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        dashboardCardAdapter = DashCardRecyclerAdapter()
        dashboardCardAdapter.setDashCardList(dataList)
        recyclerView.adapter = dashboardCardAdapter

        binding.imageProfile.setOnClickListener {
            Log.d(TAG, "onViewCreated: Profile Image Clicked")
            onProfileImageClicked()
        }

        binding.fabHome.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB Clicked")
            onFabClicked()
        }

    }

    private fun onFabClicked() {
        Log.d(TAG, "onFabClicked: ")
        view?.let {
            it.findNavController().navigate(R.id.action_homeFragment_to_exerciseGroupFragment)
        }
    }

    private fun populateCards() {
//        TODO("Not yet implemented")
        val exercises = exerciseModel.getUserExercises()
        for (exercise in exercises) {
            dataList.add(DashboardRecyclerData(exercise.name, exercise.image, exercise.map["value"].toString(),
                exercise.map["unit"].toString(), "Updated: "))
        }

    }

    private fun onProfileImageClicked() {
//        TODO("Not yet implemented")
        Log.d(TAG, "onProfileImageClicked: ")
        view?.let {
            it.findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
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