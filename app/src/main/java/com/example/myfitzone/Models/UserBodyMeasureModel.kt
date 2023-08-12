package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.CalenderEventData
import com.example.myfitzone.DataModels.PublicSocialData
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.example.myfitzone.Utils.toMetricHeight
import com.example.myfitzone.Utils.toMetricWeight
import com.example.myfitzone.Utils.toPublicSocialData
import com.google.android.gms.tasks.Tasks.whenAllComplete
import com.google.firebase.auth.ktx.auth
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

        val task1 = docRef.document(formated)
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
        val task2 = docRef.document(formated)
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
                }
                else{
                    Log.d(TAG, "getSelectedBodyMeasureMetrics: No data")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                callback.onGetFailure(exception.toString())
            }
        //TODO Switch the gets to TASKS with .await() to make sure they are done before the success listener

        whenAllComplete(task1, task2).addOnSuccessListener {
            callback.onGetComplete(list)
        }

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
                updateLeaderBoards(userBodyMetrics, callback = mycallBack)
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
                updateLeaderboardsDelete(userBodyMetrics, callback = callback)
            }
            .addOnFailureListener { e ->
                callback.onGetFailure(e.toString())
            }

    }

    private fun updateLeaderboardsDelete(
        userBodyMetrics: UserBodyMetrics,
        callback: FirestoreGetCompleteCallbackArrayList
    ){
        val userID = Firebase.auth.currentUser?.uid
        if(userID== null){
            callback.onGetFailure("User not logged in")
            return
        }

        getLeaderBoards(userBodyMetrics.metricName, object : FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                if(result == "No data"){
                    callback.onGetComplete(arrayListOf("Success"))
                    return
                }
                val currentLeaderVal = result as PublicSocialData
                if(userBodyMetrics.timestamp == currentLeaderVal.updated){
                    db.collection("leaderboards")
                        .document(userID)
                        .update(userBodyMetrics.metricName, FieldValue.delete())
                        .addOnSuccessListener {
                            Log.d(TAG, "updateLeaderboardsDelete: Success")
                            callback.onGetComplete(arrayListOf("Success"))
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "updateLeaderboardsDelete: Failure")
                            callback.onGetFailure(it.toString())
                        }
                }
            }

            override fun onGetFailure(string: String) {
                callback.onGetFailure(string)
            }
        })
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
                updateLeaderBoards(userBodyMetric, callback)
            }
            .addOnFailureListener { e ->
                callback.onGetFailure(e.toString())
            }
    }

    private fun getLeaderBoards(bodyMetricName: String, callback: FirestoreGetCompleteAny){
        val userID = Firebase.auth.currentUser?.uid
        if(userID== null){
            callback.onGetFailure("User not logged in")
            return
        }
        db.collection("leaderboards")
            .document(userID)
            .get()
            .addOnSuccessListener { document ->
                document.get(bodyMetricName).let {currentLeaderVal ->
                    if (currentLeaderVal != null) {
                        val currentLeaderValDouble = currentLeaderVal as HashMap<String, Any>
                        val publicDataObject = PublicSocialData(
                            logo = currentLeaderValDouble["logo"] as String,
                            name = currentLeaderValDouble["name"] as String,
                            value = currentLeaderValDouble["value"] as String,
                            unit = currentLeaderValDouble["unit"] as String,
                            updated = currentLeaderValDouble["updated"] as Long,
                            type = currentLeaderValDouble["type"] as String
                        )
                        callback.onGetComplete(publicDataObject)
                        return@addOnSuccessListener
                    }
                }
                callback.onGetComplete("No data")
            }
            .addOnFailureListener {
                callback.onGetFailure(it.toString())
            }
    }


    private fun updateLeaderBoards(userBodyMetrics: UserBodyMetrics, callback: FirestoreGetCompleteCallbackArrayList){
        val userID = Firebase.auth.currentUser?.uid
        if(userID== null){
            callback.onGetFailure("User not logged in")
            return
        }

        getLeaderBoards(userBodyMetrics.metricName, object : FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                if(result == "No data"){
                    val docData = mapOf(
                        userBodyMetrics.metricName to userBodyMetrics.toPublicSocialData()
                    )
                    db.collection("leaderboards")
                        .document(userID)
                        .set(docData, SetOptions.merge())
                        .addOnSuccessListener {
                            callback.onGetComplete(arrayListOf("Success"))
                        }
                        .addOnFailureListener {
                            callback.onGetFailure(it.toString())
                        }
                    return
                }
                val currentLeaderVal = result as PublicSocialData
                if(userBodyMetrics.timestamp >= currentLeaderVal.updated){
                    val docData = mapOf(
                        userBodyMetrics.metricName to userBodyMetrics.toPublicSocialData()
                    )
                    db.collection("leaderboards")
                        .document(userID)
                        .set(docData, SetOptions.merge())
                        .addOnSuccessListener {
                            callback.onGetComplete(arrayListOf("Success"))
                        }
                        .addOnFailureListener {
                            callback.onGetFailure(it.toString())
                        }
                    if(userBodyMetrics.metricName == "Weight" || userBodyMetrics.metricName == "Height"){
                        val docData = if (userBodyMetrics.metricName == "Weight"){
                            mapOf(
                                "Weight" to userBodyMetrics.metricValue.toMetricWeight()
                            )
                        }else{
                            mapOf(
                                "Height" to userBodyMetrics.metricValue.toMetricHeight()
                            )
                        }
                        db.collection("users")
                            .document(userID)
                            .set(docData, SetOptions.merge())
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                    }
                }
            }

            override fun onGetFailure(string: String) {
                callback.onGetFailure(string)
            }
        })

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