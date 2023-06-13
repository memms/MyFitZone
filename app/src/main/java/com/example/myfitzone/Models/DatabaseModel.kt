package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.DataModels.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class DatabaseModel: ViewModel() {
//Use Firestore

    private val TAG = "DatabaseModel"
    private val db = Firebase.firestore

    var user: User? = null

    //create user
    fun createUser(UID:String, username:String, email: String, fName: String, lName: String, DOB: Date,
                   Weight: Float, Height: Float, Gender: String) {
        val user = hashMapOf(
            "email" to email,
            "username" to username,
            "name" to hashMapOf(
                "first" to fName,
                "last" to lName
            ),
            "DOB" to DOB,
            "Weight" to Weight,
            "Height" to Height,
            "Gender" to Gender,
        )

        db.collection("users")
            .document(UID)
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot successfully written with ID: ${documentReference}!")
                this.user =
                    User(UID, username, email,
                        hashMapOf("first" to fName, "last" to lName),
                        DOB.toString(), Weight, Height, Gender)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e) }
    }

    //get user data
    fun getUserData(UID:String) {
        db.collection("users")
            .document(UID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    document.let { user = it.toObject(User::class.java) }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    //update a user data
    //REMINDER: NESTED NEEDS TO BE with dot  key, EX: "name.first"
    fun updateAValue(key: String, value: Any) {
        db.collection("users")
            .document(user!!.UID)
            .update(key, value)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    //update multiple user data values
    //REMINDER: NESTED NEEDS TO BE with dot key, EX: "name.first"
    fun updateMultiValue(hashMap: HashMap<String, Any>) {
        db.collection("users")
            .document(user!!.UID)
            .update(hashMap)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    //delete user data

    //create workout

    //get workout data

    //update workout data

    //delete workout data

    //create exercise

    //get exercise data

    //update exercise data

    //delete exercise data

}