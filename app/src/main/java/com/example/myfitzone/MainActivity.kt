package com.example.myfitzone

import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myfitzone.BroadcastRecievers.NetworkConectivityReceiver
import com.example.myfitzone.BroadcastRecievers.NetworkConectivityReceiver.Companion.registerNetworkConnectivityListener
import com.example.myfitzone.BroadcastRecievers.NetworkConectivityReceiver.Companion.unregisterNetworkConnectivityListener
import com.example.myfitzone.DataModels.User
import com.example.myfitzone.Models.UserDetailModel
import com.example.myfitzone.Workers.DeviceSensorWorker
import com.example.myfitzone.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NetworkConectivityReceiver.NetworkConnectivityListener {

    private val TAG = "MainActivity"
    private var snackbar: Snackbar? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var userDetailModel: UserDetailModel

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userDetailModel = ViewModelProvider(this)[UserDetailModel::class.java]

        registerReceiver(NetworkConectivityReceiver(), IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))

        val UserDetailModel = ViewModelProvider(this)[UserDetailModel::class.java]
        val auth = FirebaseAuth.getInstance();
        auth.currentUser?.let { it.reload() }
        val controller = Navigation.findNavController(this, R.id.fragment)


        auth.addAuthStateListener {
            Log.i(TAG, "AuthState changed to ${it.currentUser?.uid}")
            if (it.currentUser != null) {
                Log.i(TAG, "User is logged in")
                val docRef = Firebase.firestore.collection("users").document(it.currentUser?.uid.toString())
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document.data?.get("DOB").toString().isNotEmpty()) {
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                            var user: User
                            document.let { user = it.toObject(User::class.java)!! }
                            user.UID = it.currentUser?.uid.toString()
                            UserDetailModel.setUser(user)
                            controller.navigate(R.id.go_home)
                            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                                createWorker()
                            }
                        } else {
                            Log.d(TAG, "No such document")
                            UserDetailModel.setUsername(document.data?.get("username").toString())
                            UserDetailModel.setEmail(document.data?.get("email").toString())
                            UserDetailModel.setUID(it.currentUser?.uid.toString())
                            controller.navigate(R.id.go_userDetails)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
            } else {
                Log.i(TAG, "User is logged out")
                Toast.makeText(this, "User is logged out", Toast.LENGTH_SHORT).show()
                //TODO: Navigate to login screen
                controller.navigate(R.id.go_login)
                userDetailModel.setUser(User())
                Log.d(TAG, "UserDetailModel: ${userDetailModel.getUser()}")
            }
        }


        binding.bottomNavMain
            .setupWithNavController(controller)
        controller.addOnDestinationChangedListener() { _, _, argument ->
            if(argument?.getBoolean("ShowNavBar") == false) {
                binding.bottomNavMain.visibility = BottomNavigationView.GONE
            } else {
                binding.bottomNavMain.visibility = BottomNavigationView.VISIBLE
            }
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
        }
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//        if (sensor == null) {
//            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Sensor detected on this device", Toast.LENGTH_SHORT).show()
//        }
//        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    private fun createWorker() {
        Log.d(TAG, "createWorker: ")
        val workRequest = PeriodicWorkRequestBuilder<DeviceSensorWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("DeviceSensorWorker",
            ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }

    override fun onResume() {
        super.onResume()
        registerNetworkConnectivityListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkConnectivityListener(this)
    }

    override fun onNetworkConnectivityChanged(connection: Int) {
        showNetworkMessage(connection)
    }

    private fun showNetworkMessage(connection: Int) {
        if (connection == 0) {
            snackbar = Snackbar.make(
                findViewById(R.id.fragment),
                "You are offline! Data will be synced when you are online.",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar?.show()
        } else {
            snackbar?.dismiss()
            if(snackbar != null) {
                snackbar = Snackbar.make(
                    findViewById(R.id.fragment),
                    "You are online",
                    Snackbar.LENGTH_SHORT
                )
                snackbar?.show()
            }
            snackbar = null
        }
    }

    //TODO Remove below code

}


