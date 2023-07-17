package com.example.myfitzone.DataModels

data class Exercise(
    var id: String,
    var name: String,
    var description: String,
    var image: String,
    var map: HashMap<String, String>,
    var category: String,
    var creator: String,
    )
