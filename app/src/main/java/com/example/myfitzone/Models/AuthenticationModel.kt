package com.example.myfitzone.Models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthenticationModel (application: Application) :AndroidViewModel(application){
    private val context = getApplication<Application>().applicationContext

    private lateinit var auth: FirebaseAuth
    private val TAG = "AuthenticationModel"

    init {
        auth = Firebase.auth
    }

    fun setLanguage(language: String) {
        auth.setLanguageCode(language)
    }

    /*
     * Check if user is logged in
     */
    fun checkUser() : Boolean {
        //get user
        val currentUser = auth.currentUser
        //check if user is null (not logged in)
        return currentUser != null
    }

    fun getUser() : FirebaseUser {
        return auth.currentUser!!
    }

    fun register(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Register success
                        Log.d(TAG, "createUserWithEmail:success")

                    } else {
                        // Sign in failed
                        //updateUI(null)
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "createUserWithEmail:failure", e)
        }
    }

    fun login(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d(TAG, "signInWithEmail:success")

                    } else {
                        // Sign in failed
                        //updateUI(null)
                        Log.w(TAG, "signInWithEmail:failure", task.exception)

                    }
                }
            } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail:failure", e)

        }
    }

    fun updateProfile(profileChangeRequest: UserProfileChangeRequest) {
        try {
            auth.currentUser!!.updateProfile(profileChangeRequest)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update profile success
                        Log.d(TAG, "Profile updated.")
                    } else {
                        // Update profile failed
                        Log.w(TAG, "Profile not updated.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Profile not updated.", e)
        }
    }


    fun updateEmail(email: String) {
        try {
            auth.currentUser!!.updateEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set email success
                        Log.d(TAG, "Email updated.")
                    } else {
                        // Set email failed
                        Log.w(TAG, "Email not updated.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Email not updated.", e)
        }
    }

    fun updatePassword(password: String) {
        try {
            auth.currentUser!!.updatePassword(password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set password success
                        Log.d(TAG, "Password updated.")
                    } else {
                        // Set password failed
                        Log.w(TAG, "Password not updated.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Password not updated.", e)
        }
    }

    fun resetPasswordEmail(email: String) {
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Reset password success
                        Log.d(TAG, "Email sent.")
                    } else {
                        // Reset password failed
                        Log.w(TAG, "Email not sent.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Email not sent.", e)
        }
    }

    fun reauthUser(email: String, password: String) {
        try {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser!!.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Reauth user success
                        Log.d(TAG, "User reauthenticated.")
                    } else {
                        // Reauth user failed
                        Log.w(TAG, "User not reauthenticated.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "User not reauthenticated.", e)
        }
    }

    /*
     * Delete user
     * Requires reauthentication
     */
    fun deleteUser() {
        try {
            auth.currentUser!!.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Delete user success
                        Log.d(TAG, "User deleted.")
                    } else {
                        // Delete user failed
                        Log.w(TAG, "User not deleted.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "User not deleted.", e)
        }
    }

    fun sendEmailVerification() {
        try {
            auth.currentUser!!.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Email verification sent
                        Log.d(TAG, "Email verification sent.")
                    } else {
                        // Email verification failed
                        Log.w(TAG, "Email verification not sent.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Email verification not sent.", e)
        }
    }



    fun logout() {
        auth.signOut()
    }



}