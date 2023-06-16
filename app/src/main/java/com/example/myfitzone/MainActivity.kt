package com.example.myfitzone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.myfitzone.Models.UserDetailModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val UserDetailModel = ViewModelProvider(this)[UserDetailModel::class.java]
        val auth = FirebaseAuth.getInstance();
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
                            controller.navigate(R.id.userDetailsFragment)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
            } else {
                Log.i(TAG, "User is logged out")
                Toast.makeText(this, "User is logged out", Toast.LENGTH_SHORT).show()
                //TODO: Navigate to login screen
                controller.navigate(R.id.loginFragment)
            }
        }
    }
}