package com.example.myfitzone.DataModels

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import java.util.Date

data class User(@Exclude var UID:String = "",
                @PropertyName("username") var username: String = "",
                @PropertyName("email") var email: String = "",
                @PropertyName("name") var name: HashMap<String, String> = hashMapOf("first" to "", "last" to ""),
                @PropertyName("DOB") var DOB: String = "",
                @PropertyName("Weight") var Weight: Float = 0f,
                @PropertyName("Height") var Height: Float= 0f,
                @PropertyName("Gender") var Gender: String= "")
