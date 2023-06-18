package com.example.myfitzone.Views.InitialAuthViews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myfitzone.Models.AuthenticationModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val TAG = "LoginFragment"

    private lateinit var authModel : AuthenticationModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authModel = ViewModelProvider(requireActivity())[AuthenticationModel::class.java]
        binding.buttonRegisterBottomLogin.setOnClickListener { onRegisterClick() }
        binding.buttonRegisterTopLogin.setOnClickListener { onRegisterClick() }
        binding.buttonForgotPasswordLogin.setOnClickListener { onForgotPasswordClick() }
        binding.buttonLogin.setOnClickListener { onLoginClick() }
    }

    private fun onLoginClick() {
        Log.d(TAG, "onLoginClick: ")
        if(binding.emailLogin.text.toString().isEmpty()){
            Log.d(TAG, "onLoginClick: Email is empty")
            Toast.makeText(requireContext(), "Email is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if(binding.passwordLogin.text.toString().isEmpty()){
            Log.d(TAG, "onLoginClick: Password is empty")
            Toast.makeText(requireContext(), "Password is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()
        authModel.login(email, password)

    }

    private fun onForgotPasswordClick() {
        TODO("Not yet implemented")
    }

    private fun onRegisterClick() {
        findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}