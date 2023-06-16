package com.example.myfitzone.Views

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.myfitzone.Models.AuthenticationModel
import com.example.myfitzone.Models.DatabaseModel
import com.example.myfitzone.Models.UserDetailModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentRegistrationBinding
import java.util.Calendar


class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val TAG = "RegistrationFragment"
    private lateinit var authModel : AuthenticationModel
    private lateinit var userDetailModel : UserDetailModel
    private lateinit var databaseModel : DatabaseModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authModel = ViewModelProvider(requireActivity())[AuthenticationModel::class.java]
        userDetailModel = ViewModelProvider(requireActivity())[UserDetailModel::class.java]
        databaseModel = ViewModelProvider(requireActivity())[DatabaseModel::class.java]
        binding.buttonLoginBottomRegister.setOnClickListener { onLoginClick() }
        binding.buttonLoginTopRegister.setOnClickListener { onLoginClick() }
        binding.buttonRegister.setOnClickListener { onRegisterClick() }


        authModel.exception.observe(viewLifecycleOwner) {
            Log.e(TAG, "onViewCreated: ", it)
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            if(it.message == "Register Successful" && authModel.checkUser()){
                view.findNavController().navigate(R.id.action_registrationFragment_to_userDetailsFragment)
                userDetailModel.setUsername(binding.usernameRegister.text.toString())
                userDetailModel.setEmail(binding.emailRegister.text.toString())
                userDetailModel.setUID(authModel.getUser().uid)
                databaseModel.createUser(userDetailModel.getUser())
            }
            else{
                binding.passwordRegister.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun onLoginClick(){
        requireView().findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
    }

    //TODO: Handle Errors
    private fun onRegisterClick() {
        if(binding.usernameRegister.text.isEmpty() || binding.emailRegister.text.isEmpty() || binding.passwordRegister.text.isEmpty()){
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }
        val email = binding.emailRegister.text.toString().trim()
        val password = binding.passwordRegister.text.toString()
        authModel.register(email, password)

    }

}