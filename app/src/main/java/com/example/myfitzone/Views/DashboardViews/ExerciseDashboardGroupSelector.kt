package com.example.myfitzone.Views.DashboardViews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.Models.DashboardModel
import com.example.myfitzone.Models.DatabaseExercisesModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.ExerciseGroupLinearBinding
import com.example.myfitzone.databinding.FragmentExerciseGroupBinding

class ExerciseDashboardGroupSelector: Fragment() {

    private val TAG = "ExerciseDashboardGroupSelector"

    private var _binding : FragmentExerciseGroupBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseExercisesModel: DatabaseExercisesModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentExerciseGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseExercisesModel = ViewModelProvider(requireActivity())[DatabaseExercisesModel::class.java]
        binding.exerciseGroupCloseButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        databaseExercisesModel.getExerciseGroups(object: FirestoreGetCompleteCallbackArrayList {
            override fun onGetComplete(result: ArrayList<String>) {
                binding.exerciseGroupRecyclerView.adapter = ExerciseGroupAdapter(result)
                binding.exerciseGroupRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }

            override fun onGetFailure(string: String) {

            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ExerciseGroupAdapter(private val exersiceGroups : ArrayList<String>) : RecyclerView.Adapter<ExerciseGroupAdapter.ExerciseGroupViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseGroupViewHolder {
            val binding = ExerciseGroupLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ExerciseGroupViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ExerciseGroupViewHolder, position: Int) {
            holder.bind(exersiceGroups[position])
        }

        override fun getItemCount(): Int {
            return exersiceGroups.size
        }

        inner class ExerciseGroupViewHolder(private val binding: ExerciseGroupLinearBinding) :
            RecyclerView.ViewHolder(binding.root), View.OnClickListener {
            fun bind(item: String) {
                binding.titleExerciseGroup.text = exersiceGroups[adapterPosition]
                binding.titleExerciseGroup.setOnClickListener(this)
                binding.selectFieldButtonExerciseGroup.setOnClickListener(this)
            }

            override fun onClick(p0: View?) {
                Log.d("TAG", "onClick: ")
                val dashboardModel = ViewModelProvider(requireActivity())[DashboardModel::class.java]
                dashboardModel.setTempExerciseGroup(exersiceGroups[bindingAdapterPosition])
                view?.let {
                    findNavController().navigate(R.id.action_exerciseDashboardGroupSelector_to_exerciseDashboardSelector)
                }
            }
        }
    }
}