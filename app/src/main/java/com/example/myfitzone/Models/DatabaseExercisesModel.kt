package com.example.myfitzone.Models

import androidx.lifecycle.ViewModel

class DatabaseExercisesModel: ViewModel() {

    private val TAG = "DatabaseExercisesModel"


    fun getExerciseGroups(): ArrayList<String> {
        return arrayListOf("Chest", "Back", "Shoulders", "Biceps", "Triceps", "Legs", "Abs")
    }

    fun getExercises(): ArrayList<String> {
        return arrayListOf("Bench Press", "Incline Bench Press", "Decline Bench Press", "Dumbbell Bench Press",
            "Dumbbell Incline Bench Press", "Dumbbell Decline Bench Press", "Dumbbell Fly", "Incline Dumbbell Fly", "Decline Dumbbell Fly")
    }
}