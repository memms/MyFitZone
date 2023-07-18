package com.example.myfitzone.DataModels

data class Exercise(
    var exerciseGroup: String,
    var name: String,
    var description: String,
//    var image: String,
    var map: HashMap<String, String>,
    var creator: String,
    )
