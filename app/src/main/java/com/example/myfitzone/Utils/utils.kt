package com.example.myfitzone.Utils

import android.util.Log
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.PublicSocialData
import com.example.myfitzone.DataModels.UserExercise
import java.util.TimeZone

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

fun UserExercise.toPublicSocialData(): PublicSocialData{
    val stringBuilder = StringBuilder()
    for (field in this.fieldmap) {
        //average it out
        if (field.key != "sets") {
            stringBuilder.append(findAverage(field.value as List<*>))
            stringBuilder.append("\n")
        } else {
            stringBuilder.append(field.value)
            stringBuilder.append("\n")
        }
    }
    return PublicSocialData(
        name = this.name,
        value = stringBuilder.toString().trim('\n'),
        unit = FieldUnits.unitOfDashboard(this.fieldmap.keys.toList()).trim('\n'),
        updated = this.timeAdded,
        type = "exercise",
        logo = ""
    )
}

private fun findAverage(list: List<*>): String{
    var sum = 0.0
    for(item in list){
        sum += item.toString().toDouble()
    }
    val average = String.format("%.1f", sum/list.size)
    return average
}