package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.CalenderEventData
import com.example.myfitzone.DataModels.UserExercise
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class UserExerciseModel: ViewModel() {

    private var userExercise: UserExercise? = null
    private val TAG = "UserExerciseModel"
    val db = Firebase.firestore
    val eventsList = mutableMapOf<String, MutableMap<LocalDate ,CalenderEventData>>()

    fun getUserExercises(YearMonth: String, callback: FirestoreGetCompleteAny) {
        Log.d(TAG, "getUserExercises: yearMonth $YearMonth")
        if (eventsList.containsKey(YearMonth)) {
            Log.d(TAG, "getUserExercises: eventsList contains key")
            callback.onGetComplete(eventsList[YearMonth]!!)
            return
        }
        val UserID = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(UserID)
            .collection("userExerciseList")
            .document(YearMonth)
            .get()
            .addOnSuccessListener {document ->
                if (document.exists()){
                    Log.d(TAG, "getUserExercises: document exists $YearMonth ${document.id}")
                    eventsList[YearMonth] = mutableMapOf()
                    val userExerciseList = arrayListOf<UserExercise>()
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
                            userExerciseList.add(userExerciseObject)
                        }
                    }
                    for (userExercise in userExerciseList){
                        val localDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(userExercise.timeAdded), UTC).toLocalDate()
                        val title = "${userExercise.exerciseGroup}: ${userExercise.name}"
                        val desc = "${userExercise.fieldmap["sets"]} sets"
                        eventsList[YearMonth]!![localDate]= CalenderEventData(type = "exercise", title = title,
                            description = desc, timeMillis = userExercise.timeAdded)
                    }
                }

                callback.onGetComplete(eventsList[YearMonth] ?: return@addOnSuccessListener)
            }
            .addOnFailureListener {e->
                callback.onGetFailure(e.toString())
            }
    }
}