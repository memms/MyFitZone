package com.example.myfitzone.Models

import androidx.lifecycle.ViewModel
import com.example.myfitzone.DataModels.DatabaseExercise
import com.example.myfitzone.DataModels.UserExercise

class UserNewExercisesModel: ViewModel() {


    private val TAG = "UserExercisesModel"
    private var newExercise: UserExercise? = null
    private var dataBaseExerciseTemplate: DatabaseExercise? = null

    fun setSelectedExercise(databaseExercise: DatabaseExercise) {
        dataBaseExerciseTemplate = databaseExercise
    }

    fun getSelectedExercise(): DatabaseExercise? {
        return dataBaseExerciseTemplate
    }

    fun clearSelectedExercise() {
        dataBaseExerciseTemplate = null
    }
}