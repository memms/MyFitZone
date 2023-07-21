package com.example.myfitzone.Views.ExerciseViews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.DataModels.DatabaseExercise
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.Models.UserNewExercisesModel
import com.example.myfitzone.databinding.AddUserExerciseItemLayoutBinding
import com.example.myfitzone.databinding.FragmentAddUserExerciseBinding


class AddUserExerciseFragment : Fragment() {

    private val TAG = "AddUserExerciseFragment"
    private var _binding: FragmentAddUserExerciseBinding? = null
    private val binding get() = _binding!!
    private lateinit var userExerciseModel: UserNewExercisesModel
    private var exerciseTemplate: DatabaseExercise? = null
    private var attributesList: HashMap<String, ArrayList<*>>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentAddUserExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userExerciseModel = ViewModelProvider(requireActivity())[UserNewExercisesModel::class.java]
        exerciseTemplate = userExerciseModel.getSelectedExercise()

        binding.titleAddUserExercise.text = exerciseTemplate?.exerciseName
        binding.descriptionAddUserExercise.text = exerciseTemplate?.exerciseDescription
        binding.exerciseFieldsAddUserExercise.text = "Exercise Fields: ${exerciseTemplate?.exerciseFieldsList.toString().trim('[', ']')}"
        addDefaultAttributes()
        binding.attributesRecyclerviewAddUserExercise.adapter = AddUserExerciseAdapter(attributesList!!)

        binding.exitAddUserExercise.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.addAttributeAddUserExercise.setOnClickListener {
              addAttributesPressed()
        }

    }

    private fun addDefaultAttributes() {
        for(attribute in exerciseTemplate?.exerciseFieldsList!!) {
            when (FieldUnits.valueOf(attribute)){
                0 -> attributesList?.put(attribute, ArrayList<Int>())
                1 -> attributesList?.put(attribute, ArrayList<Double>())
                2 -> attributesList?.put(attribute, ArrayList<String>())
            }
        }
    }

    private fun addAttributesPressed() {
        TODO("Not yet implemented")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        userExerciseModel.clearSelectedExercise()
    }

    inner class AddUserExerciseAdapter(attributesList: HashMap<String, ArrayList<*>>) : RecyclerView.Adapter<AddUserExerciseAdapter.AddUserExerciseViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddUserExerciseViewHolder {
            val binding = AddUserExerciseItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AddUserExerciseViewHolder(binding)
        }

        override fun onBindViewHolder(holder: AddUserExerciseViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int {
            return attributesList?.size ?: 0
        }

        inner class AddUserExerciseViewHolder(private val binding: AddUserExerciseItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind() {

            }
        }
    }


}