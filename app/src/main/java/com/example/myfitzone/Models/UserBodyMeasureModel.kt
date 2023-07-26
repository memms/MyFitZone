package com.example.myfitzone.Models

import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date

class UserBodyMeasureModel: ViewModel() {
    private val TAG = "UserBodyMeasureModel"
    private val db = Firebase.firestore
    private var selectedType = ""
    private var selectedName = ""

    fun getAllLastBodyMeasureMetrics() {

    }

    fun getSpecificBodyMeasureMetrics() {

    }

    fun newBodyMeasurement(userBodyMetrics: UserBodyMetrics, mycallBack: FirestoreGetCompleteCallbackArrayList) {
        val userID = Firebase.auth.currentUser?.uid
        if(userID== null){
            mycallBack.onGetFailure("User not logged in")
            return
        }
        val docData = hashMapOf(
            "${userBodyMetrics.timestamp}" to userBodyMetrics
        )
        val simpleDateFormat = SimpleDateFormat("yyyy-MM")
        val formated = simpleDateFormat.format(System.currentTimeMillis())

        db.collection("users")
            .document(userID)
            .collection("userBodyMeasurements")
            .document(formated)
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {
                mycallBack.onGetComplete(arrayListOf("Success"))
            }
            .addOnFailureListener { e ->
                mycallBack.onGetFailure("Error adding new body measurement")
            }

    }

    fun deleteBodyMeasurement() {

    }

    fun updateBodyMeasurement() {

    }

    fun setSelectedType(type: String) {
        selectedType = type
    }

    fun setSelectedName(name: String) {
        selectedName = name
    }

    fun getSelectedType(): String {
        return selectedType
    }

    fun getSelectedName(): String {
        return selectedName
    }





}