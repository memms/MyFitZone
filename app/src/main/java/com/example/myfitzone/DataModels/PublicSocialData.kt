package com.example.myfitzone.DataModels

data class PublicSocialData(
    var uid : String,   //uid of user
    var name: String,   //name of exercise, body, etc.
    var value: String, //value of exercise, body, etc.
    var fields: String, //fields of exercise to calculate score for leaderboard
    var unit: String,   //unit of exercise, body, etc.
    var updated: Long,  //last added
    var type: String,   //type of measurement (body, exercise, etc.)
    var logo: String,   //logo of measurement
)
