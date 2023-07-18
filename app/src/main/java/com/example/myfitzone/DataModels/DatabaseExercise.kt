package com.example.myfitzone.DataModels

data class DatabaseExercise(
    var exerciseName : String,
    var exerciseDescription: String,
    var exerciseFieldsList: ArrayList<String>,
    var creatorName: String,
)