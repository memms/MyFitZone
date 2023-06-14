package com.example.myfitzone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance();
        val controller = Navigation.findNavController(this, R.id.fragment)
        auth.addAuthStateListener {
            Log.i(TAG, "AuthState changed to ${it.currentUser?.uid}")
            if (it.currentUser != null) {
                Log.i(TAG, "User is logged in")
                controller.navigate(R.id.homeFragment)
            } else {
                Log.i(TAG, "User is logged out")
                //TODO: Navigate to login screen
                controller.navigate(R.id.loginFragment)
            }
        }

    }
}