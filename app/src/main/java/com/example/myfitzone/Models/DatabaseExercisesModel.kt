package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallback
import com.example.myfitzone.DataModels.DatabaseExercise
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DatabaseExercisesModel: ViewModel() {

    private val TAG = "DatabaseExercisesModel"
    private val exerciseGroups: ArrayList<String> = arrayListOf()
    private val exerciseList = hashMapOf<String, String>()
    private val db = Firebase.firestore
    private var selectedGroup: String = ""


    fun getExerciseGroups(callback: FirestoreGetCompleteCallback) {
        //reduce calls to database & reduce visual lag
        if(exerciseGroups.isNotEmpty()) {
            callback.onGetComplete(exerciseGroups)
            return
        }
        db.collection("workoutList")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    exerciseGroups.add(document.id)
                    exerciseList[document.id] = document.data.toString()
                }
                Log.d(TAG, "getExerciseGroups: $exerciseGroups")
                callback.onGetComplete(exerciseGroups)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getExercises(): String {
        if(selectedGroup.isEmpty()) {
            return ""
        }
        val key = selectedGroup
        return exerciseList[key].toString()
    }

    fun setSelectedGroup(group: String) {
        selectedGroup = group
    }

    fun getSelectedGroup(): String {
        return selectedGroup
    }

    fun clearSelectedGroup() {
        selectedGroup = ""
    }

    fun addNewExercise(exerciseDetails:DatabaseExercise){
        val docDataMap = hashMapOf(
            "exerciseName" to exerciseDetails.exerciseName,
            "exerciseDescription" to exerciseDetails.exerciseDescription,
            "exerciseFieldsList" to exerciseDetails.exerciseFieldsList,
            "creatorName" to exerciseDetails.creatorName,
        )
        val docData = hashMapOf(
            exerciseDetails.exerciseName to docDataMap
        )
        db.collection("workoutList")
            .document(selectedGroup)
            .set(docData)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}