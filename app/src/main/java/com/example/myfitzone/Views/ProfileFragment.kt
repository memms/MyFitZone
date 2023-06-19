package com.example.myfitzone.Views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myfitzone.MainActivity
import com.example.myfitzone.Models.AuthenticationModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentHomeBinding
import com.example.myfitzone.databinding.FragmentProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authmodel: AuthenticationModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editButtonProfile.setOnClickListener {
            showBottomSheetDialog()
        }
        authmodel = ViewModelProvider(requireActivity())[AuthenticationModel::class.java]
    }

    private fun showBottomSheetDialog() {
        val bottomSheetFragment: BottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetFragment.setContentView(R.layout.profile_dialog)
        bottomSheetFragment.show()
        bottomSheetFragment.findViewById<LinearLayout>(R.id.logout_row_profile_diag)?.setOnClickListener {
            bottomSheetFragment.dismiss()
            logout()
        }
        bottomSheetFragment.findViewById<LinearLayout>(R.id.first_row_profile_diag)?.setOnClickListener {
            bottomSheetFragment.dismiss()
            editProfile()
        }
    }

    fun logout(){
        authmodel.logout()
    }

    fun editProfile(){

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}