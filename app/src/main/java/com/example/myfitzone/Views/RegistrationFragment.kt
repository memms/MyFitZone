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
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentRegistrationBinding
import java.util.Calendar


class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val TAG = "RegistrationFragment"
    private lateinit var authModel : AuthenticationModel
    private lateinit var databaseModel : DatabaseModel

    private var DOB = Calendar.getInstance()


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
        databaseModel = ViewModelProvider(requireActivity())[DatabaseModel::class.java]

        binding.buttonLoginBottomRegister.setOnClickListener { onLoginClick() }
        binding.buttonLoginTopRegister.setOnClickListener { onLoginClick() }
        binding.buttonRegister.setOnClickListener { onRegisterClick() }

        binding.bdayRegister.setOnClickListener { inflateDatePickers() }

        authModel.exception.observe(viewLifecycleOwner) {
            Log.e(TAG, "onViewCreated: ", it)
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            binding.passwordRegister.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun inflateDatePickers(){
        val calendar =  DOB
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in textbox
            binding.bdayRegister.text = ""+ (monthOfYear+1) + "/" + dayOfMonth + "/" + year
            DOB.set(year, monthOfYear, dayOfMonth)
        }, year, month, day)

        dpd.show()
    }

    private fun onLoginClick(){
        requireView().findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
    }

    private fun onRegisterClick(){
        val username = binding.usernameRegister.text.toString()
        val email = binding.emailRegister.text.toString().trim()
        val password = binding.passwordRegister.text.toString()
        authModel.register(email, password)


    }

}