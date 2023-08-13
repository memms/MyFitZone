package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.Friend
import com.example.myfitzone.DataModels.PublicSocialData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList

class LeaderboardModel: ViewModel() {

    private val TAG = "LeaderboardModel"
    private val db = Firebase.firestore
    //Leaderboard name -> Hashmap < Score, PublicSocialData >
    private var leaderboardDataLocal = HashMap<String, HashMap<Double, ArrayList<PublicSocialData>>>()
    private var leaderboardData = MutableLiveData<HashMap<String, HashMap<Double, ArrayList<PublicSocialData>>>>()

    fun getLeaderboardLiveData(): LiveData<HashMap<String, HashMap<Double, ArrayList<PublicSocialData>>>> {
        return leaderboardData
    }

    private fun getUserID(): String?{
        return Firebase.auth.currentUser?.uid
    }

    fun getMyLeaderboardData(callback: FirestoreGetCompleteAny) {
        val uid = getUserID()
        if (uid == null) {
            callback.onGetFailure("User not logged in/connectivity issue")
            return
        }
        db.collection("leaderboards")
            .document(uid)
            .get()
            .addOnSuccessListener {document ->
                val result = document.data as HashMap<String, Any>
                val socialData = ArrayList<PublicSocialData>()
                result.forEach {
                    val itemData = it.value as HashMap<String, Any>
                    val publicSocialData = PublicSocialData(
                        name = itemData["name"].toString(),
                        value = itemData["value"].toString(),
                        fields = itemData["fields"].toString(),
                        logo = itemData["logo"].toString(),
                        unit = itemData["unit"].toString(),
                        updated = itemData["updated"] as Long,
                        type = itemData["type"].toString(),
                        uid = it.key
                    )
                    socialData.add(publicSocialData)
                }
                callback.onGetComplete(socialData)
            }
            .addOnFailureListener {
                callback.onGetFailure(it.message.toString())
            }
    }

