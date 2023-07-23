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

        fun unitOf(attribute: String): String {
            //TODO: add user settings to determine units
            return tableUnitsImperial[attribute]!!
        }
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
            "Distance" to "miles",
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

