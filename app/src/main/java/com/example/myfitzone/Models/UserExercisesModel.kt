package com.example.myfitzone.Models

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.DatabaseExercise
import com.example.myfitzone.DataModels.UserExercise
import com.example.myfitzone.Utils.toPublicSocialData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserExercisesModel: ViewModel() {


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

    fun saveUserExercise(userExercise: UserExercise, template: DatabaseExercise, mycallback: FirestoreGetCompleteCallbackArrayList) {
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
                addToLeaderBoard(userExercise, template, mycallback)
            }
            .addOnFailureListener { exception ->
                mycallback.onGetFailure(exception.toString())
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun addToLeaderBoard(userExercise: UserExercise, template: DatabaseExercise, mycallback: FirestoreGetCompleteCallbackArrayList) {
        val db = Firebase.firestore
        val userUID = FirebaseAuth.getInstance().currentUser?.uid
        if(userUID == null){
            mycallback.onGetFailure("User not logged in")
            return
        }
        template.exerciseFieldsList.add("sets")
        Log.d(TAG, "addToLeaderBoard: ${userExercise.fieldmap.keys.toSet()} \n ${template.exerciseFieldsList.toSet()}")
        if(template.exerciseFieldsList.toSet() != userExercise.fieldmap.keys.toSet()){
            mycallback.onGetComplete(arrayListOf("noLeaderBoard"))
            return
        }
        val publicData = userExercise.toPublicSocialData()
        val docData = mapOf(
            userExercise.name to publicData,
        )
        db.collection("leaderboards")
            .document(userUID)
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {
                mycallback.onGetComplete(arrayListOf("Success"))
            }
            .addOnFailureListener { exception ->
                mycallback.onGetFailure(exception.toString())
            }
    }

    fun getUserExercises(docNames: List<String>, callback: FirestoreGetCompleteAny){
        val db = Firebase.firestore
        val userUID = FirebaseAuth.getInstance().currentUser?.uid
        if(userUID == null){
            callback.onGetFailure("User not logged in")
            return
        }

        db.collection("users")
            .document(userUID)
            .collection("userExerciseList")
            .whereIn(FieldPath.documentId(), docNames)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback.onGetComplete("No Exercises Found")
                    return@addOnSuccessListener
                }
                val userExerciseList = arrayListOf<UserExercise>()
                for (document in documents) {
                    document.data.let {
                        it.forEach {map ->
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
                }
                callback.onGetComplete(userExerciseList)
            }
            .addOnFailureListener { exception ->
                callback.onGetFailure(exception.toString())
            }
    }
}