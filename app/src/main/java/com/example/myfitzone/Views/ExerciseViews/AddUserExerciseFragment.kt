package com.example.myfitzone.Views.ExerciseViews

import android.content.Context
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
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.myfitzone.AdapterHelper.HeaderItem
import com.example.myfitzone.AdapterHelper.ListItem
import com.example.myfitzone.AdapterHelper.SubItem
import com.example.myfitzone.DataModels.DatabaseExercise
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.Models.UserNewExercisesModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.AddUserExerciseItemLayoutBinding
import com.example.myfitzone.databinding.FragmentAddUserExerciseBinding
import com.example.myfitzone.databinding.HeaderItemLinearBinding


class AddUserExerciseFragment : Fragment() {

    private val TAG = "AddUserExerciseFragment"
    private var _binding: FragmentAddUserExerciseBinding? = null
    private val binding get() = _binding!!
    private lateinit var userExerciseModel: UserNewExercisesModel
    private var exerciseTemplate: DatabaseExercise? = null
    private var attributesList: MutableMap<String, ArrayList<String>>? = mutableMapOf()
    private var setNumber = 0
    private lateinit var adapter: AddUserExerciseAdapter
    private val list = ArrayList<ListItem>();

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentAddUserExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userExerciseModel = ViewModelProvider(requireActivity())[UserNewExercisesModel::class.java]
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
        binding.addAttributeAddUserExercise.setOnClickListener {
//              addAttributesPressed(
            addSet()
        }
        binding.saveAddUserExercise.setOnClickListener {
            saveExercise()
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
        for (attribute in exerciseTemplate?.exerciseFieldsList!!) {
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
        TODO("Not yet implemented")

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

    inner class AddUserExerciseAdapter(

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return list[position].getType()
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val viewHolder: RecyclerView.ViewHolder
            val inflater = LayoutInflater.from(parent.context)
            when (viewType) {
                ListItem.TYPE_HEADER -> {
                    val v1 = inflater.inflate(R.layout.header_item_linear, parent, false)
                    viewHolder = HeaderViewHolder(v1)
                }

                ListItem.TYPE_ITEM -> {
                    val v2 = inflater.inflate(R.layout.add_user_exercise_item_layout, parent, false)
                    viewHolder = SubItemViewHolder(v2)
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
                    itemViewHolder.measurement.setText(item.getMeasurement())

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


                }

                else -> throw IllegalArgumentException("Invalid type of data $holder.adapterPosition")
            }
        }

        inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val header = itemView.findViewById<TextView>(R.id.header_item_linear_text_view)

        }

        inner class SubItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name = itemView.findViewById<Spinner>(R.id.field_type_spinner_add_user_exercise)
            val measurement =
                itemView.findViewById<EditText>(R.id.field_value_edit_text_add_user_exercise)
            val onClickListenerEditText = measurement.addTextChangedListener(object : TextWatcher {
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
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }
                }
            }
        }
    }
}