package com.example.myfitzone.Workers

import android.app.NotificationChannel
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.time.LocalDate


class DeviceSensorWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context,
    workerParams), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private var totalStepCount: Int = 0
    private var oldStepCount: Int = 0
    private var currentStepCount: Int = 0
    private val TAG = "DeviceSensorWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: ")
        try {
            setForeground(createForegroundInfo("MyFitZone", "Tracking your steps"))
            sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            Log.d(TAG, "doWork: registerListener")
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)

//            delay(1000)
            Log.d(TAG, "doWork: unregisterListener")
            sensorManager.unregisterListener(this@DeviceSensorWorker)
//            val stepCount = getStepCount()
            Log.d(TAG, "doWork: $totalStepCount")
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
        currentStepCount += (totalStepCount - oldStepCount)
        Log.d(TAG, "getStepCount: NEW currentStepCount $currentStepCount")
        oldStepCount = totalStepCount
        saveOldStepCount()
        saveCurrentStepCount()
        Log.d(TAG, "getStepCount: $currentStepCount")
    }

    private fun loadStepCount() {
        val db = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()
        val UID = firebaseAuth.currentUser?.uid
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
                        Log.d(TAG, "loadCurrentStepCount: $currentStepCount")
                        Log.d(TAG, "loadOldStepCount: $oldStepCount")
                    }
                    else{
                        currentStepCount = 0
                        oldStepCount = 0
                    }
                }
        }
    }

    private fun saveCurrentStepCount() {
        Log.d(TAG, "saveCurrentStepCount: $currentStepCount")
        val db = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()
        val data = hashMapOf(
            "${LocalDate.now()}.currentStepCount" to currentStepCount
        )
        val UID = firebaseAuth.currentUser?.uid
        if (UID != null) {
            db.collection("users")
                .document(UID)
                .collection("sensorsData")
                .document("steps")
                .update("${LocalDate.now()}.currentStepCount", currentStepCount)
                .addOnSuccessListener {
                    Log.d(TAG, "saveCurrentStepCount: Success")
                }
        }
    }

    private fun saveOldStepCount(){
        Log.d(TAG, "saveOldStepCount: $oldStepCount")
        val db = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()
        val data = hashMapOf(
            "${LocalDate.now()}.oldStepCount" to oldStepCount
        )
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