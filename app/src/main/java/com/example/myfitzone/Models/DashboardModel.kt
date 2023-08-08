package com.example.myfitzone.Models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackHashMap
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.DataModels.DashboardTemplateData
import com.example.myfitzone.DataModels.Friend
import com.example.myfitzone.DataModels.UserBodyMetrics
import com.example.myfitzone.DataModels.UserExercise
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar

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
    private var tempExerciseGroup = ""
    private var notificationsOpened = false
    private var friendRequestList = MutableLiveData<ArrayList<Friend>>()

    init {
        friendRequestList.value = arrayListOf()
        friendRequestListener()
    }

    fun setNotificationsOpened(value: Boolean){
        notificationsOpened = value
    }

    fun getNotificationsOpened(): Boolean{
        return notificationsOpened
    }

    fun getFriendRequestLiveList(): MutableLiveData<ArrayList<Friend>>{
        return friendRequestList
    }

    private fun friendRequestListener(){
        val userId = Firebase.auth.currentUser?.uid
        val docRef = db.collection("friends").document(userId.toString())

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                snapshot.data?.let {
                    try {
                        val friendRequests = it["friendRequests"] as HashMap<String, Any>
                        val arrayList = arrayListOf<Friend>()
                        friendRequests.let {map ->
                            map.forEach {friendData ->
                                val friend = friendData.value as HashMap<String, Any>
                                val friendData = Friend(
                                    name = friend["name"] as String,
                                    username = friend["username"] as String,
                                    uid = friend["uid"] as String,
                                    profilePic = friend["profilePic"] as String,
                                    dateAdded = friend["dateAdded"] as Long,
                                    status = friend["status"] as String
                                )
                                arrayList.add(friendData)
                            }
                            friendRequestList.value = arrayList
                            notificationsOpened = false
                        }

                    }
                    catch (e: Exception){
                        Log.d(TAG, "friendRequestListener: $e")
                    }
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }


    fun setTempExerciseGroup(group: String){
        tempExerciseGroup = group
    }
    fun getTempExerciseGroup(): String{
        return tempExerciseGroup
    }

    fun getDashLiveData(): MutableLiveData<List<DashboardRecyclerData>>{
        return liveData
    }

    fun getSensorTemplates(callback: FirestoreGetCompleteAny){
        val hashMap = hashMapOf<String, DashboardTemplateData>()
        db.collection("dashboardTypes")
            .document("sensor")
            .get()
            .addOnSuccessListener {document ->
                document.data?.let {
                    it.forEach {
                        val template = it.value as HashMap<String, Any>
                        val temp = DashboardTemplateData(
                            template["unit"] as String,
                            template["valueType"] as ArrayList<String>
                        )
                        hashMap[it.key] = temp
                    }
                }
                callback.onGetComplete(hashMap)
            }
            .addOnFailureListener{e->
                callback.onGetFailure(e.toString())
            }

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
                Log.d(TAG, "getHomePageDashboard: SIZE ${it.size()}")
                for(document in it){
                    if (!document.exists()){
                        continue
                    }
                    val temp = document.data as HashMap<String, Any>
                    Log.d(TAG, "getHomePageDashboard: document.data/temp $temp")
                    temp.keys.forEach { key ->
                        val temp2 = temp[key] as HashMap<String, Any>
                        Log.d(TAG, "getHomePageDashboard: temp2 = temp[key] $temp2")
                        val temp3 = DashboardRecyclerData(
                            cardName = temp2["cardName"] as String,
                            cardLogo = temp2["cardLogo"] as String,
                            cardValue = temp2["cardValue"] as String,
                            cardUnit = temp2["cardUnit"] as String,
                            cardUpdated = temp2["cardUpdated"] as Long,
                            recyclerPosition = temp2["recyclerPosition"].toString().toInt(),
                            cardType = temp2["cardType"] as String
                        )
                        tempList[key] = temp3

                    }
                }
                dashboardItems.addAll(tempList.values)
                Log.d(TAG, "getHomePageDashboard: tempList $tempList")
                dashboardItems.sortBy { it.recyclerPosition }
                Log.d(TAG, "getHomePageDashboard: dashboardItems $dashboardItems")
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

    fun getExercises(timeFrame: String, callback: FirestoreGetCompleteAny){
        //get the exercises from the database
        val userID = Firebase.auth.currentUser?.uid ?: return
        if(timeFrame == ""){
            return
        }
        if(timeFrame == "30d"){
            val simpleDateFormat = SimpleDateFormat("yyyy-MM")
            val range = arrayListOf(Instant.now().toEpochMilli(), Instant.now().toEpochMilli()-2592000000)
            var formated = simpleDateFormat.format(range[0])
            val userExerciseList = arrayListOf<UserExercise>()
            repeat(2){
                getExerciseByDocument(formated, object: FirestoreGetCompleteAny{
                    override fun onGetComplete(data: Any) {
                        Log.d(TAG, "onGetComplete: $formated")
                        val temp = data as ArrayList<UserExercise>
                        temp.forEach { tempData ->
                            if(!userExerciseList.contains(tempData)){
                                userExerciseList.add(tempData)
                            }
                        }
                        callback.onGetComplete(userExerciseList)
                        Log.d(TAG, "onGetComplete: $userExerciseList")
                    }

                    override fun onGetFailure(exception: String) {
                    }
                })
                val cal = Calendar.getInstance()
                cal.timeInMillis = range[0]
                cal.set(Calendar.DAY_OF_MONTH, -1)
                formated = simpleDateFormat.format(cal.timeInMillis)
            }
        }
    }

    private fun getExerciseByDocument(name:String, callback: FirestoreGetCompleteAny){
        //get the exercise by document from the database
        val userID = Firebase.auth.currentUser?.uid ?: return
        if(name == ""){
            return
        }
        Log.d(TAG, "getExerciseByDocument: $name")
        db.collection("users")
            .document(userID)
            .collection("userExerciseList")
            .document(name)
            .get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val userExerciseList = arrayListOf<UserExercise>()
                    document.data?.let {
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
                    callback.onGetComplete(userExerciseList)
                }
            }
            .addOnFailureListener { e ->
                callback.onGetFailure(e.toString())
            }
    }

    fun startAddExerciseMeasureDashBoard(tempDashboardRecyclerData: DashboardRecyclerData, callback: FirestoreGetCompleteCallbackArrayList){
        getExercises("30d" , object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                Log.d(TAG, "onGetComplete: startAddExerciseMeasureDashBoard $result")
                val list = result as ArrayList<UserExercise>
                list.removeIf { it.name != valueAddName }
                if (list.size == 0) {
                    tempDashboardRecyclerData.cardValue = "N/A"
                }
                //"Last", "High", "Low", "1RM", "Avg"
                else {
                    val cardValueL = with(tempDashboardRecyclerData.cardName) {
                        when {
                            contains("Last") -> {
                                list.sortByDescending { it.timeAdded }
                                val stringBuilder = StringBuilder()
                                for (field in list[0].fieldmap) {
                                    //average it out
                                    if (field.key != "sets") {
                                        stringBuilder.append(findAverage(field.value as List<*>))
                                        stringBuilder.append("\n")
                                    } else {
                                        stringBuilder.append(field.value)
                                        stringBuilder.append("\n")
                                    }
                                }
                                tempDashboardRecyclerData.cardValue =
                                    stringBuilder.toString().trim('\n')
                                Log.d(TAG, "startAddExerciseMeasureDashBoard: $stringBuilder")
                            }

                            contains("High") -> ""
                            contains("Low") -> ""
                            contains("1RM") -> ""
                            contains("Avg") -> ""
                            else -> ""
                        }
                    }
                }
                tempDashboardRecyclerData.cardUpdated = Instant.now().toEpochMilli()
                tempDashboardRecyclerData.recyclerPosition = dashboardItems.size
                Log.d(TAG, "startAddExerciseMeasureDashBoard: $tempDashboardRecyclerData")
                addExerciseMeasureDashBoard(tempDashboardRecyclerData, callback)
            }

            override fun onGetFailure(string: String) {
                callback.onGetFailure(string)
            }
        })
    }

    private fun findAverage(list: List<*>): String{
        Log.d(TAG, "findAverage: $list")
        var sum = 0.0
        for(item in list){
            sum += item.toString().toDouble()
        }
        val average = String.format("%.1f", sum/list.size)
        return average
    }

    private fun addExerciseMeasureDashBoard(realDashboardRecyclerData: DashboardRecyclerData, callback: FirestoreGetCompleteCallbackArrayList){
        val userID = Firebase.auth.currentUser?.uid ?: return
        val docData = mapOf(
            realDashboardRecyclerData.cardName to realDashboardRecyclerData
        )
        db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document("exercise")
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {
                dashboardItems.removeIf { it.cardName == realDashboardRecyclerData.cardName }
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
        valueAddName = getBodyMeasureName(tempDashboardRecyclerData.cardName)
        userBodyMeasureModel.setSelectedName(valueAddName)
        userBodyMeasureModel.getSelectedBodyMeasureMetrics(object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val listL = result as MutableMap<Long, UserBodyMetrics>
                val list = arrayListOf<UserBodyMetrics>()
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

    private fun getBodyMeasureName(string: String):String{
        return with(string){
            when {
                contains("Weight") -> "Weight"
                contains("Body Fat") -> "Body Fat %"
                contains("Calories") -> "Calories"
                contains("Resting Heart Rate") -> "Resting Heart Rate"
                contains("Sleep") -> "Sleep"
                contains("Neck") -> "Neck"
                contains("Shoulders") -> "Shoulders"
                contains("Chest") -> "Chest"
                contains("Right Arm") -> "Right Arm"
                contains("Left Arm") -> "Left Arm"
                contains("Right Forearm") -> "Right Forearm"
                contains("Left Forearm") -> "Left Forearm"
                contains("Upper Abs") -> "Upper Abs"
                contains("Waist") -> "Waist"
                contains("Hips") -> "Hips"
                contains("Right Thigh") -> "Right Thigh"
                contains("Left Thigh") -> "Left Thigh"
                contains("Right Calf") -> "Right Calf"
                contains("Left Calf") -> "Left Calf"
                else -> ""
            }
        }
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
                dashboardItems.removeIf { it.cardName == realDashboardRecyclerData.cardName }
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

    fun getValueAddName(): String{
        return valueAddName
    }

    fun clearTempValues() {
        dashboardAddType = ""
        valueAddName = ""
    }

    fun getHomePageDashboardFromServer(callback: FirestoreGetCompleteCallbackArrayList) {
        Log.d(TAG, "getHomePageDashboardFromServer: CALLED")
        dashboardItems.clear()
        val userID = Firebase.auth.currentUser?.uid ?: return
        val colRef  = db.collection("users")
            .document(userID)
            .collection("dashboard")

        colRef.get(Source.SERVER)
            .addOnSuccessListener { documents ->
                val tempList = hashMapOf<String, DashboardRecyclerData>()
                for(document in documents){
                    val temp = document.data as HashMap<String, Any>
                    Log.d(TAG, "getHomePageDashboard: document.data/temp $temp")
                    temp.keys.forEach { key ->
                        val temp2 = temp[key] as HashMap<String, Any>
                        Log.d(TAG, "getHomePageDashboard: temp2 = temp[key] $temp2")
                        val temp3 = DashboardRecyclerData(
                            cardName = temp2["cardName"] as String,
                            cardLogo = temp2["cardLogo"] as String,
                            cardValue = temp2["cardValue"] as String,
                            cardUnit = temp2["cardUnit"] as String,
                            cardUpdated = temp2["cardUpdated"] as Long,
                            recyclerPosition = temp2["recyclerPosition"].toString().toInt(),
                            cardType = temp2["cardType"] as String
                        )
                        tempList[key] = temp3
                    }
                }
                dashboardItems.clear()
                dashboardItems.addAll(tempList.values)
                Log.d(TAG, "getHomePageDashboard: tempList $tempList")
                dashboardItems.sortBy { it.recyclerPosition }
                Log.d(TAG, "getHomePageDashboard: dashboardItems $dashboardItems")
                liveData.value = dashboardItems
                callback.onGetComplete(arrayListOf("success"))
            }
            .addOnFailureListener { e ->
                callback.onGetFailure(e.toString())
            }
    }

    fun updateDashboardValuesSensors(callback: FirestoreGetCompleteCallbackArrayList){
        dashboardItems.clear()
        val userID = Firebase.auth.currentUser?.uid ?: return
        val colRef  = db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document("sensor")

        colRef.get(Source.SERVER)
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val temp = document.data as HashMap<String, Any>
                    Log.d(TAG, "updateDashboardValuesBodyMeasure: document.data/temp $temp")
                    temp.keys.forEach { key ->
                        val temp2 = temp[key] as HashMap<String, Any>
                        Log.d(TAG, "updateDashboardValuesBodyMeasure: temp2 = temp[key] $temp2")
                        val temp3 = DashboardRecyclerData(
                            cardName = temp2["cardName"] as String,
                            cardLogo = temp2["cardLogo"] as String,
                            cardValue = temp2["cardValue"] as String,
                            cardUnit = temp2["cardUnit"] as String,
                            cardUpdated = temp2["cardUpdated"] as Long,
                            recyclerPosition = temp2["recyclerPosition"].toString().toInt(),
                            cardType = temp2["cardType"] as String
                        )
                        setDashboardAddType("bodyMeasure")
                        startAddSensorDashBoard(temp3, object : FirestoreGetCompleteCallbackArrayList{
                            override fun onGetComplete(string: ArrayList<String>) {
                                Log.d(TAG, "updateDashboardValuesBodyMeasure: $string")
                                callback.onGetComplete(string)
                            }

                            override fun onGetFailure(string: String) {
                                Log.d(TAG, "onGetFailure: $string")
                            }
                        })
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "getHomePageDashboardFromServer: $exception")

            }
    }

    private fun getBodyMeasures(timeRange: String, callback: FirestoreGetCompleteAny){
        val userID = Firebase.auth.currentUser?.uid ?: return
        val simpleDateFormat = SimpleDateFormat("yyyy-MM")
        val range = arrayListOf(Instant.now().toEpochMilli(), Instant.now().toEpochMilli()-2592000000)
        var formated = simpleDateFormat.format(range[0])

        var list = mutableMapOf<Long,UserBodyMetrics>()
        val docRef = db.collection("users")
            .document(userID)
            .collection("userBodyMeasurements")

        val task1 = docRef.document(formated)
            .get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    document.data!!.forEach {
                        val userBodyMetrics = it.value as HashMap<String, Any>
                        val userBodyMetricsObject = UserBodyMetrics(
                            userBodyMetrics["timestamp"] as Long,
                            userBodyMetrics["metricType"] as String,
                            userBodyMetrics["metricName"] as String,
                            userBodyMetrics["metricValue"] as Double,
                            userBodyMetrics["dateLastModified"] as Long
                        )
                        list[userBodyMetricsObject.timestamp] = userBodyMetricsObject
                    }
                }
                else{
                    Log.d(TAG, "getBodyMeasures: No data 1")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                callback.onGetFailure(exception.toString())
            }

            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = range[0]
            cal.set(Calendar.DAY_OF_MONTH, -1)
            formated = simpleDateFormat.format(cal.timeInMillis)
            val task2 = docRef.document(formated)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.data!!.forEach {
                            val userBodyMetrics = it.value as HashMap<String, Any>
                            val userBodyMetricsObject = UserBodyMetrics(
                                userBodyMetrics["timestamp"] as Long,
                                userBodyMetrics["metricType"] as String,
                                userBodyMetrics["metricName"] as String,
                                userBodyMetrics["metricValue"] as Double,
                                userBodyMetrics["dateLastModified"] as Long
                            )
                            if (userBodyMetricsObject.timestamp > range[1]
                                && userBodyMetricsObject.timestamp < range[0]
                                && !list.containsKey(userBodyMetricsObject.timestamp)
                            ) {
                                list[userBodyMetricsObject.timestamp] = userBodyMetricsObject
                            }
                        }
                    } else {
                        Log.d(TAG, "getBodyMeasures: No data 2")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                    callback.onGetFailure(exception.toString())
                }


            Tasks.whenAllComplete(task1, task2).addOnSuccessListener {
                callback.onGetComplete(list)
            }


    }

    fun updateDashboardValuesBodyMeasure(callback: FirestoreGetCompleteCallbackArrayList) {
        dashboardItems.clear()
        val userID = Firebase.auth.currentUser?.uid ?: return
        val colRef  = db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document("bodyMeasure")

        colRef.get(Source.SERVER)
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val temp = document.data as HashMap<String, Any>
                    Log.d(TAG, "updateDashboardValuesBodyMeasure: document.data/temp $temp")
                    val listUpdatedDashboards = arrayListOf<DashboardRecyclerData>()
                    getBodyMeasures("30d", callback = object : FirestoreGetCompleteAny{
                        override fun onGetComplete(result: Any) {
                            val mainList = result as MutableMap<Long, UserBodyMetrics>
                            Log.d(TAG, "updateDashboardValuesBodyMeasure: mainList $mainList")
                            temp.keys.forEach {key ->
                                val temp2 = temp[key] as HashMap<String, Any>
                                Log.d(TAG, "updateDashboardValuesBodyMeasure: temp2 = temp[key] $temp2")
                                val tempDashboardRecyclerData = DashboardRecyclerData(
                                    cardName = temp2["cardName"] as String,
                                    cardLogo = temp2["cardLogo"] as String,
                                    cardValue = temp2["cardValue"] as String,
                                    cardUnit = temp2["cardUnit"] as String,
                                    cardUpdated = temp2["cardUpdated"] as Long,
                                    recyclerPosition = temp2["recyclerPosition"].toString().toInt(),
                                    cardType = temp2["cardType"] as String
                                )
                                val measureName = getBodyMeasureName(tempDashboardRecyclerData.cardName)
                                var list = mainList.filter { it.value.metricName == measureName }.values.toMutableList()
                                Log.d(TAG, "updateDashboardValuesBodyMeasure: list for ${key} $list")
                                if(list.isEmpty()){
                                    tempDashboardRecyclerData.cardValue = "N/A"
                                }
                                else{   //get Last value
                                    list.sortByDescending { it.timestamp }
                                    Log.d(TAG, "updateDashboardValuesBodyMeasure: list[0] descending ${list[0]}")
                                    tempDashboardRecyclerData.cardValue = list[0].metricValue.toString()
                                }
                                tempDashboardRecyclerData.cardUpdated = Calendar.getInstance().timeInMillis
                                listUpdatedDashboards.add(tempDashboardRecyclerData)
                            }
                            val docData = mutableMapOf<String, Any>()
                            listUpdatedDashboards.forEach {
                                docData[it.cardName] = mapOf(
                                    "cardName" to it.cardName,
                                    "cardLogo" to it.cardLogo,
                                    "cardValue" to it.cardValue,
                                    "cardUnit" to it.cardUnit,
                                    "cardUpdated" to it.cardUpdated,
                                    "recyclerPosition" to it.recyclerPosition,
                                    "cardType" to it.cardType
                                )
                            }
                            colRef.set(docData, SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d(TAG, "updateDashboardValuesExercise: success")
                                    callback.onGetComplete(arrayListOf("success"))
                                }.addOnFailureListener { e ->
                                    Log.d(TAG, "updateDashboardValuesExercise: failure")
                                    callback.onGetFailure(e.toString())
                                }
                        }

                        override fun onGetFailure(string: String) {
                            Log.d(TAG, "updateDashboardValuesExercise: failure")
                            callback.onGetFailure(string)
                        }

                    })

                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "getHomePageDashboardFromServer: $exception")
                callback.onGetFailure(exception.toString())
            }
    }

    fun updateDashboardValuesExercise(callback: FirestoreGetCompleteCallbackArrayList) {
        dashboardItems.clear()

        val userID = Firebase.auth.currentUser?.uid ?: return
        val colRef  = db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document("exercise")

        colRef.get(Source.SERVER)
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val temp = document.data as HashMap<String, Any>
                    val listUpdatedDashboards = arrayListOf<DashboardRecyclerData>()
                    getExercises("30d", callback = object : FirestoreGetCompleteAny{
                        override fun onGetComplete(result: Any) {
                            val mainList = result as ArrayList<UserExercise>
                            temp.keys.forEach{key ->
                                val temp2 = temp[key] as HashMap<String, Any>
                                val tempDashboardRecyclerData = DashboardRecyclerData(
                                    cardName = temp2["cardName"] as String,
                                    cardLogo = temp2["cardLogo"] as String,
                                    cardValue = temp2["cardValue"] as String,
                                    cardUnit = temp2["cardUnit"] as String,
                                    cardUpdated = temp2["cardUpdated"] as Long,
                                    recyclerPosition = temp2["recyclerPosition"].toString().toInt(),
                                    cardType = temp2["cardType"] as String
                                )
                                val exerciseName = tempDashboardRecyclerData.cardName.split('(')[0].trim()
                                val list = mainList.filter { it.name == exerciseName }
                                if(list.isEmpty()){
                                    tempDashboardRecyclerData.cardValue = "N/A"
                                }
                                else {
                                    with(tempDashboardRecyclerData.cardName) {
                                        when {
                                            contains("Last") -> {
                                                list.sortedByDescending { it.timeAdded }
                                                val stringBuilder = StringBuilder()
                                                for (field in list[0].fieldmap) {
                                                    //average it out
                                                    if (field.key != "sets") {
                                                        stringBuilder.append(findAverage(field.value as List<*>))
                                                        stringBuilder.append("\n")
                                                    } else {
                                                        stringBuilder.append(field.value)
                                                        stringBuilder.append("\n")
                                                    }
                                                }
                                                tempDashboardRecyclerData.cardValue =
                                                    stringBuilder.toString().trim('\n')
                                                Log.d(TAG, "startAddExerciseMeasureDashBoard: $stringBuilder")
                                            }
                                            contains("High") -> ""
                                            contains("Low") -> ""
                                            contains("1RM") -> ""
                                            contains("Avg") -> ""
                                            else -> ""
                                        }
                                    }
                                }
                                tempDashboardRecyclerData.cardUpdated = Calendar.getInstance().timeInMillis
                                Log.d(TAG, "startAddExerciseMeasureDashBoard: $tempDashboardRecyclerData")
//                                addExerciseMeasureDashBoard(tempDashboardRecyclerData, callback)
                                listUpdatedDashboards.add(tempDashboardRecyclerData)
                            }
                            //add all the updated dashboards at once for 1 read
                            val docData = mutableMapOf<String, Any>()
                            listUpdatedDashboards.forEach {
                                docData[it.cardName] = mapOf(
                                    "cardName" to it.cardName,
                                    "cardLogo" to it.cardLogo,
                                    "cardValue" to it.cardValue,
                                    "cardUnit" to it.cardUnit,
                                    "cardUpdated" to it.cardUpdated,
                                    "recyclerPosition" to it.recyclerPosition,
                                    "cardType" to it.cardType
                                )
                            }
                            colRef.set(docData, SetOptions.merge())
                                .addOnSuccessListener {
                                Log.d(TAG, "updateDashboardValuesExercise: success")
                                callback.onGetComplete(arrayListOf("success"))
                            }.addOnFailureListener {e ->
                                Log.d(TAG, "updateDashboardValuesExercise: failure")
                                callback.onGetFailure(e.toString())
                            }
                        }
                        override fun onGetFailure(string: String) {
                            Log.d(TAG, "updateDashboardValuesExercise: failure")
                            callback.onGetFailure(string)
                        }

                    })
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "getHomePageDashboardFromServer: $exception")
                callback.onGetFailure(exception.toString())
            }
    }

    fun startAddSensorDashBoard(
        itemAdd: DashboardRecyclerData,
        callback: FirestoreGetCompleteCallbackArrayList
    ) {
        Log.d(TAG, "startAddSensorDashBoard: itemAdd $itemAdd")
        val sensorName = itemAdd.cardName.split('(')[0].trim()
        val selectedType = itemAdd.cardName.split('(')[1].split(')')[0].trim()
        Log.d(TAG, "startAddSensorDashBoard: sensorName $sensorName")
        Log.d(TAG, "startAddSensorDashBoard: selectedType $selectedType")
        when(selectedType){
            "Today" -> {
                Log.d(TAG, "startAddSensorDashBoard: Today")
                getSensorData("1d", sensorName, object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        try {
                            itemAdd.cardValue = result.toString()
                        }
                        catch (e: Exception){
                            itemAdd.cardValue = "0"
                        }
                        Log.d(TAG, "onGetComplete: result $result")
                        itemAdd.cardUpdated = Calendar.getInstance().timeInMillis
                        itemAdd.recyclerPosition = dashboardItems.size
                        addSensorDashboard(itemAdd, object : FirestoreGetCompleteAny{
                            override fun onGetComplete(result: Any) {
                                callback.onGetComplete(arrayListOf("success"))
                            }

                            override fun onGetFailure(string: String) {
                                callback.onGetFailure(string)
                                Log.d(TAG, "onGetFailure: $string")
                            }

                        })
                    }

                    override fun onGetFailure(string: String) {
                        callback.onGetFailure(string)
                    }

                })
            }
        }
    }

    private fun addSensorDashboard(
        realDashboardRecyclerData: DashboardRecyclerData,
        callback: FirestoreGetCompleteAny
    ) {
        val userID = Firebase.auth.currentUser?.uid ?: return
        val docData = mapOf(
            realDashboardRecyclerData.cardName to realDashboardRecyclerData
        )

        val docRef = db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document("sensor")

        docRef.set(docData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                dashboardItems.removeIf { it.cardName == realDashboardRecyclerData.cardName }
                dashboardItems.add(realDashboardRecyclerData)
                liveData.value = dashboardItems
                callback.onGetComplete(arrayListOf("success"))
                Log.d(TAG, "addSensorDashboard: $dashboardItems")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                callback.onGetFailure(e.toString())
            }
    }

    private fun getYear() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy")
        return sdf.format(cal.timeInMillis)
    }

    private fun getMonth() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("MM")
        return sdf.format(cal.timeInMillis)
    }

    private fun getSensorData(range: String, sensorName: String, callback: FirestoreGetCompleteAny){
        val userID = Firebase.auth.currentUser?.uid ?: return
        var ref  = db.collection("users")
            .document(userID)
            .collection("sensorsData")
            .document(sensorName)
            .collection(getYear())
            .document(getMonth())
        Log.d(TAG, "getSensorData: ${getYear()} ${getMonth()}")

        ref.get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "getSensorData: $document")
                if(document.exists()){
                    Log.d(TAG, "getSensorData: ${document.data}")
                    if(range == "1d"){
                        Log.d(TAG, "getSensorData: ${LocalDate.now()}.currentStepCount")
                        document.getLong("${LocalDate.now()}.currentStepCount").let { currentStepCount ->
                            Log.d(TAG, "getSensorData: currentStepCount $currentStepCount")
                            if (currentStepCount != null) {
                                callback.onGetComplete(currentStepCount)
                            }
                            else{
                                callback.onGetComplete("0")
                            }
                            return@addOnSuccessListener
                        }
                    }
                    else {
                        document.data?.let {
                            val hashMap = it as HashMap<String, Any>
                            it.forEach { map ->
                                val temp = map.value as HashMap<String, Any>
                                hashMap[map.key] = temp
                            }
                            callback.onGetComplete(hashMap)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "getSensorData: $exception")
                callback.onGetFailure(exception.toString())
            }
    }

    fun deleteDashboard(itemDelete: DashboardRecyclerData, callback: FirestoreGetCompleteAny){
        val userID = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userID)
            .collection("dashboard")
            .document(itemDelete.cardType)
            .update(itemDelete.cardName, FieldValue.delete())
            .addOnSuccessListener {
                Log.d(TAG, "deleteDashboard: success")
                callback.onGetComplete(arrayListOf("success"))
                dashboardItems.removeIf { it.cardName == itemDelete.cardName }
                liveData.value = dashboardItems
            }
            .addOnFailureListener {
                Log.d(TAG, "deleteDashboard: failure")
                callback.onGetFailure(it.toString())
            }


    }


}