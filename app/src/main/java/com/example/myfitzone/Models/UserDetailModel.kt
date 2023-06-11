package com.example.myfitzone.Models

import androidx.lifecycle.ViewModel
import com.example.myfitzone.DataModels.User

class UserDetailModel : ViewModel() {
    private val TAG = "UserDetailModel"
    var TempRegistrationUser: User? = null

}