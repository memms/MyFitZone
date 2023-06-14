package com.example.myfitzone.Views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myfitzone.Models.DatabaseModel
import com.example.myfitzone.Models.UserDetailModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentUserDetailsBinding
import java.util.Calendar


class UserDetailsFragment : Fragment() {

    private var _binding: FragmentUserDetailsBinding? = null
    private val binding get() = _binding!!

    private var DOB = Calendar.getInstance()

    private lateinit var registrationUserDetails : UserDetailModel
    private lateinit var databaseModel : DatabaseModel
    private var weight = 0.0
    private var tempHeight = listOf<Int>(0,0)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.DOBUserinfo.setOnClickListener { inflateDatePickers() }
        binding.weightUserinfo.setOnClickListener{ weightAlertBuilder() }
        binding.heightUserinfo.setOnClickListener{ heightAlertBuilder() }
        registrationUserDetails = ViewModelProvider(requireActivity())[UserDetailModel::class.java]
        databaseModel = ViewModelProvider(requireActivity())[DatabaseModel::class.java]
        setSpinner()
        binding.nextUserinfo.setOnClickListener { onNextClick() }
    }

    private fun setSpinner(){
        val items = resources.getStringArray(R.array.Genders)
        val mySpinner = binding.genderUserinfo
        val spinnerAdapter = object: ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner
                // First item will be used for hint
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView =
                    super.getDropDownView(position, convertView, parent) as TextView
                //set the color of first item in the drop down list to gray
                if (position == 0) {
                    view.setTextColor(Color.GRAY)
                } else {
                    //here it is possible to define color for other items by
                    //view.setTextColor(Color.RED)
                }
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = spinnerAdapter

        mySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = parent!!.getItemAtPosition(position).toString()
                if(value == items[0]){
                    (view as TextView).setTextColor(Color.GRAY)
                }
                else{
                    registrationUserDetails.setGender(value)
                }
            }

        }

    }


    @SuppressLint("SetTextI18n")
    private fun inflateDatePickers(){
        val calendar =  DOB
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in textbox
            binding.DOBUserinfo.text = ""+ (monthOfYear+1) + "/" + dayOfMonth + "/" + year
            DOB.set(year, monthOfYear, dayOfMonth)
        }, year, month, day)

        dpd.show()
    }

    fun onNextClick(){
        //first name
        registrationUserDetails.setFirstName(binding.fnameUserinfo.text.toString())
        //last name
        registrationUserDetails.setLastName(binding.lnameUserinfo.text.toString())
        //DOB
        registrationUserDetails.setDOB(binding.DOBUserinfo.text.toString())
        //Height
        val heightDec = tempHeight[0].toDouble() + (tempHeight[1].toDouble()/12)
        val meters = Math.round((heightDec/3.281)*10000.0)/10000.0
        registrationUserDetails.setHeight(meters.toFloat())
        //Weight
        registrationUserDetails.setWeight((weight/2.205).toFloat())

        if(sufficientDetails()){
            Log.d("UserDetailsFragment", "User Details: ${registrationUserDetails.getUser().toString()}")
            databaseModel.createUser(registrationUserDetails.getUser())
        }

    }

    fun sufficientDetails():Boolean{
        return  registrationUserDetails.getEmail() != "" &&
                registrationUserDetails.getUID() != "" &&
                registrationUserDetails.getUsername() != "" &&
                registrationUserDetails.getFirstName() != "" &&
                registrationUserDetails.getLastName() != "" &&
                registrationUserDetails.getDOB() != "" &&
                registrationUserDetails.getHeight() != 0.0f &&
                registrationUserDetails.getWeight() != 0.0f &&
                registrationUserDetails.getGender() != ""
    }

    @SuppressLint("SetTextI18n")
    fun weightAlertBuilder(){
        val builder = AlertDialog.Builder(requireContext())
        val mView = layoutInflater.inflate(R.layout.numberpicker_dialog, null)
        val numberPicker1 = mView.findViewById<NumberPicker>(R.id.weight_int_picker)
        with(numberPicker1){
            minValue = 1
            maxValue = 1000
            wrapSelectorWheel = true
            value= (weight/10).toInt()
        }
        val numberPicker2 = mView.findViewById<NumberPicker>(R.id.weight_double_picker)
        with(numberPicker2){
            minValue = 0
            maxValue = 9
            wrapSelectorWheel = true
            value= weight.toInt()%10
        }
        with(builder){
            setTitle("Weight")
            val dialog = create()
            setPositiveButton("OK"){_,_ ->
                weight = numberPicker1.value.toDouble() + numberPicker2.value.toDouble()/10
                binding.weightUserinfo.text = "${weight.toString()} lbs"
                Toast.makeText(requireContext(), "Weight set to $weight lbs", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            setNegativeButton("Cancel"){_,_ ->
                Toast.makeText(requireContext(), "Weight not set", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }
            setView(mView)
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun heightAlertBuilder(){
        val builder = AlertDialog.Builder(requireContext())
        val mView = layoutInflater.inflate(R.layout.numberpicker_dialog, null)
        mView.let {
            it.findViewById<TextView>(R.id.decimal).text = "ft"
            it.findViewById<TextView>(R.id.units_end).text = "in"
        }
        val numberPicker1 = mView.findViewById<NumberPicker>(R.id.weight_int_picker)
        with(numberPicker1){
            minValue = 1
            maxValue = 9
            wrapSelectorWheel = true
            value= tempHeight[0]
        }
        val numberPicker2 = mView.findViewById<NumberPicker>(R.id.weight_double_picker)
        with(numberPicker2){
            minValue = 0
            maxValue = 11
            wrapSelectorWheel = true
            value= tempHeight[1]
        }
        with(builder){
            setTitle("Height")
            val dialog = create()
            setPositiveButton("OK"){_,_ ->
                tempHeight = listOf(numberPicker1.value, numberPicker2.value)
                binding.heightUserinfo.text = "${tempHeight[0]}' ${tempHeight[1]}\" ft"
                Toast.makeText(requireContext(), "Height set to ${tempHeight[0]}' ${tempHeight[1]}\" ft ", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            setNegativeButton("Cancel"){_,_ ->
                Toast.makeText(requireContext(), "Height not set", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }
            show()
        }
    }

    fun loadingAlertBuilder(){
        val builder = AlertDialog.Builder(requireContext())
        val mView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val dialog = builder.create()
        with(builder){
            setTitle("Loading")
            setView(mView)
            setCancelable(false)
            show()
        }
    }


}