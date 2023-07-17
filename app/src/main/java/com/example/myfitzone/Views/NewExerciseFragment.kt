package com.example.myfitzone.Views

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.DataModels.Exercise
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentNewExerciseBinding
import com.example.myfitzone.databinding.NewExerciseFieldsLinearBinding


class NewExerciseFragment : Fragment() {
    private val TAG = "NewExerciseFragment"
   private var _binding: FragmentNewExerciseBinding? = null
    private val binding get() = _binding!!
    private val fields: MutableList<String> = mutableListOf()
    private lateinit var adapter: NewExerciseAdapter

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
        binding.attributesRecyclerviewNewExercise.adapter = adapter
        binding.attributesRecyclerviewNewExercise.layoutManager = LinearLayoutManager(requireContext())

        binding.addAttributeNewExercise.setOnClickListener {
            addAttribute()
        }
        binding.saveNewExercise.setOnClickListener {
            saveExercise()
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

        //fallback check if needed -TODO FIX if used
//        val selectedItems: MutableList<String> = mutableListOf()
//        for (i in 0 until fields.size) {
//            val spinnerItem = binding.attributesRecyclerviewNewExercise.findViewHolderForAdapterPosition(i) as? NewExerciseAdapter.NewExerciseViewHolder
//            val selectedItem = spinnerItem?.binding?.fieldTypeSpinnerNewExercise?.selectedItem?.toString()
//            selectedItem?.let {
//                selectedItems.add(selectedItem)
//            }
//        }
//        Log.d("NewExerciseFragment", "saveExercise:selectedItems: ${selectedItems.toString()}")
//        Log.d(TAG, "saveExercise:size comp: ${fields.size.toString()}, ${selectedItems.size.toString()}")
//        if(fields.size != selectedItems.size){
//            Toast.makeText(requireContext(), "Please select an attribute for each field", Toast.LENGTH_SHORT).show()
//            return
//        }

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
        //TODO: add exercise templates to database

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
//                binding.fieldTypeSpinnerNewExercise.adapter = ArrayAdapter(
//                    requireContext(),
//                    android.R.layout.simple_spinner_dropdown_item,
//                    fieldsL
//                )

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