package com.example.myfitzone.DataModels

data class DatabaseExercise(
    //0 -> title, 1 -> description, 3,4,5... -> exercise attributes
    var exerciseDetailList: ArrayList<String>,
)