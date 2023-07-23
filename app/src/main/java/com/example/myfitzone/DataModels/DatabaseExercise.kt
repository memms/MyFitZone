package com.example.myfitzone.DataModels

import com.google.firebase.database.PropertyName

data class DatabaseExercise(
    @PropertyName("exerciseGroup") var exerciseGroup: String,
    @PropertyName("exerciseName") var exerciseName : String,
    @PropertyName("exerciseDescription") var exerciseDescription: String,
    @PropertyName("exerciseFieldsList") var exerciseFieldsList: ArrayList<String>,
    @PropertyName("creatorName") var creatorName: String,

)