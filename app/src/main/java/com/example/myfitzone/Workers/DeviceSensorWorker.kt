package com.example.myfitzone.Workers

import android.app.NotificationChannel
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.system.SystemCleaner
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds


class DeviceSensorWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context,
    workerParams), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private var totalStepCount: Int = 0
    private var oldStepCount: Int = 0
    private var currentStepCount: Int = 0
    private var lastsensorStepCount: Int = 0
    private var timestamp : Timestamp? = null
    private val TAG = "DeviceSensorWorker"
    private lateinit var db : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var UID: String

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: ")
        try {
            setForeground(createForegroundInfo("MyFitZone", "Tracking your steps"))
            sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            Log.d(TAG, "doWork: registerListener")
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)

            delay(1000)
            Log.d(TAG, "doWork: unregisterListener")
            sensorManager.unregisterListener(this@DeviceSensorWorker)
//            val stepCount = getStepCount()
            Log.d(TAG, "doWork: $totalStepCount")
            db = Firebase.firestore
            firebaseAuth = FirebaseAuth.getInstance()
            UID = firebaseAuth.currentUser?.uid.toString()
            loadStepCount()
            delay(2000)
            getStepCount()
            return Result.success()
        }
        catch (e: Exception){
            return Result.failure()
        }
    }

    private fun createForegroundInfo(s: String, s1: String): ForegroundInfo {
        val channelId =
            createNotificationChannel("MyFitZone", "My Fit Zone")
        return ForegroundInfo(1, android.app.Notification.Builder(applicationContext, channelId)
            .setContentTitle(s)
            .setContentText(s1)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build())
    }

    private fun createNotificationChannel(s: String, s1: String): String {
        val chan = NotificationChannel(s, s1, android.app.NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = android.graphics.Color.BLUE
        chan.lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        val service = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        service.createNotificationChannel(chan)
        return s
    }

    private fun getStepCount(){

        Log.d(TAG, "getStepCount: called")
        if (totalStepCount ==0 || totalStepCount < oldStepCount){
            oldStepCount = totalStepCount
            saveOldStepCount()
        }
        Log.d(TAG, "getStepCount: totalStepCount $totalStepCount")
        Log.d(TAG, "getStepCount: oldStepCount $oldStepCount")
        Log.d(TAG, "getStepCount: currentStepCount $currentStepCount")
        //TODO: minus yesterday's last sensor read
        val date = Calendar.getInstance()
        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        date.set(Calendar.MILLISECOND,0)
        Log.d(TAG, "getStepCount: todayStartDate ${date.time}")
        //TODO get last reboot time
        val lastRebootTime = SystemClock.elapsedRealtime()
        val currentTime = Calendar.getInstance().timeInMillis - lastRebootTime
        val timestampTime = timestamp?.toDate()?.time
        if(timestamp != null && timestamp!!.toDate().time.milliseconds > currentTime.milliseconds){
                Log.d(TAG, "getStepCount: timestamp ${timestamp!!.toDate().time}")
                Log.d(TAG, "getStepCount: lastRebootTime $lastRebootTime")
                val lastSensorRead = timestamp!!.toDate().time - lastRebootTime
                Log.d(TAG, "getStepCount: lastSensorRead $lastSensorRead")
//                currentStepCount = totalStepCount - oldStepCount - lastsensorStepCount
//                Log.d(TAG, "getStepCount: currentStepCount $currentStepCount")
         }
            currentStepCount += (totalStepCount - oldStepCount)

        Log.d(TAG, "getStepCount: NEW currentStepCount $currentStepCount")
        oldStepCount = totalStepCount
        saveOldStepCount()
        saveCurrentStepCount()
        Log.d(TAG, "getStepCount: $currentStepCount")
    }

    private fun setDistanceTravelled(){
        db.collection("users")
            .document(UID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val height = it.getLong("Height")?.toDouble()?.times(100.00).toString().format("%.3f").toDouble()
                    Log.d(TAG, "setDistanceTravelled: $height")
                    val gender = it.get("Gender")
                    val stepLength = height.times(if(gender== "Male") 0.415 else 0.41)
                    val distanceTravelled = (currentStepCount * stepLength)/100

                    db.collection("users")
                        .document(UID)
                        .collection("sensorsData")
                        .document("distanceTravelled")
                        .update(mapOf("${LocalDate.now()}.distanceTravelled" to distanceTravelled,
                            "timestamp" to FieldValue.serverTimestamp()))
                        .addOnSuccessListener {
                            Log.d(TAG, "setDistanceTravelled: Success")
                        }
                }
            }
    }

    private fun calculateCaloriesBurnt(){

    }

    private fun loadStepCount() {
//        val db = Firebase.firestore
//        val firebaseAuth = FirebaseAuth.getInstance()
//        val UID = firebaseAuth.currentUser?.uid
        if (UID != null) {
            db.collection("users")
                .document(UID)
                .collection("sensorsData")
                .document("steps")
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        it.getLong("${LocalDate.now()}.currentStepCount")?.let {
                                it1 -> currentStepCount = it1.toInt() }

                        it.getLong("${LocalDate.now()}.oldStepCount")?.let {
                                it1 -> oldStepCount = it1.toInt() }

                        it.getTimestamp("timestamp")?.let {
                               it1 -> timestamp = it1
                        }
                        it.getLong("${LocalDate.now().minusDays(1)}.oldStepCount")?.let {
                                it1 -> lastsensorStepCount = it1.toInt() }
                        Log.d(TAG, "loadCurrentStepCount: $currentStepCount")
                        Log.d(TAG, "loadOldStepCount: $oldStepCount")
                        Log.d(TAG, "loadtimestamp: $timestamp")
                        Log.d(TAG, "loadLastSensorStepCount: $lastsensorStepCount")
                    }
                    else{
                        currentStepCount = 0
                        oldStepCount = 0
                        timestamp = null
                        lastsensorStepCount = 0
                    }
                }
        }
    }

    private fun saveCurrentStepCount() {
        Log.d(TAG, "saveCurrentStepCount: $currentStepCount")
//        val db = Firebase.firestore
//        val firebaseAuth = FirebaseAuth.getInstance()
//        val data = hashMapOf(
//            "${LocalDate.now()}.currentStepCount" to currentStepCount
//        )
//        val UID = firebaseAuth.currentUser?.uid
        if (UID != null) {
            db.collection("users")
                .document(UID)
                .collection("sensorsData")
                .document("steps")
                .update(mapOf("${LocalDate.now()}.currentStepCount" to currentStepCount,
                    "timestamp" to FieldValue.serverTimestamp()))
                .addOnSuccessListener {
                    Log.d(TAG, "saveCurrentStepCount: Success")
                }
        }
    }

    private fun saveOldStepCount(){
        Log.d(TAG, "saveOldStepCount: $oldStepCount")
//        val db = Firebase.firestore
//        val firebaseAuth = FirebaseAuth.getInstance()
//        val data = hashMapOf(
//            "${LocalDate.now()}.oldStepCount" to oldStepCount
//        )
        val UID = firebaseAuth.currentUser?.uid
        if (UID != null) {
            db.collection("users")
                .document(UID)
                .collection("sensorsData")
                .document("steps")
                .update("${LocalDate.now()}.oldStepCount", oldStepCount)
                .addOnSuccessListener {
                    Log.d(TAG, "saveOldStepCount: Success")
                }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_STEP_COUNTER){
            totalStepCount = event.values[0].toInt()
            Log.d(TAG, "onSensorChanged: $totalStepCount")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}