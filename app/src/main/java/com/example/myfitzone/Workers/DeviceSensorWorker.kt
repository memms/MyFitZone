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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.Calendar


class DeviceSensorWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context,
    workerParams), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private var stepCount: Int = 0
    private val TAG = "DeviceSensorWorker"

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
            Log.d(TAG, "doWork: $stepCount")
            saveStepCount(stepCount)
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

//    private fun getStepCount(): Int{
//        val sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//        sensor.let {
//            if (it != null) {
//                val stepCount = it.getStringType()
//                return stepCount.toInt()
//            } else {
//                return 0
//            }
//        }
//    }

    private fun saveStepCount(stepCount: Int){
        Log.d(TAG, "saveStepCount: $stepCount")
        val db = Firebase.firestore
        val data = hashMapOf(
            "stepCount" to stepCount
        )
        val firebaseAuth = FirebaseAuth.getInstance()
        val UID = firebaseAuth.currentUser?.uid
        if (UID != null) {
            db.collection("users")
                .document(UID)
                .collection("sensorsdata")
                .document(LocalDateTime.now().toString())
                .set(data)
                .addOnSuccessListener {
                    Log.d(TAG, "saveStepCount: Success")
                }
        }
    }



    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_STEP_COUNTER){
            stepCount = event.values[0].toInt()
            Log.d(TAG, "onSensorChanged: $stepCount")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}