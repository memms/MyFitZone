package com.example.myfitzone.Views.ExerciseViews

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.AdapterHelper.HeaderItem
import com.example.myfitzone.AdapterHelper.ListItem
import com.example.myfitzone.AdapterHelper.SubItem
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.DatabaseExercise
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.UserExercise
import com.example.myfitzone.Models.UserExercisesModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentAddUserExerciseBinding


class AddUserExerciseFragment : Fragment() {

    private val TAG = "AddUserExerciseFragment"
    private var _binding: FragmentAddUserExerciseBinding? = null
    private val binding get() = _binding!!
    private lateinit var userExerciseModel: UserExercisesModel
    private var exerciseTemplate: DatabaseExercise? = null
    private var attributesList: MutableMap<String, ArrayList<String>>? = mutableMapOf()
    private var setNumber = 0
    private lateinit var adapter: AddUserExerciseAdapter
    private val list = ArrayList<ListItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding =
            FragmentAddUserExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userExerciseModel = ViewModelProvider(requireActivity())[UserExercisesModel::class.java]
        exerciseTemplate = userExerciseModel.getSelectedExercise()
        binding.titleAddUserExercise.text = exerciseTemplate?.exerciseName
        binding.descriptionAddUserExercise.text = exerciseTemplate?.exerciseDescription
        binding.exerciseFieldsAddUserExercise.text =
            "Exercise Fields: ${exerciseTemplate?.exerciseFieldsList.toString().trim('[', ']')}"
        adapter = AddUserExerciseAdapter()
        addDefaultAttributes()
        calculateList()

        binding.attributesRecyclerviewAddUserExercise.adapter = adapter
        binding.attributesRecyclerviewAddUserExercise.layoutManager =
            LinearLayoutManager(requireContext())


