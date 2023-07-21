package com.example.myfitzone.DataModels

class FieldUnits(){
    companion object {
        fun valueOf(attribute: String): Int {
            return when (table[attribute]) {
                Int -> 0
                Double -> 1
                String -> 2
                else -> -1
            }
        }
        private val table: Map<String, *> = mapOf(
            "Weight" to Double,
            "Reps" to Int,
            "Sets" to Int,
            "Distance" to Double,
            "Duration" to Double,
            "Calories" to Int,
            "Speed" to Double,
            "Resistance" to Double,
            "Incline" to Double,
            "Heart Rate" to Int,
            "Rest Time" to Double,
            "Intensity" to Double,
            "Rest" to Double,
        )
    }
}

