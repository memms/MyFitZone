package com.example.myfitzone.Views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.R
import com.example.myfitzone.databinding.ExerciseGroupLinearBinding
import com.example.myfitzone.databinding.FragmentExerciseGroupBinding
import com.example.myfitzone.databinding.NewExerciseFieldsLinearBinding


class ExerciseGroupFragment : Fragment() {

    private var _binding : FragmentExerciseGroupBinding? = null
    private val binding get() = _binding!!

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
        binding.exerciseGroupCloseButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val arrTEmp = arrayListOf<String>("Chest", "Back", "Shoulders", "Biceps", "Triceps", "Legs", "Abs")
        binding.exerciseGroupRecyclerView.adapter = ExerciseGroupAdapter(arrTEmp)
        binding.exerciseGroupRecyclerView.layoutManager = LinearLayoutManager(requireContext())
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
                view?.let {
                    it.findNavController().navigate(R.id.action_exerciseGroupFragment_to_newExerciseFragment)
                }
            }
        }
    }

}