package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.DataModels.User
import java.util.Calendar

class UserDetailModel : ViewModel() {
    private val TAG = "UserDetailModel"

    private var TempRegistrationUser = User("", "", "", hashMapOf("first" to "", "last" to ""), "", 0f, 0f, "")

    fun getUser(): User {
        return TempRegistrationUser
    }

    fun setUID(UID: String) {
        TempRegistrationUser.UID = UID
        Log.d(TAG, "setUID: ${TempRegistrationUser.UID}")
    }

    fun setUsername(username: String) {
        TempRegistrationUser.username = username
        Log.d(TAG, "setUsername: ${TempRegistrationUser.username}")
    }

    fun setEmail(email: String) {
        TempRegistrationUser.email = email
        Log.d(TAG, "setEmail: ${TempRegistrationUser.email}")
    }

    fun setFirstName(firstName: String) {
        TempRegistrationUser.let { it.name["first"] = firstName }
        Log.d(TAG, "setFirstName: ${TempRegistrationUser.name["first"]}")
    }

    fun setLastName(lastName: String) {
        TempRegistrationUser.let { it.name["last"] = lastName }
        Log.d(TAG, "setLastName: ${TempRegistrationUser.name["last"]}")
    }

    fun setDOB(DOB: String) {
        TempRegistrationUser.let { it.DOB = DOB }
        Log.d(TAG, "setDOB: ${TempRegistrationUser.DOB}")
    }

    fun setWeight(Weight: Float) {
        TempRegistrationUser.let { it.Weight = Weight }
        Log.d(TAG, "setWeight: ${TempRegistrationUser.Weight}")
    }

    fun setHeight(Height: Float) {
        TempRegistrationUser.let { it.Height = Height }
        Log.d(TAG, "setHeight: ${TempRegistrationUser.Height}")
    }

    fun setGender(gender: String){
        TempRegistrationUser.let { it.Gender = gender }
        Log.d(TAG, "setGender: ${TempRegistrationUser.Gender}")
    }

    fun getGender(): String {
        TempRegistrationUser.let { return it.Gender}
    }

    fun getUID(): String {
        TempRegistrationUser.let { return it.UID }
    }

    fun getUsername(): String {
        TempRegistrationUser.let { return it.username }
    }

    fun getEmail(): String {
        TempRegistrationUser.let { return it.email }
    }

    fun getFirstName(): String? {
        TempRegistrationUser.let { return it.name["first"] }
    }

    fun getLastName(): String? {
        TempRegistrationUser.let { return it.name["last"] }
    }

    fun getDOB(): String {
        TempRegistrationUser.let { return it.DOB }
    }

    fun getWeight(): Float {
        TempRegistrationUser.let { return it.Weight }
    }

    fun getHeight(): Float {
        TempRegistrationUser.let { return it.Height }
    }

}