    fun getLeaderboardData(
        friendUIDList: List<Friend>,
        attributeType: String,
        callback: FirestoreGetCompleteAny
    ) {
        val uidList = friendUIDList.map { it.uid }.toList()
        db.collection("leaderboards")
            .whereIn(FieldPath.documentId(), uidList)
            .get()
            .addOnSuccessListener { documents ->
                val list = mutableListOf<PublicSocialData>()
                for (document in documents) {
                    if (!document.exists()) continue
                    if (document.get(attributeType) == null) continue
                    val data = document.get(attributeType) as HashMap<String, Any>
                    val publicSocialData = PublicSocialData(
                        uid = document.id,
                        name = data["name"] as String,
                        value = data["value"] as String,
                        fields = data["fields"] as String,
                        logo = data["logo"] as String,
                        unit = data["unit"] as String,
                        updated = data["updated"] as Long,
                        type = data["type"] as String
                    )
                    list.add(publicSocialData)
                }
                sortLeaderboardData(friendUIDList, list, callback)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                callback.onGetFailure(exception.message.toString())
            }
    }

  
    //TODO: Thread this for sure
    private fun sortLeaderboardData(friendUIDList: List<Friend>, list: MutableList<PublicSocialData>, callback: FirestoreGetCompleteAny) {
        //distance, weight, calories: add first
        //reps, sets, duration, incline, speed, intensity, resistance: add then multiply to score
        //rest: add then subtract from score
        //Theoretically this should work, but may need to be tweaked

        leaderboardDataLocal[list[0].name] = hashMapOf()
        var score: Double = 1.00
        val fields = list[0].fields.split('\n').toList()
        val sortedFieldMap = hashMapOf<Int, Int>()
        Log.d(TAG, "sortLeaderboardData: fields: $fields")
        for(i in fields.indices){
            when (fields[i]){
                FieldUnits.DISTANCE -> {
                    sortedFieldMap[i] = 1
                }
                FieldUnits.WEIGHT -> {
                    sortedFieldMap[i] = 1
                }
                FieldUnits.CALORIES -> {
                    sortedFieldMap[i] = 1
                }
                FieldUnits.REPS -> {
                    sortedFieldMap[i] = 2
                }
                FieldUnits.DURATION -> {
                    sortedFieldMap[i] = 2
                }
                FieldUnits.INCLINE -> {
                    sortedFieldMap[i] = 2
                }
                FieldUnits.SPEED -> {
                    sortedFieldMap[i] = 2
                }
                FieldUnits.INTENSITY -> {
                    sortedFieldMap[i] = 2
                }
                FieldUnits.RESISTANCE -> {
                    sortedFieldMap[i] = 2
                }
                FieldUnits.REST -> {
                    sortedFieldMap[i] = 3
                }
                FieldUnits.SETS -> {
                    sortedFieldMap[i] = 4
                }
            }
        }
        Log.d(TAG, "sortLeaderboardData: sortedFieldMap: NOT SORTED $sortedFieldMap")
        //sort map by value
        sortedFieldMap.toList().sortedBy { (_, value) -> value }.toMap()
        Log.d(TAG, "sortLeaderboardData: sortedFieldMap: SORTED BY Value $sortedFieldMap")
        list.forEach {
            score = 1.00
            val values = it.value.split('\n').toList()
            var additionScore = 0.00
            var multiplicationScore = 0.00
            var subtractionScore = 0.00
            var sets = 1
            sortedFieldMap.forEach { (key, value) ->
                Log.d(TAG, "sortLeaderboardData: key: $key, value: $value")
                when (value) {
                    1 -> {
                        additionScore += values[key].toDouble()
                    }
                    2 -> {
                        multiplicationScore += values[key].toDouble()
                    }
                    3 -> {
                        subtractionScore += values[key].toDouble()
                    }
                    4 -> {
                        sets = values[key].toInt()
                    }
                }
                Log.d(TAG, "sortLeaderboardData: additionScore: $additionScore, multiplicationScore: $multiplicationScore, subtractionScore: $subtractionScore")
            }
            score = (additionScore*(if(multiplicationScore>0.00) multiplicationScore else 1.00) - subtractionScore) * sets
            Log.d(TAG, "sortLeaderboardData: score: $score")
            if(leaderboardDataLocal[list[0].name]?.containsKey(score) == true){
                leaderboardDataLocal[list[0].name]?.get(score)?.add(it)
            } else {
                leaderboardDataLocal[list[0].name]?.put(score, arrayListOf(it))
            }
            Log.d(TAG, "sortLeaderboardData: leaderboardMap: ${leaderboardDataLocal[list[0].name]?.get(score)}")
        }
        //get the UIDs present in friendsUIDlist but not present in leaderboardMap's PublicSocialData.uid
        Log.d(TAG, "sortLeaderboardData: friendUIDList: $friendUIDList , leaderboardMap: ${leaderboardDataLocal[list[0].name]}")
        val missingUIDList = friendUIDList.filter { friend -> leaderboardDataLocal[list[0].name]?.values?.flatten()?.map { it.uid }?.contains(friend.uid) == false }
        Log.d(TAG, "sortLeaderboardData: missingUIDList: $missingUIDList")
        //add missing UIDs to leaderboardMap
        missingUIDList.forEach {
            leaderboardDataLocal[list[0].name]?.put(0.00, arrayListOf(PublicSocialData(uid = it.uid, name = it.name, value = "0", fields = "", logo = "", unit = "", updated = 0, type = "")))
        }
        Log.d(TAG, "sortLeaderboardData: leaderboardMap: missingUIDSAdded ${leaderboardDataLocal[list[0].name]}")
        //sort leaderboardMap by score
        leaderboardDataLocal[list[0].name]?.toList()?.sortedBy { (key, _) -> key }?.toMap()
        Log.d(TAG, "sortLeaderboardData: leaderboardMap: sorted by score ${leaderboardDataLocal[list[0].name]}")
        //add sorted leaderboardMap to leaderboardData[attributeType]

        leaderboardData.value = leaderboardDataLocal
        Log.d(TAG, "sortLeaderboardData: leaderboardData: ${leaderboardData.value}")
 
    }


}