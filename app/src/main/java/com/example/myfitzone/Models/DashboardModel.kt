package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackHashMap
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.DataModels.DashboardTemplateData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.security.auth.callback.Callback

class DashboardModel: ViewModel() {
    //reload all data
    //reload certain data
    //add a dashboard
    //remove a dashboard
    private val TAG = "DashboardModel"
    private val db = Firebase.firestore
    private var dashboardAddType: String = ""
    private var valueAddName: String = ""
    private var dashboardItems = arrayListOf<DashboardRecyclerData>()

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

    fun addBodyMeasureDashBoard(){
        //add a body measure dashboard
        
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