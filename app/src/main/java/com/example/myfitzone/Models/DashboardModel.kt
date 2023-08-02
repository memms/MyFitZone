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
                list.sortByDescending { it.timestamp }
                realDashboardRecyclerData.cardValue = list[0].metricValue.toString()
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
            valueAddName to realDashboardRecyclerData
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