package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackHashMap
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.DataModels.DashboardTemplateData
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant

class DashboardModel: ViewModel() {
    //reload all data
    //reload certain data
    //add a dashboard
    //remove a dashboard
    private val TAG = "DashboardModel"
    private val db = Firebase.firestore
    private var dashboardAddType: String = ""
    private var valueAddName: String = ""
    private var dashboardItems = mutableListOf<DashboardRecyclerData>()
    private var liveData = MutableLiveData<List<DashboardRecyclerData>>()

    fun getDashLiveData(): MutableLiveData<List<DashboardRecyclerData>>{
        return liveData
    }

    fun getHomePageDashboard(callback: FirestoreGetCompleteCallbackArrayList){
        //get the dashboard from the database
        if(dashboardItems.size > 0){
            Log.d(TAG, "getHomePageDashboard: returned $dashboardItems")
            return
        }
        val userID = Firebase.auth.currentUser?.uid ?: return
        val colRef  = db.collection("users")
            .document(userID)
            .collection("dashboard")

        colRef.get(Source.CACHE)
            .addOnSuccessListener {
                val tempList = hashMapOf<String, DashboardRecyclerData>()
                for(document in it){
                    val temp = document.data as HashMap<String, Any>
                    Log.d(TAG, "getHomePageDashboard: $temp")
                    temp.keys.forEach { key ->
                        val temp2 = temp[key] as HashMap<String, Any>
                        Log.d(TAG, "getHomePageDashboard: $temp2")
                        val temp3 = DashboardRecyclerData(
                            cardName = temp2["cardName"] as String,
                            cardLogo = temp2["cardLogo"] as String,
                            cardValue = temp2["cardValue"] as String,
                            cardUnit = temp2["cardUnit"] as String,
                            cardUpdated = temp2["cardUpdated"] as Long,
                            recyclerPosition = temp2["recyclerPosition"].toString().toInt()
                        )
                        tempList[key] = temp3
                    }
                }
                Log.d(TAG, "getHomePageDashboard: $tempList")
                dashboardItems.addAll(tempList.values)
                dashboardItems.sortBy { it.recyclerPosition }
                liveData.value = dashboardItems
                callback.onGetComplete(arrayListOf("success"))
            }
            .addOnFailureListener { e ->
                callback.onGetFailure(e.toString())
            }

    }



    fun getDashboardTemplate(callback: FirestoreGetCompleteCallbackHashMap){
        if(dashboardAddType == ""|| valueAddName == ""){
            return
        }
        //get the dashboard template from the database
        db.collection("dashboardTypes")
            .document(dashboardAddType)
            .get()
            .addOnSuccessListener { document ->
                if(document != null){
                    val dashboardTemplate = hashMapOf<String, DashboardTemplateData>()
                    val temp = document.get(valueAddName) as HashMap<String, Any>
                    Log.d(TAG, "getDashboardTemplate: $temp")
                    dashboardTemplate[valueAddName] = DashboardTemplateData(
                        temp["unit"] as String,
                        temp["valueType"] as ArrayList<String>
                    )
                    Log.d(TAG, "getDashboardTemplate: $dashboardTemplate")
                    callback.onGetComplete(dashboardTemplate)
                }
            }
            .addOnFailureListener { e ->

            }
    }

    fun startAddExerciseMeasureDashBoard(tempDashboardRecyclerData: DashboardRecyclerData, callback: FirestoreGetCompleteCallbackArrayList){

    }
    private fun addExerciseMeasureDashBoard(realDashboardRecyclerData: DashboardRecyclerData, callback: FirestoreGetCompleteCallbackArrayList){
        val userID = Firebase.auth.currentUser?.uid ?: return
        val docData = mapOf(
            realDashboardRecyclerData.cardName to mapOf(
                "cardName" to realDashboardRecyclerData.cardName,
                "cardLogo" to realDashboardRecyclerData.cardLogo,
                "cardValue" to realDashboardRecyclerData.cardValue,
                "cardUnit" to realDashboardRecyclerData.cardUnit,
                "cardUpdated" to realDashboardRecyclerData.cardUpdated,
                "recyclerPosition" to realDashboardRecyclerData.recyclerPosition
            )
        )
        db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document("exercise")
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {

                dashboardItems.add(realDashboardRecyclerData)
                liveData.value = dashboardItems
                callback.onGetComplete(arrayListOf("success"))
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addExerciseMeasureDashBoard: $e")
                callback.onGetFailure(e.toString())
            }
    }

    fun startAddBodyMeasureDashBoard(tempDashboardRecyclerData: DashboardRecyclerData, callback: FirestoreGetCompleteCallbackArrayList){
        //add a body measure dashboard
        val realDashboardRecyclerData = tempDashboardRecyclerData

        val userBodyMeasureModel = UserBodyMeasureModel()
        userBodyMeasureModel.setSelectedName(valueAddName)
        userBodyMeasureModel.getSelectedBodyMeasureMetrics(object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val listL = result as MutableMap<Long, UserBodyMetrics>
                val list = mutableListOf<UserBodyMetrics>()
                list.addAll(listL.values)
                if(list.size == 0){
                    realDashboardRecyclerData.cardValue = "N/A"
                }
                else{
                    list.sortByDescending { it.timestamp }
                    realDashboardRecyclerData.cardValue = list[0].metricValue.toString()
                }
                realDashboardRecyclerData.cardUpdated = Instant.now().toEpochMilli()
                realDashboardRecyclerData.recyclerPosition = dashboardItems.size
                addBodyMeasureDashBoard(realDashboardRecyclerData, callback)
            }

            override fun onGetFailure(string: String) {
                callback.onGetFailure(string)
            }
        })
    }

    private fun addBodyMeasureDashBoard(realDashboardRecyclerData: DashboardRecyclerData, callback: FirestoreGetCompleteCallbackArrayList) {
        val userID = Firebase.auth.currentUser?.uid ?: return
        val docData = mapOf(
            realDashboardRecyclerData.cardName to realDashboardRecyclerData
        )

        val docRef = db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document("bodyMeasure")

        docRef.set(docData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                dashboardItems.add(realDashboardRecyclerData)
                liveData.value = dashboardItems
                callback.onGetComplete(arrayListOf("success"))
                Log.d(TAG, "addBodyMeasureDashBoard: $dashboardItems")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                callback.onGetFailure(e.toString())
            }

    }

    fun setDashboardAddType(dashboardAddType: String){
        this.dashboardAddType = dashboardAddType
    }

    fun setValueAddName(valueAddName: String){
        this.valueAddName = valueAddName
    }

    fun getDashboardAddType(): String{
        return dashboardAddType
    }

    fun getValueAddName(): String{
        return valueAddName
    }

    fun clearTempValues() {
        dashboardAddType = ""
        valueAddName = ""
    }


}