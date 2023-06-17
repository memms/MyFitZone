package com.example.myfitzone

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.myfitzone.BroadcastRecievers.NetworkConectivityReceiver
import com.example.myfitzone.BroadcastRecievers.NetworkConectivityReceiver.Companion.registerNetworkConnectivityListener
import com.example.myfitzone.Models.UserDetailModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NetworkConectivityReceiver.NetworkConnectivityListener {

    private val TAG = "MainActivity"
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                            controller.navigate(R.id.go_home)
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
            }
        }


    }

    override fun onResume() {
        super.onResume()
        registerNetworkConnectivityListener(this)
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
}