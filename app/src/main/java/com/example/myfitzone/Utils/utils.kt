package com.example.myfitzone.Utils

import android.util.Log
import com.example.myfitzone.DataModels.FieldUnits
import com.example.myfitzone.DataModels.PublicSocialData
import com.example.myfitzone.DataModels.UserBodyMetrics
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
    val fieldStringBuilder = StringBuilder()
    for (field in this.fieldmap) {
        //average it out
        if (field.key != "sets") {
            stringBuilder.append(findAverage(field.value as List<*>))
            stringBuilder.append("\n")
            fieldStringBuilder.append(field.key)
            fieldStringBuilder.append("\n")
        } else {
            stringBuilder.append(field.value)
            stringBuilder.append("\n")
            fieldStringBuilder.append(field.key)
            fieldStringBuilder.append("\n")
        }
    }
    return PublicSocialData(
        name = this.name,
        value = stringBuilder.toString().trim('\n'),
        fields = fieldStringBuilder.toString().trim('\n'),
        unit = FieldUnits.unitOfDashboard(this.fieldmap.keys.toList()).trim('\n'),
        updated = this.timeAdded,
        type = "exercise",
        logo = "",
        uid = ""
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

fun UserBodyMetrics.toPublicSocialData(): PublicSocialData{
    return PublicSocialData(
        name = this.metricName,
        value = this.metricValue.toString(),
        fields = "",
        unit = FieldUnits.unitOfBody(this.metricName),
        updated = this.timestamp,
        type = "bodyMeasure",
        logo = "",
        uid = ""
    )
}

fun Double.toMetricWeight(): Float{
    return (this / 2.205).toFloat()
}

fun Double.toImperialWeight(): Float{
    return (this * 2.205).toFloat()
}

fun Double.toMetricHeight(): Float{
    return (Math.round((this/3.281)*10000.0)/10000.0).toFloat()
}

fun Double.toImperialHeight(): Float{
    return (Math.round((this*3.281)*10000.0)/10000.0).toFloat()
}