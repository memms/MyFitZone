package com.example.myfitzone.AdapterHelper

import com.example.myfitzone.DataModels.UserExercise

class SubItem: ListItem() {
    override fun getType(): Int {
        return TYPE_ITEM
    }

    override fun toString(): String {
        return "SubItem(attributeName='$attributeName', attributeMeasurement='$attributeMeasurement')"
    }

    private lateinit var attributeName: String
    private lateinit var attributeMeasurement: String

    fun setName(string: String) {
        this.attributeName = string
    }
    fun setMeasurement(measurement: String) {
        this.attributeMeasurement = measurement
    }

    fun getName(): String {
        return attributeName
    }

    fun getMeasurement(): String {
        return attributeMeasurement
    }


}