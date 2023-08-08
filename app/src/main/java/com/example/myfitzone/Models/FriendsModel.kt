package com.example.myfitzone.Models

import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.Friend
import com.example.myfitzone.DataModels.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class FriendsModel : ViewModel() {
    private val TAG = "FriendsModel"
    private val db = Firebase.firestore

    private fun getUserID(): String?{
        return Firebase.auth.currentUser?.uid
    }

    /**
     * Get results from search in form of a Friend object
     * @return callback.onGetComplete(Friend) if successful with friend data
     *@return callback.onGetComplete(false) if no user with username found
     * @return callback.onGetFailure(String) if failed with error message
     */
    fun getFriendSearchResult(username: String, callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        var otheruser: User? = null

        val docRefFriends = db.collection("friends")
            .document(userID)

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener {
                if(it.isEmpty){
                    //TODO: RETURN IF NO USER FOUND
                    callback.onGetComplete(false)
                    return@addOnSuccessListener
                }else{
                    it.documents[0].let { otheruser = it.toObject(User::class.java)!! }
                    docRefFriends.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                if(document.get("allFriends.${otheruser!!.UID}") != null) {
                                    val friend = document.get("allFriends.${otheruser!!.UID}") as Friend
                                    callback.onGetComplete(friend)
                                    return@addOnSuccessListener
                                }
                                else if (document.get("ownRequests.${otheruser!!.UID}") != null){
                                    val friend = document.get("ownRequests.${otheruser!!.UID}") as Friend
                                    callback.onGetComplete(friend)
                                    return@addOnSuccessListener
                                }
                                else if(document.get("friendRequests.${otheruser!!.UID}") != null){
                                    val friend = document.get("friendRequests.${otheruser!!.UID}") as Friend
                                    callback.onGetComplete(friend)
                                    return@addOnSuccessListener
                                }
                                else{
                                    val friend = Friend(
                                        UID = otheruser!!.UID,
                                        name = otheruser!!.name["first"].toString() + " " + otheruser!!.name["last"].toString(),
                                        username = otheruser!!.username,
                                        profilePic = "",
                                        dateAdded = Calendar.getInstance().timeInMillis,
                                        status = "none"
                                    )
                                    callback.onGetComplete(friend)
                                    return@addOnSuccessListener
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            callback.onGetFailure(e.toString())
                        }
                }
            }
            .addOnFailureListener {e->
                callback.onGetFailure(e.toString())
            }

    }

    fun sendFriendRequest(friendToAdd: Friend, callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        friendToAdd.dateAdded = Calendar.getInstance().timeInMillis
        val ownRequest = mapOf(
            "ownRequests.${friendToAdd.UID}" to friendToAdd
        )

        val task1 = db.collection("friends")
            .document(userID)
            .set(ownRequest, SetOptions.merge())
        val userDetailModel = UserDetailModel()
        val friendRequest = mapOf(
            "friendRequests.${userID}" to Friend(
                UID = userID,
                name = userDetailModel.getFirstName() + " " + userDetailModel.getLastName(),
                username = userDetailModel.getUsername(),
                profilePic = "",
                dateAdded = Calendar.getInstance().timeInMillis,
                status = "pending"
            )
        )
        val task2 = db.collection("friends")
            .document(friendToAdd.UID)
            .set(friendRequest, SetOptions.merge())

        Tasks.whenAllSuccess<Tasks>(Tasks.forResult(task1), Tasks.forResult(task2))
            .addOnSuccessListener {
                callback.onGetComplete(true)
            }
            .addOnFailureListener {e ->
                callback.onGetFailure(e.toString())
            }
    }
    fun acceptFriendRequest(friendToAccept: Friend, callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        friendToAccept.dateAdded = Calendar.getInstance().timeInMillis
        val ownFriends = mapOf(
            "allFriends.${friendToAccept.UID}" to friendToAccept
        )
        val task1 = db.collection("friends")
            .document(userID)
            .set(ownFriends, SetOptions.merge())

        val userDetailModel = UserDetailModel()
        val friendFriends = mapOf(
            "allFriends.${userID}" to Friend(
                UID = userID,
                name = userDetailModel.getFirstName() + " " + userDetailModel.getLastName(),
                username = userDetailModel.getUsername(),
                profilePic = "",
                dateAdded = Calendar.getInstance().timeInMillis,
                status = "accepted"
            )
        )
        val task2 = db.collection("friends")
            .document(friendToAccept.UID)
            .set(friendFriends, SetOptions.merge())

        Tasks.whenAllSuccess<Tasks>(Tasks.forResult(task1), Tasks.forResult(task2))
            .addOnSuccessListener {
                callback.onGetComplete(true)
            }
            .addOnFailureListener {e ->
                callback.onGetFailure(e.toString())
            }
    }

    fun declineFriendRequest(friendToDecline: Friend, callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        val task1 = db.collection("friends")
            .document(userID)
            .update("friendRequests.${friendToDecline.UID}", FieldValue.delete())

        val task2 = db.collection("friends")
            .document(friendToDecline.UID)
            .update("ownRequests.${userID}", FieldValue.delete())

        Tasks.whenAllSuccess<Tasks>(task1, task2)
            .addOnSuccessListener {
                callback.onGetComplete(true)
            }
            .addOnFailureListener {e ->
                callback.onGetFailure(e.toString())
            }

    }

    fun cancelFriendRequest(friendToCancel: Friend, callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        val task1 = db.collection("friends")
            .document(userID)
            .update("ownRequests.${friendToCancel.UID}", FieldValue.delete())

        val task2 = db.collection("friends")
            .document(friendToCancel.UID)
            .update("friendRequests.${userID}", FieldValue.delete())

        Tasks.whenAllSuccess<Tasks>(task1, task2)
            .addOnSuccessListener {
                callback.onGetComplete(true)
            }
            .addOnFailureListener {e ->
                callback.onGetFailure(e.toString())
            }
    }

    fun removeFriend(friendToRemove: Friend, callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        val task1 = db.collection("friends")
            .document(userID)
            .update("allFriends.${friendToRemove.UID}", FieldValue.delete())

        val task2 = db.collection("friends")
            .document(friendToRemove.UID)
            .update("allFriends.${userID}", FieldValue.delete())

        Tasks.whenAllSuccess<Tasks>(task1, task2)
            .addOnSuccessListener {
                callback.onGetComplete(true)
            }
            .addOnFailureListener {e ->
                callback.onGetFailure(e.toString())
            }


    }

    fun getMyFriends(callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        db.collection("friends")
            .document(userID)
            .get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val friendList = mutableMapOf<String, Friend>()
                    document.get("allFriends")?.let {allFriends->
                        val friends = allFriends as Map<String, Any>
                        for(friend in friends){
                            val friendMap = friend.value as Map<String, Any>
                            val friendToAdd = Friend(
                                UID = friend.key,
                                name = friendMap["name"] as String,
                                username = friendMap["username"] as String,
                                profilePic = friendMap["profilePic"] as String,
                                dateAdded = friendMap["dateAdded"] as Long,
                                status = friendMap["status"] as String
                            )
                            friendList[friendToAdd.UID] = friendToAdd
                        }
                    }
                    callback.onGetComplete(friendList)
                }

            }
    }

    fun getFriendRequests(callback: FirestoreGetCompleteAny){
        val userID = getUserID()?: return
        db.collection("friends")
            .document(userID)
            .get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val friendRequests = mutableMapOf<String, Friend>()
                    document.get("friendRequests")?.let {requests->
                        val friends = requests as Map<String, Any>
                        for(friend in friends){
                            val friendMap = friend.value as Map<String, Any>
                            val friendToAdd = Friend(
                                UID = friend.key,
                                name = friendMap["name"] as String,
                                username = friendMap["username"] as String,
                                profilePic = friendMap["profilePic"] as String,
                                dateAdded = friendMap["dateAdded"] as Long,
                                status = friendMap["status"] as String
                            )
                            friendRequests[friendToAdd.UID] = friendToAdd
                        }
                    }
                    callback.onGetComplete(friendRequests)
                }
            }
            .addOnFailureListener {
                callback.onGetFailure(it.toString())
            }
    }



}