package com.example.myfitzone.Views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.databinding.FragmentNewExerciseBinding


class NewExerciseFragment : Fragment() {

   private var _binding: FragmentNewExerciseBinding? = null
    private val binding get() = _binding!!

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

    }

    private fun saveExercise(){

    }

    //RecyclerView Adapter that has a spinner inside it with a string array as the options. Store the selected option back into an arrayList in main class.
    inner class NewExerciseAdapter : RecyclerView.Adapter<NewExerciseAdapter.ViewHolder>(){

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            TODO()
        }

        override fun getItemCount(): Int {
            TODO()
        }

    }





}