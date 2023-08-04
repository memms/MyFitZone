package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.CalenderEventData
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.example.myfitzone.DataModels.UserExercise
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class JournalCalendarModel: ViewModel() {

    private var userExercise: UserExercise? = null
    private val TAG = "UserExerciseModel"
    private val db = Firebase.firestore
    private val eventsList : MutableMap<String, MutableMap<LocalDate, MutableList<CalenderEventData>>> = mutableMapOf<String, MutableMap<LocalDate ,MutableList<CalenderEventData>>>()


    fun getUserCalendar(yearMonth: String, callback: FirestoreGetCompleteAny) {
        Log.d(TAG, "getUserExercises: yearMonth $yearMonth")
        if (eventsList.containsKey(yearMonth) && !eventsList[yearMonth].isNullOrEmpty()){
            Log.d(TAG, "getUserExercises: eventsList contains key")
            callback.onGetComplete(eventsList[yearMonth]!!)
            return
        }
        eventsList[yearMonth] = mutableMapOf()
        val UserID = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(UserID)
            .collection("userExerciseList")
            .document(yearMonth)
            .get()
            .addOnSuccessListener {document ->
                if (document.exists()){
                    Log.d(TAG, "getUserExercises: document exists $yearMonth ${document.id}")
                    document.data?.let {
                        it.forEach { map->
                            val userExercise = map.value as HashMap<String, Any>
                            val userExerciseObject = UserExercise(
                                name = userExercise["name"] as String,
                                exerciseGroup = userExercise["exerciseGroup"] as String,
                                notes = userExercise["notes"] as String,
                                fieldmap = userExercise["fieldmap"] as HashMap<String, *>,
                                timeAdded = userExercise["timeAdded"] as Long
                            )
                            Log.d(TAG, "getUserCalendar: userExercise $userExercise")
                            val localDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(userExerciseObject.timeAdded), UTC).toLocalDate()
                            val title = "${userExerciseObject.exerciseGroup}: ${userExerciseObject.name}"
                            val desc = "${userExerciseObject.fieldmap["sets"]} sets"
                            if (eventsList[yearMonth]!![localDate] == null){
                                eventsList[yearMonth]!![localDate] = mutableListOf()
                            }
                            eventsList[yearMonth]!![localDate]?.add(
                                CalenderEventData(type = "exercise", title = title,
                                    description = desc, timeMillis = userExerciseObject.timeAdded)
                            )
                            Log.d(TAG, "getUserCalendar: eventlist ${eventsList[yearMonth]!![localDate]}")
                        }
                    }
                }
                callback.onGetComplete(eventsList[yearMonth] ?: return@addOnSuccessListener)
            }
            .addOnFailureListener {e->
                callback.onGetFailure(e.toString())
            }
        db.collection("users")
            .document(UserID)
            .collection("userBodyMeasurements")
            .document(yearMonth)
            .get()
            .addOnSuccessListener {document ->
                if(document.exists()){
                    document.data?.let {
                        it.forEach { map ->
                            val userBodyMetrics = map.value as HashMap<String, Any>
                            val userBodyMetricsObject = UserBodyMetrics(
                                timestamp = userBodyMetrics["timestamp"] as Long,
                                metricType = userBodyMetrics["metricType"] as String,
                                metricName = userBodyMetrics["metricName"] as String,
                                metricValue = userBodyMetrics["metricValue"] as Double,
                                dateLastModified = userBodyMetrics["dateLastModified"] as Long
                            )
                            val localDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(userBodyMetricsObject.timestamp), UTC).toLocalDate()
                            val title = userBodyMetricsObject.metricName
                            val desc = "${userBodyMetricsObject.metricValue} ${FieldUnits.unitOfBody(userBodyMetricsObject.metricName)}"
                            if (eventsList[yearMonth]!![localDate] == null){
                                eventsList[yearMonth]!![localDate] = mutableListOf()
                            }
                            eventsList[yearMonth]!![localDate]?.add(CalenderEventData(type = "bodyMeasure", title = title,
                                description = desc, timeMillis = userBodyMetricsObject.timestamp))
                        }
                    }
                }
                callback.onGetComplete(eventsList[yearMonth] ?: return@addOnSuccessListener)
            }
            .addOnFailureListener { e->
                callback.onGetFailure(e.toString())
            }
    }
}