package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackHashMap
import com.example.myfitzone.DataModels.DatabaseExercise
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DatabaseExercisesModel: ViewModel() {

    private val TAG = "DatabaseExercisesModel"
    private var exerciseGroups: ArrayList<String> = arrayListOf()
    private val exerciseList = hashMapOf<String, DatabaseExercise>()
    private val db = Firebase.firestore
    private var selectedGroup: String = ""


    fun getExerciseGroups(callback: FirestoreGetCompleteCallbackArrayList) {
        //reduce calls to database & reduce visual lag
        if(exerciseGroups.isNotEmpty()) {
            callback.onGetComplete(exerciseGroups)
            return
        }
        val ids = db.collection("workoutList")
        Log.d(TAG, "getExerciseGroups: IDS $ids")
        //updated to save read costs
        db.collection("workoutList")
            .document("exerciseGroups")
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "getExerciseGroups: ${it.data}")
                exerciseGroups = it.data!!["list"] as ArrayList<String>
                exerciseGroups.sort()
                Log.d(TAG, "getExerciseGroups: $exerciseGroups")
                callback.onGetComplete(exerciseGroups)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getExercises(callback: FirestoreGetCompleteCallbackHashMap) {
        //sanity check
        if(selectedGroup.isEmpty()) {
            return
        }
        exerciseList.clear()
        val key = selectedGroup
        Log.d(TAG, "getExercises: $key")
        db.collection("workoutList")
            .document(key)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    val keys = document.data?.keys
                    keys!!.forEach() { key ->
                        val exercise = document.data!![key] as HashMap<String, Any>
                        val exerciseName = exercise["exerciseName"].toString()
                        val exerciseDescription = exercise["exerciseDescription"].toString()
                        val exerciseFieldsList = exercise["exerciseFieldsList"] as ArrayList<String>
                        val creatorName = exercise["creatorName"].toString()
                        val exerciseDetails = DatabaseExercise(
                            exerciseName,
                            exerciseDescription,
                            exerciseFieldsList,
                            creatorName
                        )
                        exerciseList[exerciseName] = exerciseDetails
                    }
                    callback.onGetComplete(exerciseList)
                    Log.d(TAG, "getExercises: $exerciseList")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
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

    fun fieldExists(callback: FirestoreGetCompleteCallbackArrayList, exerciseName: String){
        db.collection("workoutList")
            .document(selectedGroup)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    if(document.get(exerciseName)!=null){
                        Log.d(TAG, "fieldExists: true")
                        callback.onGetComplete(arrayListOf("true"))
                    }
                    else{
                        Log.d(TAG, "fieldExists: false")
                        callback.onGetComplete(arrayListOf("false"))
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "fieldExists: failure $it")
                callback.onGetFailure(it.toString())
            }
    }
    fun addNewExercise(mycallback: FirestoreGetCompleteCallbackArrayList, exerciseDetails:DatabaseExercise){
        if(selectedGroup.isEmpty()) {
            return
        }
        fieldExists(object: FirestoreGetCompleteCallbackArrayList{
            override fun onGetComplete(result: ArrayList<String>) {
                if(result[0]=="true"){
                    mycallback.onGetComplete(arrayListOf("exists"))
                    return
                }
                val docData = hashMapOf(
                    exerciseDetails.exerciseName to hashMapOf(
                        "exerciseName" to exerciseDetails.exerciseName,
                        "exerciseDescription" to exerciseDetails.exerciseDescription,
                        "exerciseFieldsList" to exerciseDetails.exerciseFieldsList,
                        "creatorName" to exerciseDetails.creatorName,
                    )
                )
                Log.d(TAG, "addNewExercise: $docData")
                Log.d(TAG, "addNewExercise: $selectedGroup")
                db.collection("workoutList")
                    .document(selectedGroup)
                    .set(docData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        mycallback.onGetComplete(arrayListOf("success"))
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error writing document", e)
                        mycallback.onGetFailure(e.toString())
                    }
            }

            override fun onGetFailure(string: String) {
                Log.d(TAG, "onGetFailure: ")
                mycallback.onGetFailure(string)
            }
        }, exerciseDetails.exerciseName)
    }
}