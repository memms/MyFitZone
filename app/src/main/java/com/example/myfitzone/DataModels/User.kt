package com.example.myfitzone.DataModels

import java.util.Date

data class User(var UID:String,
                var username: String,
                var email: String,
                var name: HashMap<String, String>,
                var DOB: Date,
                var Weight: Float,
                var Height: Float,
                var Gender: String)
