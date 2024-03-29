package com.example.myfitzone.Views.ExerciseViews

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.DatabaseExercise
import com.example.myfitzone.Models.DatabaseExercisesModel
import com.example.myfitzone.Models.UserDetailModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentNewExerciseBinding
import com.example.myfitzone.databinding.NewExerciseFieldsLinearBinding


class NewExerciseFragment : Fragment() {
    private val TAG = "NewExerciseFragment"
   private var _binding: FragmentNewExerciseBinding? = null
    private val binding get() = _binding!!
    private val fields: MutableList<String> = mutableListOf()
    private lateinit var adapter: NewExerciseAdapter
    private lateinit var userDetailModel: UserDetailModel
    private lateinit var databaseExercisesModel: DatabaseExercisesModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = NewExerciseAdapter(fields)
        userDetailModel = ViewModelProvider(requireActivity())[UserDetailModel::class.java]
        databaseExercisesModel = ViewModelProvider(requireActivity())[DatabaseExercisesModel::class.java]
        binding.attributesRecyclerviewNewExercise.adapter = adapter
        binding.attributesRecyclerviewNewExercise.layoutManager = LinearLayoutManager(requireContext())

        binding.addAttributeNewExercise.setOnClickListener {
            addAttribute()
        }
        binding.saveNewExercise.setOnClickListener {
            saveExercise()
        }
        binding.exitNewExercise.setOnClickListener {
            onDestroyView()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addAttribute(){
        fields.add("")
        adapter.notifyItemInserted(fields.size - 1)
    }

    private fun saveExercise(){
        Log.d(TAG, "saveExercise:fields: ${fields.toString()}")

        //check if all fields have been filled
        for (i in 0 until fields.size) {
            if(fields[i] == ""){
                Toast.makeText(requireContext(), "Please select an attribute for each field", Toast.LENGTH_SHORT).show()
                return
            }
        }
        val exerciseName = binding.nameNewExercise.text.toString()
        if(exerciseName == ""){
            Toast.makeText(requireContext(), "Please enter an exercise name", Toast.LENGTH_SHORT).show()
            return
        }
        val exerciseDescription = binding.descriptionNewExercise.text.toString()
        val exerciseGroup = databaseExercisesModel.getSelectedGroup()
        val finalFields = ArrayList<String>(fields)
        val exercise = DatabaseExercise(exerciseGroup, exerciseName, exerciseDescription, finalFields, userDetailModel.getUsername())
        databaseExercisesModel.addNewExercise(object : FirestoreGetCompleteCallbackArrayList{
            override fun onGetComplete(result: ArrayList<String>) {
                if(result.contains("exists")){
                    Toast.makeText(requireContext(), "Exercise with name exists!", Toast.LENGTH_SHORT).show()
                    return
                }
                Log.d(TAG, "saveExercise: onGetComplete")
                Toast.makeText(requireContext(), "Exercise saved", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            override fun onGetFailure(string: String) {
                Log.d(TAG, "saveExercise: onGetFailure $string")
                Toast.makeText(requireContext(), "Error saving exercise", Toast.LENGTH_SHORT).show()
            }

        }, exercise)
    }

    //RecyclerView Adapter that has a spinner inside it with a string array as the options. Store the selected option back into an arrayList in main class.
    inner class NewExerciseAdapter (private val fields: MutableList<String>) :
        RecyclerView.Adapter<NewExerciseAdapter.NewExerciseViewHolder>(){



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewExerciseViewHolder {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.new_exercise_fields_linear, parent, false)
            val binding =
                NewExerciseFieldsLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return NewExerciseViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: NewExerciseViewHolder,
            position: Int) {
            holder.bind(fields[position])

        }

        override fun getItemCount(): Int {
            return fields.size
        }

        inner class NewExerciseViewHolder(val binding: NewExerciseFieldsLinearBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(item: String) {
                val fieldsL = resources.getStringArray(R.array.ExerciseFields)
                val spinnerAdapter = object: ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    fieldsL
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
                binding.fieldTypeSpinnerNewExercise.adapter = spinnerAdapter
                binding.fieldTypeSpinnerNewExercise.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val value = parent!!.getItemAtPosition(position).toString()
                            if (value == fieldsL[0]) {
                                (view as TextView).setTextColor(Color.GRAY)
                            } else {
                                fields[adapterPosition] =
                                    parent?.getItemAtPosition(position).toString()
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }
                    }

                binding.removeFieldButtonNewExercise.setOnClickListener {
                    fields.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                }
            }
        }
    }





}