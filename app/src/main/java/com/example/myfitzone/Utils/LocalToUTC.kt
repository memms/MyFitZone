package com.example.myfitzone.Utils

import java.text.SimpleDateFormat
import java.util.TimeZone
import kotlin.time.Duration.Companion.milliseconds

fun convertLocalToUTC(timeInMilis: Long): Long {
    try{
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timeInMilis
        calendar.timeZone = TimeZone.getDefault()
        return calendar.timeInMillis - TimeZone.getTimeZone("UTC").rawOffset.toLong()
    }
    catch (e: Exception){
        return 0
    }
}
fun String.capitalizeFirst(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}