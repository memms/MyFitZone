package com.example.myfitzone.Models

import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.PublicSocialData
import com.example.myfitzone.DataModels.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SocialDataModel: ViewModel() {

    private val TAG = "SocialDataModel"
    private val db = Firebase.firestore

    fun getMyUserInfo(callback: FirestoreGetCompleteAny) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            callback.onGetFailure("User not logged in/connectivity issue")
            return
        }
        getUserInfo(uid, callback)
    }

    fun getUserInfo(uid: String, callback: FirestoreGetCompleteAny) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {document->
                if(document.exists()){
                    val data = document.toObject(User::class.java)
                    if (data != null) {
                        callback.onGetComplete(data)
                    }
                }
                else {
                    callback.onGetFailure("User not found")
                }
            }
            .addOnFailureListener {

            }
    }

    private fun getUserSocialData(uid: String, callback: FirestoreGetCompleteAny) {

        db.collection("leaderboards")
            .document(uid)
            .get()
            .addOnSuccessListener {document ->
            if (document.exists()) {
                val data = document.data as HashMap<String, Any>
                val socialData = ArrayList<PublicSocialData>()
                for (item in data) {
                    val itemData = item.value as HashMap<String, Any>
                    val itemDataMap = PublicSocialData(
                        name = itemData["name"].toString(),
                        value = itemData["value"].toString(),
                        unit = itemData["unit"].toString(),
                        updated = itemData["updated"].toString().toLong(),
                        type = itemData["type"].toString(),
                        logo = itemData["logo"].toString()
                    )
                    socialData.add(itemDataMap)
                }
                callback.onGetComplete(socialData)
            }
            else {
                callback.onGetFailure("User not found")
            }
        }.addOnFailureListener {
            callback.onGetFailure(it.message.toString())
        }
    }

    fun getCurrentUserProfile(callback: FirestoreGetCompleteAny){
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            callback.onGetFailure("User not logged in/connectivity issue")
            return
        }
        getUserSocialData(uid, callback)
    }

}