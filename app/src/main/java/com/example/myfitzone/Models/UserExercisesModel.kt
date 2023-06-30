package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.DataModels.Exercise

class UserExercisesModel: ViewModel() {
    private var exercises = ArrayList<Exercise>()
    private val TAG = "UserExercisesModel"

    fun getExercises(): ArrayList<Exercise> {
        Log.d(TAG, "getExercises: $exercises")
        return exercises
    }

    fun setExercises(exercises: ArrayList<Exercise>) {
        Log.d(TAG, "setExercises: $exercises")
        this.exercises = exercises
    }

    fun addExercise(exercise: Exercise) {
        Log.d(TAG, "addExercise: $exercise")
        exercises.add(exercise)
    }

    fun removeExercise(exercise: Exercise) {
        Log.d(TAG, "removeExercise: $exercise")
        exercises.remove(exercise)
    }

    fun clearExercises() {
        Log.d(TAG, "clearExercises: ")
        exercises.clear()
    }

    fun getExercise(index: Int): Exercise {
        Log.d(TAG, "getExercise: Index: ${index}, Exercise: ${exercises[index]}")
        return exercises[index]
    }
}