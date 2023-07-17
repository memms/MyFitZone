package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.DataModels.Exercise

class UserExercisesModel: ViewModel() {
    private var exercises = ArrayList<Exercise>()
    private val TAG = "UserExercisesModel"

    fun getUserExercises(): ArrayList<Exercise> {
        Log.d(TAG, "getExercises: $exercises")
        return exercises
    }

    fun setUserExercises(exercises: ArrayList<Exercise>) {
        Log.d(TAG, "setExercises: $exercises")
        this.exercises = exercises
    }

    fun addUserExercise(exercise: Exercise) {
        Log.d(TAG, "addExercise: $exercise")
        exercises.add(exercise)
    }

    fun removeUserExercise(exercise: Exercise) {
        Log.d(TAG, "removeExercise: $exercise")
        exercises.remove(exercise)
    }

    fun clearUserExercises() {
        Log.d(TAG, "clearExercises: ")
        exercises.clear()
    }

    fun getExercise(index: Int): Exercise {
        Log.d(TAG, "getExercise: Index: ${index}, Exercise: ${exercises[index]}")
        return exercises[index]
    }
}