package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.CalenderEventData
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar.DAY_OF_MONTH

class UserBodyMeasureModel: ViewModel() {
    private val TAG = "UserBodyMeasureModel"
    private val db = Firebase.firestore
    private var selectedType = ""
    private var selectedName = ""
    private val eventsList = mutableMapOf<String, MutableMap<LocalDate, CalenderEventData>>()

    fun getUserBodyMeasureYearMonth(yearMonth: String, callback: FirestoreGetCompleteAny){
        Log.d(TAG, "getUserBodyMeasureYearMonth: $yearMonth")

    }

    fun getAllLastBodyMeasureMetrics() {

    }

    fun getSelectedBodyMeasureMetrics(callback: FirestoreGetCompleteAny) {
        val userID = Firebase.auth.currentUser?.uid
        if(userID== null){
            return
        }
        val simpleDateFormat = SimpleDateFormat("yyyy-MM")
        val range = arrayListOf(Instant.now().toEpochMilli(), Instant.now().toEpochMilli()-2592000000)
        var formated = simpleDateFormat.format(range[0])
        Log.d(TAG, "getSelectedBodyMeasureMetrics: formated1 $formated")

        var list = mutableMapOf<Long,UserBodyMetrics>()
        val docRef = db.collection("users")
            .document(userID)
            .collection("userBodyMeasurements")

        docRef.document(formated)
        .get()
        .addOnSuccessListener { document ->
            if(document.exists()){
                document.data!!.forEach {
                    val userBodyMetrics = it.value as HashMap<String, Any>
                    val userBodyMetricsObject = UserBodyMetrics(
                        userBodyMetrics["timestamp"] as Long,
                        userBodyMetrics["metricType"] as String,
                        userBodyMetrics["metricName"] as String,
                        userBodyMetrics["metricValue"] as Double,
                        userBodyMetrics["dateLastModified"] as Long
                    )
                    if(userBodyMetricsObject.metricName == selectedName && !list.containsKey(userBodyMetricsObject.timestamp)){
                        list[userBodyMetricsObject.timestamp] = userBodyMetricsObject
                    }
                }
                list = list.toSortedMap()
                Log.d(TAG, "getSelectedBodyMeasureMetrics: filteredList $list")
                callback.onGetComplete(list)
//                    Log.d(TAG, "getSelectedBodyMeasureMetrics: list $list")
            }
            else{
                Log.d(TAG, "getSelectedBodyMeasureMetrics: No data")
            }
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
            callback.onGetFailure(exception.toString())
        }

        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = range[0]
        cal.set(DAY_OF_MONTH, -1)
        formated = simpleDateFormat.format(cal.timeInMillis)
        Log.d(TAG, "getSelectedBodyMeasureMetrics: formated2 $formated")
        docRef.document(formated)
            .get()
            .addOnSuccessListener { document ->
//                Log.d(TAG, "getSelectedBodyMeasureMetrics: doc $document")
                if(document.exists()){
                    document.data!!.forEach {
                        val userBodyMetrics = it.value as HashMap<String, Any>
                        val userBodyMetricsObject = UserBodyMetrics(
                            userBodyMetrics["timestamp"] as Long,
                            userBodyMetrics["metricType"] as String,
                            userBodyMetrics["metricName"] as String,
                            userBodyMetrics["metricValue"] as Double,
                            userBodyMetrics["dateLastModified"] as Long
                        )
                        if(userBodyMetricsObject.metricName == selectedName
                            && userBodyMetricsObject.timestamp > range[1]
                            && userBodyMetricsObject.timestamp < range[0]
                            && !list.containsKey(userBodyMetricsObject.timestamp)){
                            list[userBodyMetricsObject.timestamp] = userBodyMetricsObject
                        }
                    }
                    list = list.toSortedMap()
                    Log.d(TAG, "getSelectedBodyMeasureMetrics: filteredList $list")
                    callback.onGetComplete(list)
//                    Log.d(TAG, "getSelectedBodyMeasureMetrics: list $list")
                }
                else{
                    Log.d(TAG, "getSelectedBodyMeasureMetrics: No data")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                callback.onGetFailure(exception.toString())
            }
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
        val formated = simpleDateFormat.format(userBodyMetrics.timestamp)


        db.collection("users")
            .document(userID)
            .collection("userBodyMeasurements")
            .document(formated)
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {
                mycallBack.onGetComplete(arrayListOf("Success"))
            }
            .addOnFailureListener { e ->
                mycallBack.onGetFailure(e.toString())
            }

    }

    fun deleteBodyMeasurement(
        userBodyMetrics: UserBodyMetrics,
        callback: FirestoreGetCompleteCallbackArrayList
    ) {
        val userID = Firebase.auth.currentUser?.uid
        if(userID== null){
            callback.onGetFailure("User not logged in")
            return
        }
        val simpleDateFormat = SimpleDateFormat("yyyy-MM")
        val formated = simpleDateFormat.format(userBodyMetrics.timestamp)
        db.collection("users")
            .document(userID)
            .collection("userBodyMeasurements")
            .document(formated)
            .update("${userBodyMetrics.timestamp}", FieldValue.delete())
            .addOnSuccessListener {
                callback.onGetComplete(arrayListOf("Success"))
            }
            .addOnFailureListener { e ->
                callback.onGetFailure(e.toString())
            }

    }

    fun updateBodyMeasurement(
        userBodyMetric: UserBodyMetrics,
        fieldName: Long,
        callback: FirestoreGetCompleteCallbackArrayList
    ) {
        val userID = Firebase.auth.currentUser?.uid
        if(userID== null){
            callback.onGetFailure("User not logged in")
            return
        }
        val simpleDateFormat = SimpleDateFormat("yyyy-MM")
        val formated = simpleDateFormat.format(fieldName)
        val docData = hashMapOf(
            "${userBodyMetric.timestamp}" to userBodyMetric
        )
        db.collection("users")
            .document(userID)
            .collection("userBodyMeasurements")
            .document(formated)
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {
                callback.onGetComplete(arrayListOf("Success"))
            }
            .addOnFailureListener { e ->
                callback.onGetFailure(e.toString())
            }
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