        binding.exitAddUserExercise.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
//        binding.addAttributeAddUserExercise.setOnClickListener {
//            TODO()
//        }
        binding.addFieldAddUserExercise.setOnClickListener {
            addAttributesPressed()
        }
        binding.saveAddUserExercise.setOnClickListener {
            saveToDatabase()
        }
        binding.addSetAddUserExercise.setOnClickListener {
            addSet()
        }

    }

    private fun addSet() {
        setNumber++
        saveExercise()
        calculateList()
    }

    private fun saveExercise() {
        for (attribute in attributesList?.keys!!) {
            attributesList?.get(attribute)?.clear()
        }
        Log.d(TAG, "saveExercise: $list")
        for (i in 0 until list.size) {
            val item = list[i]
            if (item is SubItem) {
                val attributeName = item.getName()
                val attributeValue = item.getMeasurement()
                Log.d(TAG, "saveExercise: $attributeName $attributeValue")
                if(attributesList?.containsKey(attributeName) == false) attributesList?.put(attributeName, arrayListOf())
                attributesList?.get(attributeName)?.add(attributeValue)
                Log.d(TAG, "saveExercise: $attributesList")
            }
        }
        Log.d(TAG, "saveExercise: $attributesList")
    }

    private fun addDefaultAttributes() {
        if (exerciseTemplate?.exerciseFieldsList == null) return
        for (attribute in exerciseTemplate?.exerciseFieldsList!!) {
            Log.d(TAG, "addDefaultAttributes: $attribute")
            when (FieldUnits.valueOf(attribute)) {
                0 -> attributesList?.put(attribute, arrayListOf())
                1 -> attributesList?.put(attribute, arrayListOf())
                2 -> attributesList?.put(attribute, arrayListOf())
            }
            Log.d(TAG, "addDefaultAttributes: List $attributesList")
        }
        setNumber++
    }

    private fun addAttributesPressed() {
        saveExercise()
        val builder = AlertDialog.Builder(requireContext())
        val mView = layoutInflater.inflate(R.layout.field_radio_group_dialog, null)
        val fieldsL = resources.getStringArray(R.array.ExerciseFields)
        val fieldstoAdd = ArrayList<String>()
        val fieldstoRemove = ArrayList<String>()
        for (i in 1 until fieldsL.size){
            val checkBox = CheckBox(requireContext())
            checkBox.text = fieldsL[i]
            checkBox.setTextColor(Color.BLACK)
//            checkBox.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            checkBox.textSize = 20f
            if(attributesList?.contains(fieldsL[i]) == true) {
                checkBox.isChecked = true
            }
            checkBox.setOnClickListener {
                if(checkBox.isChecked && attributesList?.containsKey(checkBox.text.toString()) == false){
                    fieldstoAdd.add(checkBox.text.toString())
                }else if (!checkBox.isChecked
                    && attributesList?.containsKey(checkBox.text.toString()) == false
                    && fieldstoAdd.contains(checkBox.text.toString())){
                    fieldstoAdd.remove(checkBox.text.toString())
                }
                else if(!checkBox.isChecked
                    && attributesList?.containsKey(checkBox.text.toString()) == true
                    && !fieldstoAdd.contains(checkBox.text.toString())){
                    fieldstoRemove.add(checkBox.text.toString())
                    //TODO:
                    //Add dialog to ask if user is sure he wants to remove the field and all its values
                    //If yes, delete the field from the list and remove it from the list
                    //If no, do nothing

                }
                else if(checkBox.isChecked
                    && attributesList?.containsKey(checkBox.text.toString()) == true
                    && fieldstoRemove.contains(checkBox.text.toString())){
                    fieldstoRemove.remove(checkBox.text.toString())
                }
            }

            mView.findViewById<RadioGroup>(R.id.radio_group_field_dialog).addView(checkBox)
        }
        with (builder){
            val dialog = create()
            setPositiveButton("OK"){_,_ ->
                Log.d(TAG, "addAttributesPressed:fieldstoAdd $fieldstoAdd")
                Log.d(TAG, "addAttributesPressed:attributesList $attributesList")
                addToList(fieldstoAdd)
                deleteFromList(fieldstoRemove)
                setFields()
            }
            setNegativeButton("Cancel"){_,_ ->
                dialog.cancel()
            }
            setView(mView)
            show()
        }




    }

    private fun deleteFromList(fieldstoRemove: ArrayList<String>) {
        var j: Int
        for(field in fieldstoRemove){
            j=1
            while (j < list.size){
                val listItem = list[j]
                if(listItem is SubItem){
                    if(listItem.getName() == field){
                        list.removeAt(j)
                        attributesList?.remove(field)
                        adapter.notifyItemRemoved(j)
                    }
                }
                j++
            }
        }

    }

    private fun addToList(fieldstoAdd: ArrayList<String>){
        var j: Int
        for(field in fieldstoAdd){
            j=1
            attributesList?.put(field, arrayListOf())
            while (j < list.size){
                val subItem = SubItem()
                subItem.setName(field)
                subItem.setMeasurement("")
                val listItem = list[j]
                Log.d(TAG, "addToList: J $j")
                if(listItem is HeaderItem){
                    val index = j
                    list.add(index, subItem)
                    j++
                    adapter.notifyItemInserted(index)
                }
                j++
            }
        }
        for(field in fieldstoAdd){
            val subItem = SubItem()
            subItem.setName(field)
            subItem.setMeasurement("")
            list.add(subItem)
            adapter.notifyItemInserted(list.size-1)
        }

    }

    fun removeSet(set: Int, position: Int){
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            val dialog = create()
            setTitle("Delete Set")
            setMessage("Are you sure you want to delete set $set?")
            setPositiveButton("Yes") { _, _ ->
                list.removeAt(position)
                adapter.notifyItemRemoved(position)
                var i = 0
                while (position < list.size){
                    if(list[position] is HeaderItem){
                        for(j in position until list.size){
                            if(list[j] is HeaderItem){
                                val headerItem = list[j] as HeaderItem
                                headerItem.setHeader("Set ${set+i}")
                                i++
                                adapter.notifyItemChanged(j)
                            }
                        }
                        break
                    }
                    list.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
                setNumber--
            }
            setNegativeButton("No") { _, _ ->
                //Do nothing
                dialog.cancel()
            }

            show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setFields(){
        binding.exerciseFieldsAddUserExercise.text =  "Exercise Fields: ${attributesList?.keys.toString().trim('[', ']')}"
    }
    private fun saveToDatabase(){
        saveExercise()
        if(setNumber == 0){
            Toast.makeText(requireContext(), "Please add at least one set", Toast.LENGTH_SHORT).show()
            return
        }
        if(attributesList?.keys?.size == 0){
            Toast.makeText(requireContext(), "Please add at least one field", Toast.LENGTH_SHORT).show()
            return
        }
        var userExercise: UserExercise? = null
        val fieldMap:HashMap<String, Any> = hashMapOf()
        fieldMap["sets"] = setNumber
        for(attribute in attributesList?.keys!!){
            if(attributesList?.get(attribute)?.contains("") == true){
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }
        }

        fieldMap.putAll(attributesList as Map<String, Any>)
        exerciseTemplate?.let {
            userExercise = UserExercise(
                it.exerciseGroup,
                it.exerciseName,
                binding.notesAddUserExercise.text.toString(),
                fieldMap,
                System.currentTimeMillis(),
                )
        }


        userExercise?.let {
            userExerciseModel.saveUserExercise(it, template = exerciseTemplate!!, object : FirestoreGetCompleteCallbackArrayList{
                override fun onGetComplete(result: ArrayList<String>) {
                    if(result[0] == "noLeaderBoard"){
                        Toast.makeText(requireContext(), "Data Added but leaderboard not updated due to mismatch in fields", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(
                            requireContext(),
                            "Exercise and Leaderboards Updated",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
                override fun onGetFailure(string: String) {
                    Toast.makeText(requireContext(), "Exercise Not Saved $string", Toast.LENGTH_SHORT).show()
                }

            })
        }

    }

    private fun calculateList() {

        val headerItem = HeaderItem()
        headerItem.setHeader("Set $setNumber")
        list.add(headerItem)
        adapter.notifyItemInserted(list.size - 1)
        for (attribute in attributesList?.keys!!) {
            val subItem = SubItem()
            subItem.setName(attribute)
            subItem.setMeasurement("")
            list.add(subItem)
            adapter.notifyItemInserted(list.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        userExerciseModel.clearSelectedExercise()
    }

    inner class AddUserExerciseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return list[position].getType()
        }

        override fun getItemCount(): Int {
            return list.size
        }



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val viewHolder: RecyclerView.ViewHolder
            val inflater = LayoutInflater.from(parent.context)
            viewHolder = when (viewType) {
                ListItem.TYPE_HEADER -> {
                    val v1 = inflater.inflate(R.layout.header_item_linear, parent, false)
                    HeaderViewHolder(v1)
                }

                ListItem.TYPE_ITEM -> {
                    val v2 = inflater.inflate(R.layout.add_user_exercise_item_layout, parent, false)
                    SubItemViewHolder(v2, OnClickListeners())
                }

                else -> throw IllegalArgumentException("Invalid view type")
            }
            return viewHolder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            when (holder.itemViewType) {
                ListItem.TYPE_HEADER -> {
                    val item = list[holder.adapterPosition] as HeaderItem
                    val itemViewHolder = holder as HeaderViewHolder
                    itemViewHolder.header.text = item.getHeader()

                }

                ListItem.TYPE_ITEM -> {
                    val item = list[holder.adapterPosition] as SubItem
                    val itemViewHolder = holder as SubItemViewHolder
//                    itemViewHolder.listener.updatePosition(holder.adapterPosition)
                    itemViewHolder.measurement.setText(item.getMeasurement())
//                    itemViewHolder.measurement.addTextChangedListener(itemViewHolder.listener)

                    val fieldsL = resources.getStringArray(R.array.ExerciseFields)
                    val spinnerAdapter = object : ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        fieldsL
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
                    holder.name.adapter = spinnerAdapter
                    holder.name.setSelection(fieldsL.indexOf(item.getName()))
                    holder.units.text = FieldUnits.unitOf(item.getName())

                }

                else -> throw IllegalArgumentException("Invalid type of data $holder.adapterPosition")
            }
        }

        inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val header: TextView = itemView.findViewById(R.id.header_item_linear_text_view)
            val remove: Button = itemView.findViewById(R.id.remove_header_button)
            init {
                remove.setOnClickListener {
                    val position = adapterPosition
                    val headerItem = list[position] as HeaderItem
                    val set = headerItem.getHeader().split(" ")[1].toInt()
                    removeSet(set = set, position = position)
                }
            }
        }

        inner class SubItemViewHolder(itemView: View, onClickListeners: OnClickListeners) : RecyclerView.ViewHolder(itemView) {
            val name: Spinner = itemView.findViewById(R.id.field_type_spinner_add_user_exercise)
            val measurement: EditText =
                itemView.findViewById(R.id.field_value_edit_text_add_user_exercise)
            val units: TextView =
                itemView.findViewById(R.id.units_text_view_add_user_exercise)
            val listen = measurement.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val item = list[adapterPosition] as SubItem
                    item.setMeasurement(p0.toString())
                    list[adapterPosition] = item
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })
//            val listener = onClickListeners

            init {
                name.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val item = list[adapterPosition] as SubItem
                        item.setName(parent?.getItemAtPosition(position).toString())
                        list[adapterPosition] = item
                        units.text = FieldUnits.unitOf(parent?.getItemAtPosition(position).toString())
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }

            }


        }
        inner class OnClickListeners : TextWatcher{
            private var positionLoc: Int? = null

            fun updatePosition(position: Int){
                positionLoc = position
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val item = list[positionLoc!!] as SubItem
                item.setMeasurement(p0.toString())
                list[positionLoc!!] = item
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        }
    }
}