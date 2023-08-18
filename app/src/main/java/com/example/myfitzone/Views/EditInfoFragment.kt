package com.example.myfitzone.Views

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.User
import com.example.myfitzone.Models.AuthenticationModel
import com.example.myfitzone.Models.DatabaseUserModel
import com.example.myfitzone.databinding.FragmentEditInfoBinding
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class EditInfoFragment : Fragment() {

    private val TAG = "EditInfoFragment"
    private var _binding: FragmentEditInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var authenticationModel: AuthenticationModel
    private lateinit var databaseUserModel: DatabaseUserModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authenticationModel = ViewModelProvider(requireActivity())[AuthenticationModel::class.java]
        databaseUserModel = ViewModelProvider(requireActivity())[DatabaseUserModel::class.java]

        //"Edit Username", "Edit Name", "Edit Bio", "Edit Profile Picture","Edit Email",  "Edit Password"
        arguments?.let {
            when(it.getString("editType")){
                "Edit Username" -> editUsername()
                "Edit Name" -> editName()
                "Edit Bio" -> editBio()
                "Edit Profile Picture" -> editProfilePicture()
                "Edit Email" -> editEmail()
                "Edit Password" -> editPassword()
            }
        }
    }

    private fun editUsername(){
        getCurrentUserData(object : FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val user = result as User
                user.let {
                    binding.firstTextEditprof.apply {
                        text = "Current: ${it.username}"
                        visibility = View.VISIBLE
                    }
                }
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }


        })
        binding.firstEditprof.apply {
            hint = "Enter your new username"
            inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            visibility = View.VISIBLE
        }
        binding.secondEditprof.apply {
            hint = "Enter your password"
            inputType = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            visibility = View.VISIBLE
        }
        binding.thirdEditprof.visibility = View.GONE
        binding.confirmEditprof.setOnClickListener {
            val newUsername = binding.firstEditprof.text.toString()
            if(newUsername.isEmpty()){
                Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val password = binding.secondEditprof.text.toString()
            authenticationModel.getUser().email?.let { it1 ->
                authenticationModel.reauthUser(it1, password, object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        if(result==true){
                            dialogInflator("Confirm Username Change", "Are you sure that you want to change your username to $newUsername", object : FirestoreGetCompleteAny {
                                override fun onGetComplete(result: Any) {
                                    if(result == true){
                                        databaseUserModel.updateAValue("username", newUsername, callback = object: FirestoreGetCompleteAny{
                                            override fun onGetComplete(result: Any) {
                                                Toast.makeText(requireContext(), "Username changed successfully", Toast.LENGTH_SHORT).show()
                                                completeEdit()
                                            }

                                            override fun onGetFailure(string: String) {
                                                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                            }

                                        })

                                    }
                                    else{
                                        Toast.makeText(requireContext(), "Username change cancelled", Toast.LENGTH_SHORT).show()
                                        binding.firstEditprof.text?.clear()
                                        binding.secondEditprof.text?.clear()
                                    }
                                }

                                override fun onGetFailure(string: String) {
                                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                }

                            })
                        }
                        else{
                            try {
                                Toast.makeText(
                                    requireContext(),
                                    "Authentication failed, check current Email and Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.firstEditprof.text?.clear()
                                binding.secondEditprof.text?.clear()
                            }
                            catch (e: Exception){

                            }
                        }
                    }

                    override fun onGetFailure(string: String) {

                    }

                })
            }
        }
    }

    private fun getCurrentUserData(callback: FirestoreGetCompleteAny){
        databaseUserModel.getUserData(authenticationModel.getUser().uid, callback)
    }

    private fun editName(){
        getCurrentUserData(object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val user = result as User
                user.let {
                    binding.firstTextEditprof.apply {
                        text = "Current: ${it.name["first"]} ${it.name["last"]}"
                        visibility = View.VISIBLE
                    }
                }
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }

        })
        binding.firstEditprof.apply {
            hint = "Enter a first name"
            inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            visibility = View.VISIBLE
        }
        binding.secondEditprof.apply {
            hint = "Enter a last name"
            inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            visibility = View.VISIBLE
        }
        binding.thirdEditprof.apply {
            hint = "Enter your password"
            inputType = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            visibility = View.VISIBLE
        }
        binding.confirmEditprof.setOnClickListener {
            val firstname = binding.firstEditprof.text.toString()
            val lastname = binding.secondEditprof.text.toString()
            val name = mapOf(
                "first" to firstname,
                "last" to lastname
            )
            val password = binding.thirdEditprof.text.toString()
            authenticationModel.getUser().email?.let { it1 ->
                authenticationModel.reauthUser(it1, password, object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        if(result==true){
                            dialogInflator("Confirm Username Change", "Are you sure that you want to change your username to $firstname $lastname", object : FirestoreGetCompleteAny {
                                override fun onGetComplete(result: Any) {
                                    if(result == true){
                                        databaseUserModel.updateAValue("name", name, callback = object: FirestoreGetCompleteAny{
                                            override fun onGetComplete(result: Any) {
                                                Toast.makeText(requireContext(), "Name changed successfully", Toast.LENGTH_SHORT).show()
                                                completeEdit()
                                            }

                                            override fun onGetFailure(string: String) {
                                                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                            }

                                        })

                                    }
                                    else{
                                        Toast.makeText(requireContext(), "Name change cancelled", Toast.LENGTH_SHORT).show()
                                        binding.firstEditprof.text?.clear()
                                        binding.secondEditprof.text?.clear()
                                        binding.thirdEditprof.text?.clear()
                                    }
                                }

                                override fun onGetFailure(string: String) {
                                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                }

                            })
                        }
                        else{
                            try {
                                Toast.makeText(
                                    requireContext(),
                                    "Authentication failed, check current Email and Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.firstEditprof.text?.clear()
                                binding.secondEditprof.text?.clear()
                            }
                            catch (e: Exception){

                            }
                        }
                    }

                    override fun onGetFailure(string: String) {

                    }

                })
            }
        }
    }

    private fun editBio(){

    }

    private fun editProfilePicture(){

    }

    private fun editEmail(){
        val user = authenticationModel.getUser()
        val email = user.email
        val hiddenEmail = email!!.replaceRange(2, email.indexOf('@'), "*****")
        binding.titleEditprof.text = "Edit Email"
        binding.firstTextEditprof.apply {
            text = "Current: $hiddenEmail"
            visibility = View.VISIBLE
        }
        binding.firstEditprof.apply {
            hint = "Enter current email"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            visibility = View.VISIBLE
        }
        binding.secondEditprof.apply {
            hint = "Enter your new email"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            visibility = View.VISIBLE
        }
        binding.thirdEditprof.apply {
            hint = "Enter your password"
            inputType = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            visibility = View.VISIBLE
        }
        binding.confirmEditprof.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val currentEmail = binding.firstEditprof.text.toString()
                val newEmail = binding.secondEditprof.text.toString()
                if(currentEmail != email){
                    Toast.makeText(requireContext(), "Current email does not match", Toast.LENGTH_SHORT).show()
                    binding.firstEditprof.text?.clear()
                    binding.secondEditprof.text?.clear()
                    binding.thirdEditprof.text?.clear()
                    return@setOnClickListener
                }
                val password = binding.thirdEditprof.text.toString()
                authenticationModel.reauthUser(currentEmail, password, object : FirestoreGetCompleteAny {
                    override fun onGetComplete(result: Any) {
                        if(result == true){
                            dialogInflator("Confirm Email Change", "Are you sure that you want to change your email from: \n" +
                                    "$currentEmail \nto\n$newEmail \n" +
                                    "You may need to sign in again.", object : FirestoreGetCompleteAny {
                                override fun onGetComplete(result: Any) {
                                    if(result == true){
                                        authenticationModel.updateEmail(newEmail, object : FirestoreGetCompleteAny {
                                            override fun onGetComplete(result: Any) {
                                                when (result) {
                                                    true -> {
                                                        Toast.makeText(requireContext(), "Email changed successfully", Toast.LENGTH_SHORT).show()
                                                        completeEdit()
                                                        databaseUserModel.updateAValue(
                                                            "email",
                                                            newEmail,
                                                            object: FirestoreGetCompleteAny {
                                                                override fun onGetComplete(result: Any) {}

                                                                override fun onGetFailure(string: String) {
                                                                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                                                }

                                                            })
                                                    }
                                                    is FirebaseAuthUserCollisionException -> {
                                                        Toast.makeText(requireContext(), "Email already in use", Toast.LENGTH_SHORT).show()
                                                        binding.secondEditprof.text?.clear()
                                                        binding.thirdEditprof.text?.clear()
                                                    }
                                                    else -> {
                                                        Toast.makeText(requireContext(), "Email change failed", Toast.LENGTH_SHORT).show()
                                                        binding.secondEditprof.text?.clear()
                                                        binding.thirdEditprof.text?.clear()
                                                    }
                                                }
                                            }

                                            override fun onGetFailure(string: String) {
                                                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                                binding.thirdEditprof.text?.clear()
                                            }

                                        })
                                    }
                                    else{
                                        Toast.makeText(requireContext(), "Email change cancelled", Toast.LENGTH_SHORT).show()
                                        binding.secondEditprof.text?.clear()
                                        binding.thirdEditprof.text?.clear()
                                    }
                                }

                                override fun onGetFailure(string: String) {
                                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                }

                            })
                        }
                        else{
                            try {
                                Toast.makeText(
                                    requireContext(),
                                    "Authentication failed, check current Email and Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.firstEditprof.text?.clear()
                                binding.secondEditprof.text?.clear()
                                binding.thirdEditprof.text?.clear()
                            }
                            catch (e: Exception){

                            }
                        }
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    /**
     * Dialog inflator for confirmation
     * @return true if user confirms, false if user cancels
     */
    private fun dialogInflator(title: String, text:String, callback: FirestoreGetCompleteAny){
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setTitle(title)
            setMessage(text)
            setPositiveButton("OK"){ dialog, _ ->
                dialog.dismiss()
                callback.onGetComplete(true)
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                callback.onGetComplete(false)
            }
            show()
        }
    }

    private fun completeEdit(){
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun editPassword(){
        val user = authenticationModel.getUser()
        val email = user.email
        val hiddenEmail = email!!.replaceRange(2, email.indexOf('@'), "*****")
        binding.titleEditprof.text = "Edit Email"
        binding.firstTextEditprof.apply {
            text = "Current: $hiddenEmail"
            visibility = View.VISIBLE
        }
        binding.firstEditprof.apply {
            hint = "Enter current email"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            visibility = View.VISIBLE
        }
        binding.secondEditprof.apply {
            hint = "Enter your current password"
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            //make hidden
            visibility = View.VISIBLE
        }
        binding.thirdEditprof.apply {
            hint = "Enter your new password"
            inputType = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            visibility = View.VISIBLE
        }
        binding.confirmEditprof.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val currentEmail = binding.firstEditprof.text.toString()
                val password = binding.secondEditprof.text.toString()
                if(currentEmail != email){
                    Toast.makeText(requireContext(), "Current email does not match", Toast.LENGTH_SHORT).show()
                    binding.firstEditprof.text?.clear()
                    binding.secondEditprof.text?.clear()
                    binding.thirdEditprof.text?.clear()
                    return@setOnClickListener
                }
                authenticationModel.reauthUser(currentEmail, password, object : FirestoreGetCompleteAny {
                    override fun onGetComplete(result: Any) {
                        if(result == true){
                            dialogInflator("Confirm Password Change", "Are you sure that you want to change your password?\nYou may need to re-login", object : FirestoreGetCompleteAny {
                                override fun onGetComplete(result: Any) {
                                    if(result == true){
                                        val newPassword = binding.thirdEditprof.text.toString()
                                        authenticationModel.updatePassword(newPassword, callback = object: FirestoreGetCompleteAny{
                                            override fun onGetComplete(result: Any) {
                                                when (result) {
                                                    true -> {   //will require relogin
                                                        Toast.makeText(requireActivity(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                                                        completeEdit()
                                                    }
                                                    else -> {
                                                        Toast.makeText(requireContext(), "Password change failed", Toast.LENGTH_SHORT).show()
                                                        binding.secondEditprof.text?.clear()
                                                        binding.thirdEditprof.text?.clear()
                                                    }
                                                }
                                            }

                                            override fun onGetFailure(string: String) {
                                                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                                binding.thirdEditprof.text?.clear()
                                                binding.secondEditprof.text?.clear()
                                            }

                                        })

                                    }
                                    else{
                                        Toast.makeText(requireContext(), "Password change cancelled", Toast.LENGTH_SHORT).show()
                                        binding.secondEditprof.text?.clear()
                                        binding.thirdEditprof.text?.clear()
                                    }
                                }

                                override fun onGetFailure(string: String) {
                                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                                }

                            })
                        }
                        else{
                            try {
                                Toast.makeText(
                                    requireContext(),
                                    "Authentication failed, check current Email and Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.firstEditprof.text?.clear()
                                binding.secondEditprof.text?.clear()
                                binding.thirdEditprof.text?.clear()
                            }
                            catch (e: Exception){
                                Log.e(TAG, "onGetComplete: ", e)
                            }
                        }
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}