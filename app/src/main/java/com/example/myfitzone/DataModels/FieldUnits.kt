package com.example.myfitzone.DataModels

import android.util.Log

class FieldUnits {
    companion object {

        fun unitOfDashboard(listOfFields: List<String>): String {
            val stringBuilder = StringBuilder()
            for(field in listOfFields){
                stringBuilder.append(tableUnitsMetrics[field] ?: field)
                stringBuilder.append("\n")
            }
            return stringBuilder.toString()
        }
        fun valueOf(attribute: String): Int {
            return when (table[attribute]) {
                Int -> 0
                Double -> 1
                String -> 2
                else -> -1
            }
        }
        fun unitOf(attribute: String): String {
            //TODO: add user settings to determine units
            Log.d("FieldUnits", "unitOf: $attribute")
            return tableUnitsImperial[attribute]!!
        }
        fun unitOfBody(attribute: String): String {
            //TODO: add user settings to determine units
            return when (tableBodyUnitsImperial[attribute]) {
                null -> "in"
                else -> tableBodyUnitsImperial[attribute]!!
            }
//            return when (tableBodyUnitsMetric[attribute]){
//                null -> "m"
//                else -> tableBodyUnitsMetric[attribute]!!
//            }
        }
        val WEIGHT: String = "Weight"
        val REPS: String = "Reps"
        val SPEED: String = "Speed"
        val DISTANCE: String = "Distance"
        val DURATION: String = "Duration"
        val CALORIES: String = "Calories"
        val REST: String = "Rest"
        val INCLINE: String = "Incline"
        val RESISTANCE: String = "Resistance"
        val INTENSITY: String = "Intensity"
        val SETS: String = "sets"

        private val tableBodyUnitsMetric: Map<String, String> = mapOf(
            "Weight" to "kg",
            "Body Fat %" to "%",
            "Calories" to "kcal",
            "Resting Heart Rate" to "bpm",
            "Sleep" to "hrs",
        )
        private val tableBodyUnitsImperial: Map<String, String> = mapOf(
            "Weight" to "lbs",
            "Body Fat %" to "%",
            "Calories" to "kcal",
            "Resting Heart Rate" to "bpm",
            "Sleep" to "hrs",
        )
        private val tableUnitsMetrics: Map<String, String> = mapOf(
            "Weight" to "kg",
            "Reps" to "reps",
            "Distance" to "km",
            "Duration" to "mins",
            "Calories" to "kcal",
            "Speed" to "km/h",
            "Resistance" to "kg",
            "Incline" to "deg",
            "Heart Rate" to "bpm",
            "Rest" to "mins",
            "Intensity" to "RPE",
        )
        private val tableUnitsImperial: Map<String, String> = mapOf(
            "Weight" to "lbs",
            "Reps" to "reps",
            "Distance" to "mi",
            "Duration" to "mins",
            "Calories" to "kcal",
            "Speed" to "mph",
            "Resistance" to "lbs",
            "Incline" to "deg",
            "Heart Rate" to "bpm",
            "Rest" to "mins",
            "Intensity" to "RPE",
        )
        private val table: Map<String, *> = mapOf(
            "Weight" to Double,
            "Reps" to Int,
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

