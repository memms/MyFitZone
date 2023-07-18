package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallback
import com.example.myfitzone.DataModels.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DatabaseUserModel: ViewModel() {
//Use Firestore

    private val TAG = "DatabaseModel"
    private val db = Firebase.firestore

    var user: User? = null
    var exerciseGroupsG: MutableLiveData<ArrayList<String>> = MutableLiveData()

    //create user
    fun createUser(userToRegister: User) {

        val user = hashMapOf(
            "email" to userToRegister.email,
            "username" to userToRegister.username,
            "name" to mapOf(
                "first" to userToRegister.name["first"].toString(),
                "last" to userToRegister.name["last"].toString()
            ),
            "DOB" to userToRegister.DOB,
            "Weight" to userToRegister.Weight,
            "Height" to userToRegister.Height,
            "Gender" to userToRegister.Gender,
        )

        db.collection("users")
            .document(userToRegister.UID)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                this.user =
                    userToRegister
                Log.d(TAG, "createUser: ${this.user.toString()}")
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
    /**
     *update multiple user data values
     *REMINDER: NESTED NEEDS TO BE with dot key, EX: "name.first"
     **/
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