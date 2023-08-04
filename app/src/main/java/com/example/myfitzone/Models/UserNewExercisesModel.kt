package com.example.myfitzone.Models

import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.DatabaseExercise
import com.example.myfitzone.DataModels.UserExercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class UserNewExercisesModel: ViewModel() {


    private val TAG = "UserExercisesModel"
    private var newExercise: UserExercise? = null
    private var dataBaseExerciseTemplate: DatabaseExercise? = null

    fun setSelectedExercise(databaseExercise: DatabaseExercise) {
        dataBaseExerciseTemplate = databaseExercise
    }

    fun getSelectedExercise(): DatabaseExercise? {
        return dataBaseExerciseTemplate
    }

    fun clearSelectedExercise() {
        dataBaseExerciseTemplate = null
    }

    fun saveUserExercise(userExercise: UserExercise, mycallback: FirestoreGetCompleteCallbackArrayList) {
        val db = Firebase.firestore
        val userUID = FirebaseAuth.getInstance().currentUser?.uid
        if(userUID == null){
            mycallback.onGetFailure("User not logged in")
            return
        }
        val simpleDateFormat = java.text.SimpleDateFormat("yyyy-MM")
        val docID = simpleDateFormat.format(userExercise.timeAdded)
        val docData = mapOf(
            userExercise.timeAdded.toString() to userExercise,
        )
        db.collection("users")
            .document(userUID!!)
            .collection("userExerciseList")
            .document(docID)
            .set(docData, SetOptions.merge() )
            .addOnSuccessListener {
                mycallback.onGetComplete(arrayListOf("Success"))
            }
            .addOnFailureListener { exception ->
                mycallback.onGetFailure(exception.toString())
            }
    }
}