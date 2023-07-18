package com.example.myfitzone.Views.ExerciseViews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Models.DatabaseExercisesModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.ExerciseGroupLinearBinding
import com.example.myfitzone.databinding.FragmentExerciseSelectorBinding

class ExerciseSelectorFragment : Fragment() {

    private val TAG = "ExerciseSelectorFragment"
    private var _binding: FragmentExerciseSelectorBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseExercisesModel: DatabaseExercisesModel

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
        databaseExercisesModel = ViewModelProvider(requireActivity())[DatabaseExercisesModel::class.java]
        binding.exerciseSelectorCloseButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        databaseExercisesModel.getExercises()
        binding.exerciseSelectorTitle.text = "${databaseExercisesModel.getSelectedGroup()} Exercises"
        binding.exerciseSelectorAddButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_exerciseSelectorFragment_to_newExerciseFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ExerciseSelectorAdapter(private val exersiceGroups : ArrayList<String>) : RecyclerView.Adapter<ExerciseSelectorAdapter.ExerciseSelectorViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseSelectorViewHolder {
            val binding = ExerciseGroupLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ExerciseSelectorViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ExerciseSelectorViewHolder, position: Int) {
            holder.bind(exersiceGroups[position])

        }

        override fun getItemCount(): Int {
            return exersiceGroups.size
        }

        inner class ExerciseSelectorViewHolder(private val binding: ExerciseGroupLinearBinding) :
            RecyclerView.ViewHolder(binding.root), View.OnClickListener {
            fun bind(item: String) {
                binding.titleExerciseGroup.text = exersiceGroups[adapterPosition]
                binding.titleExerciseGroup.setOnClickListener(this)
                binding.selectFieldButtonExerciseGroup.setOnClickListener(this)
            }

            override fun onClick(p0: View?) {
                Log.d("TAG", "onClick: ")
                view?.let {
                    it.findNavController().navigate(R.id.action_exerciseGroupFragment_to_exerciseSelectorFragment)
                }
            }
        }
    }



}